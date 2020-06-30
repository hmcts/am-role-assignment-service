package uk.gov.hmcts.reform.roleassignment.domain.service.deleteroles;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.RequestEntity;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.Request;
import uk.gov.hmcts.reform.roleassignment.domain.model.RequestedRole;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ParseRequestService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ValidationModelService;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DeleteRoleAssignmentOrchestrator {


    private PersistenceService persistenceService;
    private ParseRequestService parseRequestService;
    private ValidationModelService validationModelService;
    RequestEntity requestEntity;
    AssignmentRequest assignmentRequest;

    public DeleteRoleAssignmentOrchestrator(PersistenceService persistenceService,
                                            ParseRequestService parseRequestService,
                                            ValidationModelService validationModelService) {
        this.persistenceService = persistenceService;
        this.parseRequestService = parseRequestService;
        this.validationModelService = validationModelService;
    }

    public ResponseEntity<Object> deleteRoleAssignment(String actorId,
                                                       String process,
                                                       String reference) throws Exception {
        List<RequestedRole> requestedRoles = null;

        //1. create the request Object
        Request request = parseRequestService.prepareRequestObject(process, reference);
        assignmentRequest = new AssignmentRequest();

        //2. Call persistence service to store only the request
        requestEntity = persistenceService.persistRequest(request);
        requestEntity.setHistoryEntities(new HashSet<>());
        request.setId(requestEntity.getId());

        //3. retrieve all assignment records based on actorId/process+reference
        if (actorId != null) {
            requestedRoles = persistenceService.getRoleAssignment(UUID.fromString(actorId));

        } else if (process != null && reference != null) {
            requestedRoles = persistenceService.getExistingRoleByProcessAndReference(
                process,
                reference,
                Status.LIVE.toString()
            );

        }

        //4. call validation rule
        if (requestedRoles != null && !requestedRoles.isEmpty()) {
            validationByDrool(request, requestedRoles);
        }

        //5. persist the  requested roles  and update status
        updateStatusAndPersist(request);

        //6. check status updated by drools and take decision
        checkAllDeleteApproved(assignmentRequest);


        return new ResponseEntity<>(HttpStatus.NO_CONTENT);

    }

    private void validationByDrool(Request request, List<RequestedRole> requestedRoles) throws Exception {
        assignmentRequest.setRequest(request);
        assignmentRequest.setRequestedRoles(requestedRoles);

        //calling drools rules for validation
        validationModelService.validateRequest(assignmentRequest);


    }

    private void updateStatusAndPersist(Request request) {
        for (RequestedRole requestedRole : assignmentRequest.getRequestedRoles()) {
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

    private void checkAllDeleteApproved(AssignmentRequest parsedAssignmentRequest) {
        // decision block
        List<RequestedRole> deleteApprovedRoles = parsedAssignmentRequest.getRequestedRoles().stream()
            .filter(role -> role.getStatus().equals(
                Status.DELETE_APPROVED)).collect(
                Collectors.toList());

        if (deleteApprovedRoles.size() == parsedAssignmentRequest.getRequestedRoles().size()) {

            //Delete existing Assignment records
            deleteLiveRecords(parsedAssignmentRequest);

            //insert status deleted in history table
            insertRequestedRole(parsedAssignmentRequest, Status.DELETED);

            // Update request status to approved
            updateRequestStatus(parsedAssignmentRequest, Status.APPROVED);


        } else {


            //Insert requested roles  into history table with status deleted-Rejected
            insertRequestedRole(parsedAssignmentRequest, Status.DELETE_REJECTED);

            // Update request status to REJECTED
            updateRequestStatus(parsedAssignmentRequest, Status.REJECTED);

        }
    }

    private void deleteLiveRecords(AssignmentRequest assignmentRequest) {
        for (RoleAssignment requestedRole : assignmentRequest.getRequestedRoles()) {
            persistenceService.deleteRoleAssignmentByActorId(requestedRole.getActorId());
            persistenceService.persistActorCache(requestedRole);

        }
    }


    private void insertRequestedRole(AssignmentRequest parsedAssignmentRequest, Status status) {
        for (RequestedRole requestedRole : parsedAssignmentRequest.getRequestedRoles()) {
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
