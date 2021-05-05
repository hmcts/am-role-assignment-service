package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import lombok.extern.slf4j.Slf4j;
import org.kie.api.runtime.StatelessKieSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignment.config.DBFlagConfigurtion;
import uk.gov.hmcts.reform.roleassignment.domain.model.Assignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.FeatureFlag;
import uk.gov.hmcts.reform.roleassignment.domain.model.QueryRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleConfig;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.FeatureFlagEnum;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RequestType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class ValidationModelService {

    private StatelessKieSession kieSession;
    private RetrieveDataService retrieveDataService;
    private PersistenceService persistenceService;

    @Value("${launchdarkly.sdk.environment}")
    private String environment;


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
            Math.subtractExact(System.currentTimeMillis(), startTime)
        );

    }


    /**
     * Get the existing role assignments for the assigner and authenticated user, as well as for all
     * assignees when the request is a create request (but not for deletes).
     */
    private List<? extends Assignment> getExistingRoleAssignmentsForRequest(AssignmentRequest assignmentRequest) {
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


    public List<Assignment> getCurrentRoleAssignmentsForActors(Set<String> actorIds) {
        QueryRequest queryRequest = QueryRequest.builder()
            .actorId(actorIds)
            .roleType("ORGANISATION")
            .validAt(LocalDateTime.now())
            .build();

        return persistenceService.retrieveRoleAssignmentsByQueryRequest(
            queryRequest,
            0,
            0,
            null,
            null,
            true

        );


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

        // adding the latest flag instance into the kie session

        List<FeatureFlag> featureFlags = new ArrayList<>();

        Map<String, Boolean> droolFlagStates = new ConcurrentHashMap<>();
        // building the LDFeature Flag
        if (environment.equals("prod")) {
            droolFlagStates = DBFlagConfigurtion.getDroolFlagStates();
        } else {
            // fetch the latest value from db for lower env
            getFlagValuesFromDB(droolFlagStates);
        }

        for (String flag : droolFlagStates.keySet()) {
            FeatureFlag featureFlag = FeatureFlag.builder()
                .flagName(flag)
                .status(droolFlagStates.get(flag))
                .build();
            featureFlags.add(featureFlag);
        }
        facts.addAll(featureFlags);
        // facts must contain existing role assignments for assigner and authenticatedUser,
        // and assignee for create requests
        facts.addAll(getExistingRoleAssignmentsForRequest(assignmentRequest));


        // Make the retrieve data service available to rules - this allows data - e.g. case data - to be
        // loaded dynamically when needed by a rule, rather than up-front, when it may never be used.
        kieSession.setGlobal("DATA_SERVICE", retrieveDataService);

        // Run the rules
        kieSession.execute(facts);
        log.info(String.format(
            " >> runRulesOnAllRequestedAssignments execution finished at %s . Time taken = %s milliseconds",
            System.currentTimeMillis(),
            Math.subtractExact(System.currentTimeMillis(), startTime)
        ));


    }

    private void getFlagValuesFromDB(Map<String, Boolean> droolFlagStates) {
        for (FeatureFlagEnum featureFlagEnum : FeatureFlagEnum.values()) {
            Boolean status = persistenceService.getStatusByParam(featureFlagEnum.getValue(), environment);
            droolFlagStates.put(featureFlagEnum.getValue(), status);


        }
    }


}
