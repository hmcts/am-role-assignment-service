package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import org.kie.api.runtime.StatelessKieSession;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.RequestedRole;
import uk.gov.hmcts.reform.roleassignment.domain.model.Role;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Classification;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.domain.service.security.IdamRoleService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class ValidationModelService {
    //1. retrieve existingRoleAssignment records for Assignee
    //2. retrieve existingRoleAssignment records for Requester
    //3. retrieve AuthorisedRoleAssignment records for Requester/Assignee(??)
    //Note: These are aggregation records from Assignment_history table.

    private StatelessKieSession kieSession;
    private PersistenceService persistenceService;
    private IdamRoleService idamRoleService;

    public ValidationModelService(StatelessKieSession kieSession, PersistenceService persistenceService, IdamRoleService idamRoleService) {
        this.kieSession = kieSession;
        this.persistenceService = persistenceService;
        this.idamRoleService = idamRoleService;
    }

    public void validateRequest(AssignmentRequest assignmentRequest) throws Exception {

        // Force the status and timestamp on all new request
        assignmentRequest.getRequest().status = Status.CREATED;
        assignmentRequest.getRequest().setCreated(LocalDateTime.now());
        runRulesOnAllRequestedAssignments(assignmentRequest);

    }

    private void runRulesOnAllRequestedAssignments(AssignmentRequest assignmentRequest) throws Exception {
        // Package up the request and the assignments
        List<Object> facts = new ArrayList<>();
        //Pre defined role configuration
        Role role = Role.builder().id("223")
            .name("judge")
            .roleType(RoleType.ORGANISATION)
            .classification(Classification.PUBLIC)
            .build();
        facts.add(role);
        facts.add(assignmentRequest.getRequest());
        facts.addAll(assignmentRequest.getRequestedRoles());
        addExistingRoleAssignments(assignmentRequest, facts);

        // Run the rules
        kieSession.execute(facts);

        //Update status
        updateStatus(assignmentRequest);

    }

    public void addExistingRoleAssignments(AssignmentRequest assignmentRequest, List<Object> facts) throws Exception {
        Set<String> userIds = new HashSet<>();
        userIds.add(String.valueOf(assignmentRequest.getRequest().assignerId));
        userIds.add(String.valueOf(assignmentRequest.getRequest().getAuthenticatedUserId()));
        for (RequestedRole requestedRole : assignmentRequest.getRequestedRoles()) {
            requestedRole.status = Status.CREATED;
            requestedRole.created = LocalDateTime.now();
            requestedRole.beginTime = LocalDateTime.now();
            requestedRole.endTime = LocalDateTime.now();
            userIds.add(String.valueOf(requestedRole.getActorId()));

        }
        for (String actorId : userIds) {
            facts.addAll(persistenceService.getExistingRoleAssignment(UUID.fromString(actorId)));
            facts.addAll(idamRoleService.getIdamRoleAssignmentsForActor(actorId));

        }
    }

    public void updateStatus(AssignmentRequest assignmentRequest) {
        //Only need to update the status here
    }
}
