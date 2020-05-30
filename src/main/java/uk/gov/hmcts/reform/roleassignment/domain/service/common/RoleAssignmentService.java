package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignment.data.casedata.DefaultCaseDataRepository;
import uk.gov.hmcts.reform.roleassignment.domain.model.RequestedRole;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class RoleAssignmentService {


    private DefaultCaseDataRepository caseService;
    private IdamRoleService idamService;
    private RetrieveSupportingDataService retrieveSupportingDataService;


    public RoleAssignmentService(DefaultCaseDataRepository caseService, IdamRoleService idamService,
                                 RetrieveSupportingDataService retrieveSupportingDataService) {
        this.caseService = caseService;
        this.idamService = idamService;
        this.retrieveSupportingDataService = retrieveSupportingDataService;
    }


    public void addExistingRoleAssignments(RoleAssignmentRequest roleAssignmentRequest, List<Object> facts) throws Exception {
        Set<String> actorIds = new HashSet<>();
        actorIds.add(roleAssignmentRequest.roleRequest.requestorId);
        actorIds.add(roleAssignmentRequest.roleRequest.authenticatedUserId);
        for (RequestedRole requestedRole : roleAssignmentRequest.requestedRoles) {
            actorIds.add(requestedRole.actorId);
        }
        for (String actorId : actorIds) {
            facts.addAll(retrieveSupportingDataService.getRoleAssignmentsForActor(actorId));
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
