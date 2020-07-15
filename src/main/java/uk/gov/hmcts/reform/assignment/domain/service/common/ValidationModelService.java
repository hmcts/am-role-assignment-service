package uk.gov.hmcts.reform.assignment.domain.service.common;

import org.kie.api.runtime.StatelessKieSession;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.assignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.assignment.domain.model.Role;
import uk.gov.hmcts.reform.assignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.assignment.domain.service.security.IdamRoleService;
import uk.gov.hmcts.reform.assignment.util.JacksonUtils;

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

    public ValidationModelService(StatelessKieSession kieSession,
                                  IdamRoleService idamRoleService,
                                  RetrieveDataService retrieveDataService) {
        this.kieSession = kieSession;
        this.idamRoleService = idamRoleService;
        this.retrieveDataService = retrieveDataService;
    }

    public void validateRequest(AssignmentRequest assignmentRequest) throws Exception {

        // Force the status and timestamp on all new request
        runRulesOnAllRequestedAssignments(assignmentRequest);

    }

    private void runRulesOnAllRequestedAssignments(AssignmentRequest assignmentRequest) {
        // Package up the request and the assignments
        List<Object> facts = new ArrayList<>();
        //Pre defined role configuration
        List<Role> role =  JacksonUtils.getConfiguredRoles().get("roles");
        facts.addAll(role);
        facts.add(assignmentRequest.getRequest());
        facts.addAll(assignmentRequest.getRequestedRoles());
        addExistingRoleAssignments(assignmentRequest, facts);
        kieSession.setGlobal("retrieveDataService", retrieveDataService);

        // Run the rules
        kieSession.execute(facts);


    }

    public void addExistingRoleAssignments(AssignmentRequest assignmentRequest, List<Object> facts) {
        Set<String> userIds = new HashSet<>();
        userIds.add(String.valueOf(assignmentRequest.getRequest().getAssignerId()));
        userIds.add(String.valueOf(assignmentRequest.getRequest().getAuthenticatedUserId()));
        for (RoleAssignment requestedRole : assignmentRequest.getRequestedRoles()) {
            userIds.add(String.valueOf(requestedRole.getActorId()));

        }
        for (String actorId : userIds) {
            if (actorId != null) {
                facts.addAll(idamRoleService.getIdamRoleAssignmentsForActor(actorId));
            }

        }
    }


}
