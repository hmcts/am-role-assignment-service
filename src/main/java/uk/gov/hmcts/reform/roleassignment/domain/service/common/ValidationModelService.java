package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import com.microsoft.applicationinsights.boot.dependencies.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.runtime.StatelessKieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.ExistingRoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.QueryRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.Role;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RequestType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignment.domain.service.security.IdamRoleService;
import uk.gov.hmcts.reform.roleassignment.util.JacksonUtils;
import uk.gov.hmcts.reform.roleassignment.util.SecurityUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.apache.commons.beanutils.BeanUtils.copyProperties;

@Service
@Slf4j
public class ValidationModelService {
    //1. retrieve existingRoleAssignment records for Assignee
    //2. retrieve existingRoleAssignment records for Requester
    //3. retrieve AuthorisedRoleAssignment records for Requester/Assignee(??)
    //Note: These are aggregation records from Assignment_history table.
    private static final Logger logger = LoggerFactory.getLogger(ValidationModelService.class);
    private StatelessKieSession kieSession;
    private IdamRoleService idamRoleService;
    private RetrieveDataService retrieveDataService;
    private SecurityUtils securityUtils;
    private PersistenceService persistenceService;


    public ValidationModelService(StatelessKieSession kieSession,
                                  IdamRoleService idamRoleService,
                                  RetrieveDataService retrieveDataService,
                                  SecurityUtils securityUtils,
                                  PersistenceService persistenceService) {
        this.kieSession = kieSession;
        this.idamRoleService = idamRoleService;
        this.retrieveDataService = retrieveDataService;
        this.securityUtils = securityUtils;
        this.persistenceService = persistenceService;

    }

    public void validateRequest(AssignmentRequest assignmentRequest) {
        long startTime = System.currentTimeMillis();
        // Force the status and timestamp on all new request
        runRulesOnAllRequestedAssignments(assignmentRequest);
        long endTime = System.currentTimeMillis();
        log.info("Execution time of validateRequest() : {} in milli seconds ", (endTime - startTime));

    }

    private void runRulesOnAllRequestedAssignments(AssignmentRequest assignmentRequest) {
        long startTime = System.currentTimeMillis();
        logger.info(String.format("runRulesOnAllRequestedAssignments execution started at %s", startTime));

        // Package up the request and the assignments
        //Pre defined role configuration
        List<Role> role = JacksonUtils.getConfiguredRoles().get("roles");
        Set<Object> facts = new HashSet<>(role);
        facts.add(assignmentRequest.getRequest());
        facts.addAll(assignmentRequest.getRequestedRoles());
        if (assignmentRequest.getRequest().getRequestType() == RequestType.CREATE) {

            addExistingRecordsByQueryParam(assignmentRequest, facts);
            kieSession.setGlobal("retrieveDataService", retrieveDataService);
        }



        // Run the rules
        kieSession.execute(facts);
        logger.info(String.format(
            "runRulesOnAllRequestedAssignments execution finished at %s . Time taken = %s milliseconds",
            System.currentTimeMillis(),
            System.currentTimeMillis() - startTime
        ));


    }

    public void addExistingRecordsByQueryParam(AssignmentRequest assignmentRequest, Set<Object> facts) {

        final long startTime = System.currentTimeMillis();

        Set<String> requestActorIds = new HashSet<>();
        Set<String> actorIds = new HashSet<>();


        requestActorIds.add(String.valueOf(assignmentRequest.getRequest().getAssignerId()));
        if (!assignmentRequest.getRequest().getAssignerId().equals(
            assignmentRequest.getRequest().getAuthenticatedUserId())) {
            requestActorIds.add(assignmentRequest.getRequest().getAuthenticatedUserId());
        }

        assignmentRequest.getRequestedRoles().forEach(requestedRole -> {
            if (requestedRole.getRoleType() == RoleType.CASE && requestedRole.getRoleName()
                .equals("tribunal-caseworker")) {
                actorIds.add(requestedRole.getActorId());
            }
        });

        executeQueryParamForCaseRole(facts, requestActorIds, actorIds);

        long endTime = System.currentTimeMillis();
        log.info("Execution time of addExistingRecordsByQueryParam() : {} in milli seconds ", (endTime - startTime));

    }

    private void executeQueryParamForCaseRole(Set<Object> facts, Set<String> requestActorIds, Set<String> actorIds) {
        if (!actorIds.isEmpty()) {

            HashMap<String, List<String>> attributes = new HashMap<>();
            attributes.put("jurisdiction", Arrays.asList("IA"));

            QueryRequest queryRequest = QueryRequest.builder()
                .actorId(List.copyOf(Sets.union(actorIds, requestActorIds)))
                .roleName(Arrays.asList("senior-tribunal-caseworker", "tribunal-caseworker"))
                .roleType(Arrays.asList("ORGANISATION"))
                .attributes(attributes)
                .build();

            List<RoleAssignment> roleAssignments = persistenceService.retrieveRoleAssignmentsByQueryRequest(
                queryRequest,
                0,
                0,
                null,
                null
            );
            List<ExistingRoleAssignment> existingRecords = convertRoleAssignmentIntoExistingRecords(roleAssignments);
            facts.addAll(existingRecords);
        }
    }

    private List<ExistingRoleAssignment> convertRoleAssignmentIntoExistingRecords(
        List<RoleAssignment> roleAssignments) {
        List<ExistingRoleAssignment> existingRecords = new ArrayList<>();

        for (RoleAssignment element : roleAssignments) {
            ExistingRoleAssignment existingRoleAssignment = ExistingRoleAssignment.builder().build();
            try {
                copyProperties(existingRoleAssignment, element);
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

    public void addExistingRoleAssignments(AssignmentRequest assignmentRequest, Set<Object> facts) {
        facts.add(securityUtils.getUserRoles());
        Set<String> userIds = new HashSet<>();
        if (!assignmentRequest.getRequest().getAssignerId().equals(
            assignmentRequest.getRequest().getAuthenticatedUserId())) {
            userIds.add(String.valueOf(assignmentRequest.getRequest().getAssignerId()));
        }
        assignmentRequest.getRequestedRoles().forEach(requestedRole ->
                                                          userIds.add(String.valueOf(requestedRole.getActorId()))
        );
        userIds.forEach(userId -> {
            if (userId != null) {
                log.info("Getting user Roles");
                facts.add(idamRoleService.getUserRoles(userId));
            }
        });

    }


}
