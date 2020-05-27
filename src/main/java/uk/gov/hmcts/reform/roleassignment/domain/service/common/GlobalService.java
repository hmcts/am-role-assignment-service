package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignment.data.casedata.DefaultCaseDataRepository;
import uk.gov.hmcts.reform.roleassignment.domain.model.Request;
import uk.gov.hmcts.reform.roleassignment.domain.model.RequestedRole;
import uk.gov.hmcts.reform.roleassignment.domain.model.ValidationModel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class GlobalService {


    private DefaultCaseDataRepository caseService;
    private IdamRoleService idamService;
    private RoleAssignmentService roleAssignmentService;


    public GlobalService(DefaultCaseDataRepository caseService, IdamRoleService idamService, RoleAssignmentService roleAssignmentService) {
        this.caseService = caseService;
        this.idamService = idamService;
        this.roleAssignmentService = roleAssignmentService;
    }



    public  void addExistingRoleAssignments(ValidationModel validationModel, List<Object> facts) throws Exception
    {
        Set<String> actorIds = new HashSet<>();
        actorIds.add(validationModel.request.requestorId);
        actorIds.add(validationModel.request.authenticatedUserId);
        for (RequestedRole requestedRole : validationModel.assignmentsRequested)
        {
            actorIds.add(requestedRole.actorId);
        }
        for (String actorId : actorIds)
        {
            facts.addAll(roleAssignmentService.getRoleAssignmentsForActor(actorId));
            facts.addAll(idamService.getIdamRoleAssignmentsForActor(actorId));
        }
    }

    public  void updateRequestStatus(ValidationModel validationModel)
    {
        validationModel.request.status = Request.Status.APPROVED;
        for (RequestedRole requestedRole : validationModel.assignmentsRequested)
        {
            if (!requestedRole.isApproved())
            {
                validationModel.request.status = Request.Status.REJECTED;
            }
        }
    }

}
