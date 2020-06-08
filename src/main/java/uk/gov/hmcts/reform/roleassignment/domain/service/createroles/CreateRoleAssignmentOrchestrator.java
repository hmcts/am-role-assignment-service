package uk.gov.hmcts.reform.roleassignment.domain.service.createroles;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignment.data.casedata.DefaultCaseDataRepository;
import uk.gov.hmcts.reform.roleassignment.domain.model.RequestedRole;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ParseRequestService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PrepareResponseService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.RetrieveDataService;
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
    //private ValidationModelService validationModelService;

    public CreateRoleAssignmentOrchestrator(DefaultCaseDataRepository caseService, IdamRoleService idamService,
                                            RetrieveDataService retrieveDataService, ParseRequestService parseRequestService,
                                            PersistenceService persistenceService) {
        this.caseService = caseService;
        this.idamService = idamService;
        this.retrieveDataService = retrieveDataService;
        this.parseRequestService = parseRequestService;
        this.persistenceService = persistenceService;
        //this.validationModelService = validationModelService;
    }

    public ResponseEntity<Object> createRoleAssignment(RoleAssignmentRequest roleAssignmentRequest) {
        //1. call parse request service
        //parseRequestService.parseRequest(roleAssignmentRequest);
        //2. Call persistence service to store the created records
        //persistenceService.persistRequestAndRequestedRoles(roleAssignmentRequest);
        //3. Call retrieve Data service to fetch all required objects
        //retrieveDataService.getRoleAssignmentsForActor("actorId");
        //4. Call Validation model service to create aggregation objects and apply drools validation rule
        //validationModelService needs to be written.

        //5. For Each: If success then call persistence service to update assignment record status

        //6. once all the assignment records are approved call persistence to update request status
        //7. Call persistence to move assignment records to Live status
        //8. Call the persistence to copy assignment records to RoleAssignmentLive table

        for (RequestedRole requestedRole : roleAssignmentRequest.requestedRoles) {
            requestedRole.setStatus(Status.APPROVED);
        }

        //temporary
        setRoleAssignmentIDs(roleAssignmentRequest);

        updateRequestStatus(roleAssignmentRequest);
        ResponseEntity<Object> response =  PrepareResponseService.prepareCreateRoleResponse(roleAssignmentRequest);
        return response;
    }

    private void setRoleAssignmentIDs(RoleAssignmentRequest roleAssignmentRequest) {
        roleAssignmentRequest.getRoleRequest().setId(UUID.fromString("21334a2b-79ce-44eb-9168-2d49a744be9c"));
        roleAssignmentRequest.getRequestedRoles().forEach(roles -> roles.setId(UUID.fromString(
            "21334a2b-79ce-44eb-9168-2d49a744be9a")));
    }


    public void addExistingRoleAssignments(RoleAssignmentRequest roleAssignmentRequest, List<Object> facts) throws Exception {
        Set<String> actorIds = new HashSet<>();
        actorIds.add(roleAssignmentRequest.roleRequest.requestorId);
        actorIds.add(roleAssignmentRequest.roleRequest.authenticatedUserId);
        for (RequestedRole requestedRole : roleAssignmentRequest.requestedRoles) {
            actorIds.add(requestedRole.actorId.toString());
        }
        for (String actorId : actorIds) {
            facts.addAll(retrieveDataService.getRoleAssignmentsForActor(actorId));
            facts.addAll(idamService.getIdamRoleAssignmentsForActor(actorId));
        }
    }

    public void updateRequestStatus(RoleAssignmentRequest roleAssignmentRequest) {
        roleAssignmentRequest.roleRequest.status = Status.APPROVED;
        for (RequestedRole requestedRole : roleAssignmentRequest.requestedRoles) {
            if (!requestedRole.isApproved()) {
                roleAssignmentRequest.roleRequest.status = Status.REJECTED;
            }
        }
    }

}
