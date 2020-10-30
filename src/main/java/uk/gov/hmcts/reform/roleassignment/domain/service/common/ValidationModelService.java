package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import lombok.extern.slf4j.Slf4j;
import org.kie.api.runtime.StatelessKieSession;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.ExistingRoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.QueryRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleConfig;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RequestType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.beanutils.BeanUtils.copyProperties;

@Service
@Slf4j
public class ValidationModelService {

    private StatelessKieSession kieSession;
    private RetrieveDataService retrieveDataService;
    private PersistenceService persistenceService;


    public ValidationModelService(StatelessKieSession kieSession,
                                  RetrieveDataService retrieveDataService,
                                  PersistenceService persistenceService) {
        this.kieSession = kieSession;

        this.retrieveDataService = retrieveDataService;

        this.persistenceService = persistenceService;

    }

    public void validateRequest(AssignmentRequest assignmentRequest) {
        long startTime = System.currentTimeMillis();

        runRulesOnAllRequestedAssignments(assignmentRequest);
        log.info(
            "Execution time of validateRequest() : {} in milli seconds ",
            (System.currentTimeMillis() - startTime)
        );

    }



    /**
     * Get the existing role assignments for the assigner and authenticated user, as well as for all
     * assignees when the request is a create request (but not for deletes).
     */
    private List<ExistingRoleAssignment> getExistingRoleAssignmentsForRequest(AssignmentRequest assignmentRequest) {
        // facts must contain existing role assignments for assigner and authenticatedUser,
        // if these are present in the request.
        Set<String> userIds = new HashSet<>();
        String assignerId = assignmentRequest.getRequest().getAssignerId();
        if (assignerId != null) {
            userIds.add(assignmentRequest.getRequest().getAssignerId());
        }
        String authenticatedUserId = assignmentRequest.getRequest().getAuthenticatedUserId();
        if (authenticatedUserId != null) {
            userIds.add(assignmentRequest.getRequest().getAuthenticatedUserId());
        }
        // facts needs assignee roles for creates, not for deletes (?)
        if (assignmentRequest.getRequest().getRequestType() == RequestType.CREATE) {
            assignmentRequest.getRequestedRoles().stream().forEach(r -> userIds.add(r.getActorId()));
        }

        //replacing the logic to make single db call using dynamic search api.
        return getCurrentRoleAssignmentsForActors(userIds);

    }


    public List<ExistingRoleAssignment> getCurrentRoleAssignmentsForActors(Set<String> actorIds) {
        LocalDateTime now = LocalDateTime.now();
        QueryRequest queryRequest = QueryRequest.builder()
            .actorId(actorIds)
            .roleType("ORGANISATION")
            .validAt(now)
            .build();

        List<ExistingRoleAssignment> roleAssignments = persistenceService.retrieveRoleAssignmentsByQueryRequest(
            queryRequest,
            0,
            0,
            null,
            null
        );

        return roleAssignments;


        //return convertRoleAssignmentIntoExistingRecords(roleAssignments);
    }

    private void runRulesOnAllRequestedAssignments(AssignmentRequest assignmentRequest) {
        long startTime = System.currentTimeMillis();
        log.info(String.format("runRulesOnAllRequestedAssignments execution started at %s", startTime));


        Set<Object> facts = new HashSet<>();

        // facts must contain the request
        facts.add(assignmentRequest.getRequest());
        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());
        // facts must contain the role config, for access to the patterns
        facts.add(RoleConfig.getRoleConfig());
        // facts must contain existing role assignments for assigner and authenticatedUser, and assignee for create requests
        facts.addAll(getExistingRoleAssignmentsForRequest(assignmentRequest));

        // Make the retrieve data service available to rules - this allows data - e.g. case data - to be
        // loaded dynamically when needed by a rule, rather than up-front, when it may never be used.
        kieSession.setGlobal("DATA_SERVICE", retrieveDataService);

        // Run the rules
        kieSession.execute(facts);
        log.info(String.format(
            "runRulesOnAllRequestedAssignments execution finished at %s . Time taken = %s milliseconds",
            System.currentTimeMillis(),
            System.currentTimeMillis() - startTime
        ));


    }

    /*
     * TODO: Not ideal from a performance point of view to copy every role assignment
     *       into a shadow class.  The ExistingRoleAssignment class is useful to make
     *       rules natural.  Can we not use generics somewhere to return the right
     *       class from the persistence service?
     */
    private List<ExistingRoleAssignment> convertRoleAssignmentIntoExistingRecords(
        List<RoleAssignment> roleAssignments) {
        List<ExistingRoleAssignment> existingRecords = new ArrayList<>();

        for (RoleAssignment roleAssignment : roleAssignments) {
            ExistingRoleAssignment existingRoleAssignment = ExistingRoleAssignment.builder()
                .build();
            try {
                copyProperties(existingRoleAssignment, roleAssignment);
                existingRecords.add(existingRoleAssignment);
            } catch (Exception e) {
                log.error(
                    "Exception was thrown when copy the role assignment records into existing records ",
                    e.getMessage()
                );
            }
        }
        return existingRecords;
    }


}
