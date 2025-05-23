package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import lombok.extern.slf4j.Slf4j;
import org.kie.api.runtime.StatelessKieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;
import uk.gov.hmcts.reform.roleassignment.config.DBFlagConfigurtion;
import uk.gov.hmcts.reform.roleassignment.config.EnvironmentConfiguration;
import uk.gov.hmcts.reform.roleassignment.domain.model.Assignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.FeatureFlag;
import uk.gov.hmcts.reform.roleassignment.domain.model.QueryRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleConfig;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.FeatureFlagEnum;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RequestType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequestScope
public class ValidationModelService {

    private final StatelessKieSession kieSession;
    private final RetrieveDataService retrieveDataService;
    private final PersistenceService persistenceService;
    private final EnvironmentConfiguration environmentConfiguration;

    @Value("${roleassignment.query.sizeinternal}")
    private int sizeInternal;

    @Value("${roleassignment.query.sortcolumnunique}")
    private String sortColumnUnique;

    @Autowired
    public ValidationModelService(StatelessKieSession kieSession,
                                  RetrieveDataService retrieveDataService,
                                  PersistenceService persistenceService,
                                  EnvironmentConfiguration environmentConfiguration) {
        this.kieSession = kieSession;
        this.retrieveDataService = retrieveDataService;
        this.persistenceService = persistenceService;
        this.environmentConfiguration = environmentConfiguration;
    }

    public void validateRequest(AssignmentRequest assignmentRequest) {

        runRulesOnAllRequestedAssignments(assignmentRequest);

    }


    /**
     * Get the existing role assignments for the assigner and authenticated user, as well as for all
     * assignees when the request is a create request (but not for deletes).
     */
    private List<? extends Assignment> getExistingRoleAssignmentsForRequest(AssignmentRequest assignmentRequest) {
        // facts must contain existing role assignments for assigner and authenticatedUser,
        // if these are present in the request.
        Set<String> userIds = new HashSet<>();
        var assignerId = assignmentRequest.getRequest().getAssignerId();
        if (assignerId != null) {
            userIds.add(assignmentRequest.getRequest().getAssignerId());
        }
        var authenticatedUserId = assignmentRequest.getRequest().getAuthenticatedUserId();
        if (authenticatedUserId != null) {
            userIds.add(assignmentRequest.getRequest().getAuthenticatedUserId());
        }
        // facts needs assignee roles for creates, not for deletes (?)
        if (assignmentRequest.getRequest().getRequestType() == RequestType.CREATE) {
            assignmentRequest.getRequestedRoles().forEach(r -> userIds.add(r.getActorId()));
        }

        //replacing the logic to make single db call using dynamic search api.
        return getCurrentRoleAssignmentsForActors(userIds);

    }


    public List<Assignment> getCurrentRoleAssignmentsForActors(Set<String> actorIds) {
        var queryRequest = QueryRequest.builder()
            .actorId(actorIds)
            .roleType(List.of(RoleType.ORGANISATION.name(), RoleType.CASE.name()))
            .grantType(List.of(GrantType.STANDARD.name(), GrantType.BASIC.name()))
            .validAt(LocalDateTime.now())
            .build();

        List<List<Assignment>> assignmentRecords =  new ArrayList<>();

        assignmentRecords.add(persistenceService.retrieveRoleAssignmentsByQueryRequest(
            queryRequest,
            0,
            sizeInternal,
            sortColumnUnique,
            null,
            true));

        var totalRecords = persistenceService.getTotalRecords();
        if (totalRecords > 100) {
            log.warn("Fetched assignments for the actor have {} total records", totalRecords);
        }
        double pageNumber = 0;
        if (sizeInternal > 0) {
            pageNumber = (double) totalRecords / (double) sizeInternal;
        }

        for (var page = 1; page < pageNumber; page++) {
            assignmentRecords.add(persistenceService.retrieveRoleAssignmentsByQueryRequest(
                queryRequest,
                page,
                sizeInternal,
                sortColumnUnique,
                null,
                true));

        }
        return  assignmentRecords.stream().flatMap(Collection::stream).toList();

    }

    private void runRulesOnAllRequestedAssignments(AssignmentRequest assignmentRequest) {


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
        // building the Feature Flag
        if (environmentConfiguration.getEnvironment().equals("prod")) {
            droolFlagStates = DBFlagConfigurtion.getDroolFlagStates();
        } else {
            // fetch the latest value from db for lower env
            getFlagValuesFromDB(droolFlagStates);
        }

        for (Map.Entry<String, Boolean> flag : droolFlagStates.entrySet()) {
            var featureFlag = FeatureFlag.builder()
                .flagName(flag.getKey())
                .status(flag.getValue())
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



    }

    /**
     * This utility method is used to capture the log in drools and log at DEBUG level.
     */
    public static void logMsg(final String message) {
        log.debug(message);
    }

    /**
     * This utility method is used to capture the log in drools and log at INFO level.
     */
    public static void logInfoMsg(final String message) {
        log.info(message);
    }

    private void getFlagValuesFromDB(Map<String, Boolean> droolFlagStates) {
        for (FeatureFlagEnum featureFlagEnum : FeatureFlagEnum.values()) {
            Boolean status = persistenceService.getStatusByParam(featureFlagEnum.getValue(),
                                                                 environmentConfiguration.getEnvironment());
            droolFlagStates.put(featureFlagEnum.getValue(), status);
        }
    }

}
