package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import org.kie.api.runtime.StatelessKieSession;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.Request;
import uk.gov.hmcts.reform.roleassignment.domain.model.Role;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.UserRoles;
import uk.gov.hmcts.reform.roleassignment.domain.service.security.IdamRoleService;
import uk.gov.hmcts.reform.roleassignment.util.JacksonUtils;
import uk.gov.hmcts.reform.roleassignment.util.SecurityUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ValidationModelService {
    //1. retrieve existingRoleAssignment records for Assignee
    //2. retrieve existingRoleAssignment records for Requester
    //3. retrieve AuthorisedRoleAssignment records for Requester/Assignee(??)
    //Note: These are aggregation records from Assignment_history table.

    private StatelessKieSession kieSession;
    private IdamRoleService idamRoleService;
    private RetrieveDataService retrieveDataService;
    private SecurityUtils securityUtils;

    public ValidationModelService(StatelessKieSession kieSession,
                                  IdamRoleService idamRoleService,
                                  RetrieveDataService retrieveDataService, SecurityUtils securityUtils) {
        this.kieSession = kieSession;
        this.idamRoleService = idamRoleService;
        this.retrieveDataService = retrieveDataService;
        this.securityUtils = securityUtils;
    }

    public void validateRequest(AssignmentRequest assignmentRequest) {

        // Force the status and timestamp on all new request
        runRulesOnAllRequestedAssignments(assignmentRequest);

    }

    private void runRulesOnAllRequestedAssignments(AssignmentRequest assignmentRequest) {
        // Package up the request and the assignments
        List<Object> facts = new ArrayList<>();
        //Pre defined role configuration
        List<Role> role = JacksonUtils.getConfiguredRoles().get("roles");
        facts.addAll(role);
        facts.add(assignmentRequest.getRequest());
        facts.addAll(assignmentRequest.getRequestedRoles());
        addExistingRoleAssignments(assignmentRequest, facts);
        kieSession.setGlobal("retrieveDataService", retrieveDataService);

        // Run the rules
        kieSession.execute(facts);


    }

    public void addExistingRoleAssignments(AssignmentRequest assignmentRequest, List<Object> facts) {
        Request request = assignmentRequest.getRequest();
        facts.add(securityUtils.getUserRoles());
        Set<String> userIds = new HashSet<>();
        if (!request.getAssignerId().equals(
            request.getAuthenticatedUserId())) {
            userIds.add(String.valueOf(request.getAssignerId()));
        }
        for (RoleAssignment requestedRole : assignmentRequest.getRequestedRoles()) {
            userIds.add(String.valueOf(requestedRole.getActorId()));
        }
        String userRolesCommaSeparated = String.join(",", userIds);
        Set<UserRoles> userRolesEntities = idamRoleService.getUserRoles(userRolesCommaSeparated);

        for (UserRoles userRoles : userRolesEntities) {
            if (userRoles.getUid() != null) {
                facts.add(userRoles);
            }
        }
    }


}
