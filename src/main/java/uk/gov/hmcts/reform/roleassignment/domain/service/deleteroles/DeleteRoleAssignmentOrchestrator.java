package uk.gov.hmcts.reform.roleassignment.domain.service.deleteroles;

import static uk.gov.hmcts.reform.roleassignment.v1.V1.Error.BAD_REQUEST_MISSING_PARAMETERS;
import static uk.gov.hmcts.reform.roleassignment.v1.V1.Error.NO_RECORDS_FOUND_BY_ACTOR;
import static uk.gov.hmcts.reform.roleassignment.v1.V1.Error.NO_RECORD_FOUND_BY_ASSIGNMENT_ID;
import static uk.gov.hmcts.reform.roleassignment.v1.V1.Error.NO_RECORDS_FOUND_BY_PROCESS;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.RequestEntity;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.Request;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ParseRequestService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ValidationModelService;

@Service
public class DeleteRoleAssignmentOrchestrator {


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

    public ResponseEntity<Object> deleteRoleAssignment(String actorId,
                                                       String process,
                                                       String reference,
                                                       String assignmentId) throws Exception {
        List<RoleAssignment> requestedRoles = null;

        //1. create the request Object
        if (actorId != null || (process != null && reference != null) || assignmentId != null) {
            request = parseRequestService.prepareDeleteRequest(process, reference, actorId, assignmentId);
            assignmentRequest = new AssignmentRequest(new Request(), Collections.emptyList());
        } else {
            throw new BadRequestException(BAD_REQUEST_MISSING_PARAMETERS);
        }

        //2. Call persistence service to store only the request
        requestEntity = persistenceService.persistRequest(request);
        requestEntity.setHistoryEntities(new HashSet<>());
        request.setId(requestEntity.getId());

        //3. retrieve all assignment records based on actorId/process+reference
        if (actorId != null) {
            requestedRoles = persistenceService.getAssignmentsByActor(UUID.fromString(actorId));
            if (requestedRoles.isEmpty()) {
                throw new ResourceNotFoundException(String.format(NO_RECORDS_FOUND_BY_ACTOR + "%s", actorId));
            }

        } else if (process != null && reference != null) {
            requestedRoles = persistenceService.getAssignmentsByProcess(
                process,
                reference,
                Status.LIVE.toString());
            if (requestedRoles.isEmpty()) {
                throw new ResourceNotFoundException(String.format(NO_RECORDS_FOUND_BY_PROCESS, process, reference));
            }
        } else {
            requestedRoles = persistenceService.getAssignmentById(UUID.fromString(assignmentId));
            if (requestedRoles.isEmpty()) {
                throw new ResourceNotFoundException(String.format(NO_RECORD_FOUND_BY_ASSIGNMENT_ID, assignmentId));
            }
        }

        //4. call validation rule
        if (requestedRoles != null && !requestedRoles.isEmpty()) {
            validationByDrool(request, requestedRoles);
        }

        //5. persist the  requested roles  and update status
        updateStatusAndPersist(request);

        //6. check status updated by drools and take decision
        checkAllDeleteApproved(assignmentRequest, actorId);

        if (assignmentRequest.getRequest().getStatus().equals(Status.REJECTED)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(assignmentRequest.getRequest());
        } else {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }


    }

    private void validationByDrool(Request request, List<RoleAssignment> requestedRoles) throws Exception {
        assignmentRequest.setRequest(request);
        assignmentRequest.setRequestedRoles(requestedRoles);

        //calling drools rules for validation
        validationModelService.validateRequest(assignmentRequest);


    }

    private void updateStatusAndPersist(Request request) {
        for (RoleAssignment requestedRole : assignmentRequest.getRequestedRoles()) {
            requestedRole.setRequest(request);
            if (!requestedRole.getStatus().equals(Status.APPROVED)) {
                requestedRole.status = Status.DELETE_REJECTED;
                requestedRole.statusSequence = Status.DELETE_REJECTED.sequence;
            } else {
                requestedRole.status = Status.DELETE_APPROVED;
                requestedRole.statusSequence = Status.DELETE_APPROVED.sequence;
            }
            // persist history in db
            requestEntity.getHistoryEntities().add(persistenceService.persistHistory(requestedRole, request));

        }

        //Persist request to update relationship with history entities
        persistenceService.persistRequestToHistory(requestEntity);
    }

    private void checkAllDeleteApproved(AssignmentRequest validatedAssignmentRequest, String actorId) {
        // decision block
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
    }

    private void deleteLiveRecords(AssignmentRequest validatedAssignmentRequest, String actorId) {
        if (actorId != null) {
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
            requestedRole.setRequest(parsedAssignmentRequest.getRequest());
            requestedRole.status = status;
            // persist history in db
            requestEntity.getHistoryEntities().add(persistenceService.persistHistory(
                requestedRole,
                parsedAssignmentRequest.getRequest()
            ));


        }

        //Persist request to update relationship with history entities
        persistenceService.persistRequestToHistory(requestEntity);

    }

    private void updateRequestStatus(AssignmentRequest assignmentRequest, Status status) {
        assignmentRequest.getRequest().setStatus(status);
        requestEntity.setStatus(status.toString());
        persistenceService.persistRequestToHistory(requestEntity);

    }


}
