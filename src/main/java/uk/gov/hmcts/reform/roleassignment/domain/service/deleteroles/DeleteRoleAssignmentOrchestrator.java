package uk.gov.hmcts.reform.roleassignment.domain.service.deleteroles;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
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
import uk.gov.hmcts.reform.roleassignment.util.Constants;
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
    RequestEntity requestEntity;
    AssignmentRequest assignmentRequest;
    Request request;

    public DeleteRoleAssignmentOrchestrator(PersistenceService persistenceService,
                                            ParseRequestService parseRequestService,
                                            ValidationModelService validationModelService) {
        this.persistenceService = persistenceService;
        this.parseRequestService = parseRequestService;
        this.validationModelService = validationModelService;
    }


    public ResponseEntity<Object> deleteRoleAssignmentByProcessAndReference(String process,
                                                                            String reference) {
        long startTime = System.currentTimeMillis();
        logger.info(String.format("deleteRoleAssignmentByProcessAndReference execution started at %s", startTime));

        List<RoleAssignment> requestedRoles;

        //1. create the request Object
        if (process != null && reference != null) {
            request = parseRequestService.prepareDeleteRequest(process, reference, "", "");
            assignmentRequest = new AssignmentRequest(request, Collections.emptyList());
        } else {
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
            MultiMap correlationIdHeader = new MultiValueMap();
            correlationIdHeader.put(Constants.CORRELATION_ID_HEADER_NAME,
                                    parseRequestService.getRequestCorrelationId());
            return new ResponseEntity<>(correlationIdHeader, HttpStatus.NO_CONTENT);
        }

        ResponseEntity<Object> responseEntity = performOtherStepsForDelete("", requestedRoles);
        logger.info(String.format(
            "deleteRoleAssignmentByProcessAndReference execution finished at %s . Time taken = %s milliseconds",
            System.currentTimeMillis(),
            System.currentTimeMillis() - startTime
        ));
        return responseEntity;
    }

    public ResponseEntity<Object> deleteRoleAssignmentByAssignmentId(String assignmentId) {
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
            MultiMap correlationIdHeader = new MultiValueMap();
            correlationIdHeader.put(Constants.CORRELATION_ID_HEADER_NAME,
                                    parseRequestService.getRequestCorrelationId());
            return new ResponseEntity<>(correlationIdHeader, HttpStatus.NO_CONTENT);
        }

        return performOtherStepsForDelete("", requestedRoles);

    }

    @NotNull
    private ResponseEntity<Object> performOtherStepsForDelete(String actorId, List<RoleAssignment> requestedRoles) {
        long startTime = System.currentTimeMillis();
        logger.info(String.format("performOtherStepsForDelete execution started at %s", startTime));


        //4. call validation rule
        validationByDrool(requestedRoles);

        //5. persist the  requested roles  and update status
        updateStatusAndPersist(request);

        //6. check status updated by drools and take decision
        checkAllDeleteApproved(assignmentRequest, actorId);
        logger.info(String.format(
            "performOtherStepsForDelete execution finished at %s . Time taken = %s milliseconds",
            System.currentTimeMillis(),
            System.currentTimeMillis() - startTime
        ));
        if (assignmentRequest.getRequest().getStatus().equals(Status.REJECTED)) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .header(Constants.CORRELATION_ID_HEADER_NAME, parseRequestService.getRequestCorrelationId())
                .body(assignmentRequest.getRequest());
        } else {
            MultiMap correlationIdHeader = new MultiValueMap();
            correlationIdHeader.put(Constants.CORRELATION_ID_HEADER_NAME,
                                    parseRequestService.getRequestCorrelationId());
            return new ResponseEntity<>(correlationIdHeader, HttpStatus.NO_CONTENT);
        }

    }

    private void persistInitialRequestForDelete() {

        requestEntity = persistenceService.persistRequest(request);
        requestEntity.setHistoryEntities(new HashSet<>());
        request.setId(requestEntity.getId());
    }

    private void validationByDrool(List<RoleAssignment> requestedRoles) {
        long startTime = System.currentTimeMillis();
        logger.info(String.format("validationByDrool execution started at %s", startTime));

        assignmentRequest.setRequestedRoles(requestedRoles);

        //calling drools rules for validation
        validationModelService.validateRequest(assignmentRequest);
        logger.info(String.format(
            "validationByDrool execution finished at %s . Time taken = %s milliseconds",
            System.currentTimeMillis(),
            System.currentTimeMillis() - startTime
        ));

    }

    private void updateStatusAndPersist(Request request) {
        for (RoleAssignment requestedRole : assignmentRequest.getRequestedRoles()) {
            if (!requestedRole.getStatus().equals(Status.APPROVED)) {
                requestedRole.setStatus(Status.DELETE_REJECTED);
                requestedRole.setStatusSequence(Status.DELETE_REJECTED.sequence);
            } else {
                requestedRole.setStatus(Status.DELETE_APPROVED);
                requestedRole.setStatusSequence(Status.DELETE_APPROVED.sequence);
            }
            // persist history in db
            requestEntity.getHistoryEntities().add(persistenceService.persistHistory(requestedRole, request));

        }

        //Persist request to update relationship with history entities
        persistenceService.updateRequest(requestEntity);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void checkAllDeleteApproved(AssignmentRequest validatedAssignmentRequest, String actorId) {
        // decision block
        long startTime = System.currentTimeMillis();
        logger.info(String.format("checkAllDeleteApproved execution started at %s", startTime));

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
            insertRequestedRole(validatedAssignmentRequest, Status.DELETE_REJECTED);

            // Update request status to REJECTED
            updateRequestStatus(validatedAssignmentRequest, Status.REJECTED);
        }
        logger.info(String.format(
            "checkAllDeleteApproved execution finished at %s . Time taken = %s milliseconds",
            System.currentTimeMillis(),
            System.currentTimeMillis() - startTime
        ));
    }

    public void deleteLiveRecords(AssignmentRequest validatedAssignmentRequest, String actorId) {
        if (!StringUtils.isEmpty(actorId)) {
            for (RoleAssignment requestedRole : validatedAssignmentRequest.getRequestedRoles()) {
                persistenceService.deleteRoleAssignmentByActorId(requestedRole.getActorId());
                persistenceService.persistActorCache(requestedRole);

            }
        } else {
            for (RoleAssignment requestedRole : validatedAssignmentRequest.getRequestedRoles()) {
                persistenceService.deleteRoleAssignment(requestedRole);
                persistenceService.persistActorCache(requestedRole);
            }
        }
    }


    private void insertRequestedRole(AssignmentRequest parsedAssignmentRequest, Status status) {
        for (RoleAssignment requestedRole : parsedAssignmentRequest.getRequestedRoles()) {
            requestedRole.setStatus(status);
            // persist history in db
            requestEntity.getHistoryEntities().add(persistenceService.persistHistory(
                requestedRole,
                parsedAssignmentRequest.getRequest()
            ));
        }

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
