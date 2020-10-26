package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import static org.apache.commons.beanutils.BeanUtils.copyProperties;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.kie.api.KieServices;
import org.kie.api.runtime.StatelessKieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JavaType;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.ExistingRoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleConfig;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RequestType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.stubs.StubPersistenceService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.stubs.StubRetrieveDataService;
import uk.gov.hmcts.reform.roleassignment.util.JacksonUtils;

@Service
@Slf4j
public class ValidationModelService {
    //1. retrieve existingRoleAssignment records for Assignee
    //2. retrieve existingRoleAssignment records for Requester
    //3. retrieve AuthorisedRoleAssignment records for Requester/Assignee(??)
    //Note: These are aggregation records from Assignment_history table.
    private static final Logger logger = LoggerFactory.getLogger(ValidationModelService.class);
    private StatelessKieSession kieSession;

    private StubRetrieveDataService retrieveDataService;

    private StubPersistenceService persistenceService;


    public ValidationModelService(StatelessKieSession kieSession,
                                  StubRetrieveDataService retrieveDataService,
                                  StubPersistenceService persistenceService) {
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

    /**
     * May not need to filter the assignments, but not clear whether getAssignmentsByActor does this already.
     * TODO: this is only protected to allow temporary overriding for testing purposes.
     */
    private Collection<RoleAssignment> getCurrentRoleAssignmentsForActor(String actorId) {
    	LocalDateTime now = LocalDateTime.now();
    	return
	    	persistenceService.getAssignmentsByActor(actorId).stream().filter(
	    			ra -> {
	    				LocalDateTime begin = ra.getBeginTime();
	    				LocalDateTime end = ra.getEndTime();
	    				return (begin == null || !begin.isBefore(now)) && (end == null || end.isAfter(now));
	    			})
	    	.collect(Collectors.toList());
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
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        for (String userId : userIds) {
        	roleAssignments.addAll(getCurrentRoleAssignmentsForActor(userId));
        }
        return convertRoleAssignmentIntoExistingRecords(roleAssignments);
    }

private void setRoleAssignmentsStatusToCreated(AssignmentRequest assignmentRequest) {
	assignmentRequest.getRequestedRoles().forEach(r -> r.setStatus(Status.CREATED));
}

    private void runRulesOnAllRequestedAssignments(AssignmentRequest assignmentRequest) {
        long startTime = System.currentTimeMillis();
        logger.info(String.format("runRulesOnAllRequestedAssignments execution started at %s", startTime));

        // Set all the role assignments to CREATED initially.
        setRoleAssignmentsStatusToCreated(assignmentRequest);

        Set<Object> facts = new HashSet<>();

        // facts must contain the request
        facts.add(assignmentRequest.getRequest());
        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());
        // facts must contain the role config, for access to the patterns
        facts.add(RoleConfig.getRoleConfig());
        // facts must conatin existing role assignments for assigner and authenticatedUser, and assignee for create requests
        facts.addAll(getExistingRoleAssignmentsForRequest(assignmentRequest));

        // Make the retrieve data service available to rules - this allows data - e.g. case data - to be
        // loaded dynamically when needed by a rule, rather than up-front, when it may never be used.
        kieSession.setGlobal("DATA_SERVICE", retrieveDataService);

        // Run the rules
        kieSession.execute(facts);
        logger.info(String.format(
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









    public static void main(String[] args) throws Exception {
    	ValidationModelService v = new ValidationModelService(kieSession(), new StubRetrieveDataService(), new StubPersistenceService());
    	List<AssignmentRequest> requests = loadRequests();
    	for (AssignmentRequest request : requests) {
        	v.validateRequest(request);
        	System.out.println("-----------------------------------------------------------------------");
        	System.out.println("Request " + request.getRequest().getCorrelationId());
        	System.out.println("Request " + request.getRequest().getStatus());
    	}
    }

    public static StatelessKieSession kieSession() {
        return KieServices.Factory.get().getKieClasspathContainer().newStatelessKieSession("role-assignment-validation-session");
    }

	/**
	 * Load case data from the "cases.json" resource.
	 */
	private static List<AssignmentRequest> loadRequests() {
		try {
			try (InputStream input = ValidationModelService.class.getResourceAsStream("assignment-requests.json")) {
				JavaType type = JacksonUtils.MAPPER.getTypeFactory().constructCollectionType(List.class, AssignmentRequest.class);
				List<AssignmentRequest> requests = JacksonUtils.MAPPER.readValue(input, type);
				return requests;
			}
		} catch (Throwable t) {
			throw new RuntimeException("Failed to load stub case data.", t);
		}
	}

}
