package uk.gov.hmcts.reform.roleassignment.domain.service.createroles;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignment.data.casedata.DefaultCaseDataRepository;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.HistoryEntity;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.RequestEntity;
import uk.gov.hmcts.reform.roleassignment.domain.model.Request;
import uk.gov.hmcts.reform.roleassignment.domain.model.RequestedRole;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ParseRequestService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PrepareResponseService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.RetrieveDataService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ValidationModelService;
import uk.gov.hmcts.reform.roleassignment.domain.service.security.IdamRoleService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;


@Service
public class CreateRoleAssignmentOrchestrator {

    private DefaultCaseDataRepository caseService;
    private IdamRoleService idamService;
    private RetrieveDataService retrieveDataService;
    private ParseRequestService parseRequestService;
    private PersistenceService persistenceService;
    private ValidationModelService validationModelService;

    public CreateRoleAssignmentOrchestrator(DefaultCaseDataRepository caseService, IdamRoleService idamService,
                                            RetrieveDataService retrieveDataService, ParseRequestService parseRequestService,
                                            PersistenceService persistenceService) {
        this.caseService = caseService;
        this.idamService = idamService;
        this.retrieveDataService = retrieveDataService;
        this.parseRequestService = parseRequestService;
        this.persistenceService = persistenceService;
        this.validationModelService = validationModelService;
    }

    public ResponseEntity<Object> createRoleAssignment(AssignmentRequest roleAssignmentRequest) {
        //1. call parse request service
        parseRequestService.parseRequest(roleAssignmentRequest);
        RequestEntity requestEntity;
        //2. Call persistence service to store only the request
        requestEntity = persistInitialRequestAndRoleAssignments(roleAssignmentRequest);
        //3. If replaceExisting boolean is true then fetch all existing assignments based on Process+reference and call delete else
        if (roleAssignmentRequest.getRequest().isReplaceExisting()){

            //b. Call persistence service to fetch existing assignments
            //c. Call the validation model for each assignment
            //d.
        } else {
            //2. Call persistence for newly created records and update relation with request
            //requestEntity = persistInitialRoleAssignments(roleAssignmentRequest);
        }

        //3. Call retrieve Data service to fetch all required IDAM details for Assignee, Assigner & AuthenticatedUserId
        //retrieveDataService.getRoleDetailsForAssignee("UUID");
        //retrieveDataService.getRoleDetailsForAssigner("UUID");
        //retrieveDataService.getRoleDetailsForUser("UUID");

        //4. Call Validation model service to create aggregation objects and apply drools validation rule
        //validationModelService needs to be called here.

        //5. For Each: If success then call persistence service to update assignment record status
        Request request = roleAssignmentRequest.getRequest();
        request.setId(requestEntity.getId());
        String historyId = requestEntity.getHistoryEntities().iterator().next().getRoleAssignmentIdentity().getId().toString();
        insertHistoryWithUpdatedStatus(roleAssignmentRequest, request, Status.APPROVED, UUID.fromString(historyId));

        //5.5 Update Request table with Approved/Rejected status along with role_assignment_id

        //6. once all the assignment records are approved call persistence to update request status
        insertHistoryWithUpdatedStatus(roleAssignmentRequest, request, Status.LIVE, UUID.fromString(historyId));

        //7. Call persistence to move assignment records to Live status
        moveHistoryRecordsToLiveTable(roleAssignmentRequest, requestEntity);

        //8. Call the persistence to copy assignment records to RoleAssignmentLive table
        return  PrepareResponseService.prepareCreateRoleResponse(roleAssignmentRequest);
    }

    private void moveHistoryRecordsToLiveTable(AssignmentRequest roleAssignmentRequest, RequestEntity requestEntity) {
        for (RequestedRole requestedRole : roleAssignmentRequest.getRequestedRoles()) {
            HistoryEntity historyEntity = requestEntity.getHistoryEntities().iterator().next();
            requestedRole.setStatus(Status.LIVE);
            persistenceService.persistRoleAssignment(requestedRole, historyEntity);

        }
    }

    private void insertHistoryWithUpdatedStatus(AssignmentRequest roleAssignmentRequest, Request request, Status status, UUID historyId) {

        for (RequestedRole requestedRole : roleAssignmentRequest.getRequestedRoles()) {
            requestedRole.setStatus(status);
            requestedRole.setId(historyId);
            persistenceService.insertHistoryWithUpdatedStatus(requestedRole, request);
        }
    }

    private RequestEntity persistInitialRequestAndRoleAssignments(AssignmentRequest roleAssignmentRequest) {
        roleAssignmentRequest.getRequest().setStatus(Status.CREATED);
        for (RequestedRole requestedRole : roleAssignmentRequest.getRequestedRoles()) {
            requestedRole.setStatus(Status.CREATED);
        }
        return persistenceService.persistRequest(roleAssignmentRequest);
    }

    public void addExistingRoleAssignments(AssignmentRequest assignmentRequest, List<Object> facts) throws Exception {
        Set<UUID> actorIds = new HashSet<>();
        actorIds.add(assignmentRequest.getRequest().assignerId);
        actorIds.add(assignmentRequest.getRequest().authenticatedUserId);
        for (RequestedRole requestedRole : assignmentRequest.getRequestedRoles()) {
            actorIds.add(requestedRole.actorId);
        }
        for (UUID actorId : actorIds) {
            facts.addAll(retrieveDataService.getRoleAssignmentsForActor(actorId.toString()));
            facts.addAll(idamService.getIdamRoleAssignmentsForActor(actorId.toString()));
        }
    }

    public void updateRequestStatus(AssignmentRequest assignmentRequest) {
        assignmentRequest.getRequest().status = Status.APPROVED;
        for (RequestedRole requestedRole : assignmentRequest.getRequestedRoles()) {
            if (!requestedRole.isApproved()) {
                assignmentRequest.getRequest().status = Status.REJECTED;
            }
        }
    }
}
