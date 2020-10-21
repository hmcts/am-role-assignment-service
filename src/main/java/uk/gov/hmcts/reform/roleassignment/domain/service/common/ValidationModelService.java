package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import com.microsoft.applicationinsights.boot.dependencies.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.runtime.StatelessKieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.ExistingRoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.Pattern;
import uk.gov.hmcts.reform.roleassignment.domain.model.QueryRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.Role;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RequestType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignment.util.JacksonUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
    private static final String TRIBUNAL_CASEWORKER = "tribunal-caseworker";

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
        // Force the status and timestamp on all new request
        runRulesOnAllRequestedAssignments(assignmentRequest);
        log.info(
            "Execution time of validateRequest() : {} in milli seconds ",
            (System.currentTimeMillis() - startTime)
        );

    }

    private void runRulesOnAllRequestedAssignments(AssignmentRequest assignmentRequest) {
        long startTime = System.currentTimeMillis();
        logger.info(String.format("runRulesOnAllRequestedAssignments execution started at %s", startTime));

        Set<Object> facts = new HashSet<>();

        if (assignmentRequest.getRequest().getRequestType() == RequestType.CREATE) {
            // Package up the request and the assignments
            //Pre defined role configuration
            List<Role> assignableRoles = JacksonUtils.getConfiguredRoles().get("roles");

            //Filter List<Role> based on incoming role names
            Set<String> requestedRoles = assignmentRequest.getRequestedRoles().stream()
                .map(RoleAssignment::getRoleName)
                .collect(Collectors.toSet());

            List<Role> filteredAssignableRoles = assignableRoles.stream()
                .filter(e -> requestedRoles.contains(e.getName()))
                .collect(Collectors.toList());


            // fetch List<Pattern> from List<Role>
            List<List<Pattern>> patterns = filteredAssignableRoles.stream().map(Role::getPatterns)
                .collect(Collectors.toList());

            List<Pattern> pattern = patterns.stream().flatMap(List::stream).map(element -> element)
                .collect(Collectors.toList());


            //filter List<Pattern> based on incoming roleTypes
            Set<String> roleTypes = assignmentRequest.getRequestedRoles().stream()
                .map(roleAssignment -> roleAssignment.getRoleType().toString())
                .collect(Collectors.toSet());

            List<Pattern> filteredPattern = pattern.stream()
                .filter(p -> roleTypes.contains(p.getData().get("RoleType").get("values").asText()))
                .collect(Collectors.toList());

            facts.addAll(filteredPattern);
            facts.add(assignmentRequest.getRequest());
            facts.addAll(assignmentRequest.getRequestedRoles());

            addExistingOrgRolesForCreateValidation(assignmentRequest, facts);
        } else if (assignmentRequest.getRequest().getRequestType() == RequestType.DELETE) {
            List<RoleAssignment> roleAssignments = assignmentRequest.getRequestedRoles().stream()
                .filter(roleAssignment -> roleAssignment.getRoleType() == RoleType.CASE && roleAssignment.getRoleName()
                    .equals(TRIBUNAL_CASEWORKER)).collect(Collectors.toList());


            if (!roleAssignments.isEmpty()) {
                addExistingOrgRolesForDeleteValidation(assignmentRequest, facts);
            }
            facts.addAll(assignmentRequest.getRequestedRoles());

            facts.add(assignmentRequest.getRequest());
        }


        // Run the rules
        kieSession.execute(facts);
        logger.info(String.format(
            "runRulesOnAllRequestedAssignments execution finished at %s . Time taken = %s milliseconds",
            System.currentTimeMillis(),
            System.currentTimeMillis() - startTime
        ));


    }

    public void addExistingOrgRolesForCreateValidation(AssignmentRequest assignmentRequest, Set<Object> facts) {

        final long startTime = System.currentTimeMillis();

        Set<String> requestActorIds = new HashSet<>();
        Set<String> actorIds = new HashSet<>();


        requestActorIds.add(String.valueOf(assignmentRequest.getRequest().getAuthenticatedUserId()));
        if (!assignmentRequest.getRequest().getAssignerId().equals(
            assignmentRequest.getRequest().getAuthenticatedUserId())) {
            requestActorIds.add(assignmentRequest.getRequest().getAssignerId());
        }

        assignmentRequest.getRequestedRoles().forEach(requestedRole -> {
            if (requestedRole.getRoleType() == RoleType.CASE && requestedRole.getRoleName()
                .equals(TRIBUNAL_CASEWORKER)) {
                actorIds.add(requestedRole.getActorId());

            }
        });

        // to insert the case once roleType = case & roleName = tribunal-caseworker
        Optional<RoleAssignment> roleAssignment = assignmentRequest.getRequestedRoles().stream().findFirst();

        if (roleAssignment.isPresent() && roleAssignment.get().getRoleType() == RoleType.CASE
            && roleAssignment.get().getRoleName()
            .equals(TRIBUNAL_CASEWORKER)) {
            String caseId = roleAssignment.get().getAttributes().get("caseId").asText();
            facts.add(retrieveDataService.getCaseById(caseId));
        }


        fetchExistingOrgRolesByQuery(facts, requestActorIds, actorIds);

        long endTime = System.currentTimeMillis();
        log.info("Execution time of addExistingRecordsByQueryParam() : {} in milli seconds ", (endTime - startTime));

    }

    public void fetchExistingOrgRolesByQuery(Set<Object> facts, Set<String> requestActorIds, Set<String> actorIds) {
        if (!actorIds.isEmpty()) {

            HashMap<String, List<String>> attributes = new HashMap<>();
            attributes.put("jurisdiction", Arrays.asList("IA"));

            QueryRequest queryRequest = QueryRequest.builder()
                .actorId(List.copyOf(Sets.union(actorIds, requestActorIds)))
                .roleName(Arrays.asList("senior-tribunal-caseworker", TRIBUNAL_CASEWORKER))
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
            ExistingRoleAssignment existingRoleAssignment = ExistingRoleAssignment.builder()
                .build();
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

    public void addExistingOrgRolesForDeleteValidation(AssignmentRequest assignmentRequest, Set<Object> facts) {

        Set<String> actorIds = new HashSet<>();
        actorIds.add(assignmentRequest.getRequest().getAuthenticatedUserId());
        if (!assignmentRequest.getRequest().getAssignerId().equals(
            assignmentRequest.getRequest().getAuthenticatedUserId())) {
            actorIds.add(assignmentRequest.getRequest().getAssignerId());
        }
        fetchExistingOrgRolesByQuery(facts, new HashSet<>(), actorIds);
    }


}
