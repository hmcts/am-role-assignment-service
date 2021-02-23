package uk.gov.hmcts.reform.roleassignment.domain.service.deleteroles;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.roleassignment.data.RequestEntity;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.Request;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ParseRequestService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ValidationModelService;
import uk.gov.hmcts.reform.roleassignment.util.PersistenceUtil;
import uk.gov.hmcts.reform.roleassignment.v1.V1;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DeleteRoleAssignmentOrchestrator {

    private static final Logger logger = LoggerFactory.getLogger(DeleteRoleAssignmentOrchestrator.class);

    private PersistenceService persistenceService;
    private ParseRequestService parseRequestService;
    private ValidationModelService validationModelService;
    private PersistenceUtil persistenceUtil;
    private RequestEntity requestEntity;
    AssignmentRequest assignmentRequest;
    private Request request;

    public Request getRequest() {
        return request;
    }

    public RequestEntity getRequestEntity() {
        return requestEntity;
    }

    public void setRequestEntity(RequestEntity requestEntity) {
        this.requestEntity = requestEntity;
    }

    public DeleteRoleAssignmentOrchestrator(PersistenceService persistenceService,
                                            ParseRequestService parseRequestService,
                                            ValidationModelService validationModelService,
                                            PersistenceUtil persistenceUtil) {
        this.persistenceService = persistenceService;
        this.parseRequestService = parseRequestService;
        this.validationModelService = validationModelService;
        this.persistenceUtil = persistenceUtil;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResponseEntity<Void> deleteRoleAssignmentByProcessAndReference(String process,
                                                                            String reference) {
        long startTime = System.currentTimeMillis();
        logger.info("deleteRoleAssignmentByProcessAndReference execution started at {}", startTime);

        List<RoleAssignment> requestedRoles;

        //1. create the request Object
        try {
            if (!process.isBlank() && !reference.isBlank()) {
                request = parseRequestService.prepareDeleteRequest(process, reference, "", "");
                assignmentRequest = new AssignmentRequest(request, Collections.emptyList());
            } else {
                throw new BadRequestException(V1.Error.BAD_REQUEST_MISSING_PARAMETERS);
            }
        } catch (NullPointerException npe) {
            throw new BadRequestException(V1.Error.BAD_REQUEST_MISSING_PARAMETERS);
        }

        //2. Call persistence service to store only the request
        persistInitialRequestForDelete();

        //3. retrieve all assignment records based on actorId/process+reference
        requestedRoles = persistenceService.getAssignmentsByProcess(
            process,
            reference,
            Status.LIVE.toString()
        );
        if (requestedRoles.isEmpty()) {
            requestEntity.setStatus(Status.APPROVED.toString());
            persistenceService.updateRequest(requestEntity);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            //update the records status from Live to Delete_requested for drool to approve it.
            requestedRoles.stream().forEach(roleAssignment -> roleAssignment.setStatus(Status.DELETE_REQUESTED));
        }

        ResponseEntity<Void> responseEntity = performOtherStepsForDelete("", requestedRoles);
        logger.info(
            " >> deleteRoleAssignmentByProcessAndReference execution finished at {} . Time taken = {} milliseconds",
            System.currentTimeMillis(),
            Math.subtractExact(System.currentTimeMillis(), startTime)
        );
        return responseEntity;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResponseEntity<Void> deleteRoleAssignmentByAssignmentId(String assignmentId) {
        List<RoleAssignment> requestedRoles;

        //1. create the request Object
        if (assignmentId != null) {
            request = parseRequestService.prepareDeleteRequest("", "", "", assignmentId);
            assignmentRequest = new AssignmentRequest(request, Collections.emptyList());
        } else {
            throw new BadRequestException(V1.Error.BAD_REQUEST_MISSING_PARAMETERS);
        }

        //2. Call persistence service to store only the request
        persistInitialRequestForDelete();

        //3. retrieve all assignment records based on actorId/process+reference
        requestedRoles = persistenceService.getAssignmentById(UUID.fromString(assignmentId));
        if (requestedRoles.isEmpty()) {
            requestEntity.setStatus(Status.APPROVED.toString());
            persistenceService.updateRequest(requestEntity);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            //update the records status from Live to Delete_requested for drool to approve it.
            requestedRoles.stream().forEach(roleAssignment -> roleAssignment.setStatus(Status.DELETE_REQUESTED));
        }

        return performOtherStepsForDelete("", requestedRoles);

    }

    @NotNull
    private ResponseEntity<Void> performOtherStepsForDelete(String actorId,
                                                                 List<RoleAssignment> requestedRoles) {
        long startTime = System.currentTimeMillis();
        logger.info("performOtherStepsForDelete execution started at {}", startTime);


        //4. call validation rule
        validationByDrool(requestedRoles);

        //5. persist the  requested roles  and update status
        updateStatusAndPersist(request);

        //6. check status updated by drools and take decision
        checkAllDeleteApproved(assignmentRequest, actorId);
        logger.info(
            " >> performOtherStepsForDelete execution finished at {} . Time taken = {} milliseconds",
            System.currentTimeMillis(),
            Math.subtractExact(System.currentTimeMillis(), startTime)
        );
        if (assignmentRequest.getRequest().getStatus().equals(Status.REJECTED)) {
            return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
        } else {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    private void persistInitialRequestForDelete() {

        requestEntity = persistenceService.persistRequest(request);
        requestEntity.setHistoryEntities(new HashSet<>());
        request.setId(requestEntity.getId());
    }

    private void validationByDrool(List<RoleAssignment> requestedRoles) {
        long startTime = System.currentTimeMillis();

        assignmentRequest.setRequestedRoles(requestedRoles);

        //calling drools rules for validation
        validationModelService.validateRequest(assignmentRequest);
        logger.info(
            " >> validationByDrool execution finished at {} . Time taken = {} milliseconds",
            System.currentTimeMillis(),
            Math.subtractExact(System.currentTimeMillis(), startTime)
        );

    }


    public void updateStatusAndPersist(Request request) {
        for (RoleAssignment requestedRole : assignmentRequest.getRequestedRoles()) {

            // persist history in db
            requestEntity.getHistoryEntities()
                .add(persistenceUtil.prepareHistoryEntityForPersistance(requestedRole, request));

        }
        persistenceService.persistHistoryEntities(requestEntity.getHistoryEntities());
        //Persist request to update relationship with history entities
        persistenceService.updateRequest(requestEntity);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void checkAllDeleteApproved(AssignmentRequest validatedAssignmentRequest, String actorId) {
        // decision block
        long startTime = System.currentTimeMillis();

        List<RoleAssignment> deleteApprovedRoles = validatedAssignmentRequest.getRequestedRoles().stream()
            .filter(role -> role.getStatus()
                .equals(Status.DELETE_APPROVED)).collect(Collectors.toList());

        if (!deleteApprovedRoles.isEmpty()
            && deleteApprovedRoles.size() == validatedAssignmentRequest.getRequestedRoles().size()) {

            //Delete existing Assignment records
            deleteLiveRecords(validatedAssignmentRequest, actorId);

            //insert status deleted in history table
            insertRequestedRole(validatedAssignmentRequest, Status.DELETED);

            // Update request status to approved
            updateRequestStatus(validatedAssignmentRequest, Status.APPROVED);

        } else {
            //Insert requested roles  into history table with status deleted-Rejected
            List<RoleAssignment> deleteApprovedRecords = validatedAssignmentRequest.getRequestedRoles().stream()
                .filter(role -> role.getStatus() == Status.DELETE_APPROVED).collect(
                    Collectors.toList());
            validatedAssignmentRequest.setRequestedRoles(deleteApprovedRecords);
            insertRequestedRole(validatedAssignmentRequest, Status.DELETE_REJECTED);

            // Update request status to REJECTED
            updateRequestStatus(validatedAssignmentRequest, Status.REJECTED);
        }
        logger.info(
            " >> checkAllDeleteApproved execution finished at {} . Time taken = {} milliseconds",
            System.currentTimeMillis(),
            Math.subtractExact(System.currentTimeMillis(), startTime)
        );
    }

    public void deleteLiveRecords(AssignmentRequest validatedAssignmentRequest, String actorId) {
        if (!StringUtils.isEmpty(actorId)) {
            for (RoleAssignment requestedRole : validatedAssignmentRequest.getRequestedRoles()) {
                persistenceService.deleteRoleAssignmentByActorId(requestedRole.getActorId());
            }
            persistenceService.persistActorCache(validatedAssignmentRequest.getRequestedRoles());
        } else {
            for (RoleAssignment requestedRole : validatedAssignmentRequest.getRequestedRoles()) {
                persistenceService.deleteRoleAssignment(requestedRole);
            }
            persistenceService.persistActorCache(validatedAssignmentRequest.getRequestedRoles());
        }
    }


    private void insertRequestedRole(AssignmentRequest parsedAssignmentRequest, Status status) {
        for (RoleAssignment requestedRole : parsedAssignmentRequest.getRequestedRoles()) {
            requestedRole.setStatus(status);
            // persist history in db
            requestEntity.getHistoryEntities().add(persistenceUtil.prepareHistoryEntityForPersistance(
                requestedRole,
                parsedAssignmentRequest.getRequest()
            ));
        }
        persistenceService.persistHistoryEntities(requestEntity.getHistoryEntities());
        //Persist request to update relationship with history entities
        persistenceService.updateRequest(requestEntity);

    }

    private void updateRequestStatus(AssignmentRequest assignmentRequest, Status status) {
        assignmentRequest.getRequest().setStatus(status);
        requestEntity.setStatus(status.toString());
        requestEntity.setLog(assignmentRequest.getRequest().getLog());
        persistenceService.updateRequest(requestEntity);

    }


}
