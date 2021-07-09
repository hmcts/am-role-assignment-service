package uk.gov.hmcts.reform.roleassignment;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.IgnoreNoPactsToVerify;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.VersionSelector;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.roleassignment.controller.endpoints.QueryAssignmentController;
import uk.gov.hmcts.reform.roleassignment.data.ActorCacheEntity;
import uk.gov.hmcts.reform.roleassignment.domain.model.Assignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.roleassignment.domain.service.queryroles.QueryRoleAssignmentOrchestrator;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;

import java.util.List;

@ExtendWith(SpringExtension.class)
@Provider("am_roleAssignment_queryAssignment")
@PactBroker(scheme = "${PACT_BROKER_SCHEME:http}",
    host = "${PACT_BROKER_URL:localhost}", port = "${PACT_BROKER_PORT:9292}", consumerVersionSelectors = {
    @VersionSelector(tag = "master")})
@TestPropertySource(properties = {"roleassignment.query.size=20"})
@Import(RoleAssignmentProviderTestConfiguration.class)
@IgnoreNoPactsToVerify
public class SearchQueryRoleAssignmentProviderTest {

    private static final String ACTOR_ID = "234873";

    @Autowired
    private PersistenceService persistenceService;

    @Autowired
    private QueryRoleAssignmentOrchestrator queryRoleAssignmentOrchestrator;

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        if (context != null) {
            context.verifyInteraction();
        }
    }

    @BeforeEach
    void before(PactVerificationContext context) {
        MockMvcTestTarget testTarget = new MockMvcTestTarget();
        testTarget.setControllers(new QueryAssignmentController(
            queryRoleAssignmentOrchestrator
        ));
        if (context != null) {
            context.setTarget(testTarget);
        }
    }

    @State({"A list of role assignments for the search query"})
    public void searchQueryByActorIdWithSuccess() throws Exception {
        setInitiMock(false);
    }

    @State({"A list of role assignments for the advanced search query"})
    public void advancedSearchQueryWithSuccess() throws Exception {
        initAdvancedMock();
    }

    @State({"A list of role assignments for the search query by role name"})
    public void searchQueryByRoleNameWithSuccess() throws Exception {
        setInitiMock(false);
    }

    @State({"A list of role assignments for the search query by attributes"})
    public void searchQueryByAttributesWithSuccess() throws Exception {
        setInitiMock(false);
    }

    @State({"A list of multiple role assignments for the search query"})
    public void searchQueryMultipleRoleAssignmentsWithSuccess() throws Exception {
        setInitiMock(true);
    }

    @State({"An empty list of role assignments for the search query"})
    public void searchQueryReturnsEmptyListOfAssignmentsWithSuccess() throws Exception {
        setInitiMock(false);
    }

    private void setInitiMock(boolean hasMultipleAssignments) throws Exception {
        List<Assignment> roleAssignments = (hasMultipleAssignments == true)
            ? TestDataBuilder.buildMultiAssignmentList(Status.LIVE, ACTOR_ID, "attributes_orm_orgrole.json")
            : TestDataBuilder.buildAssignmentList(Status.LIVE, ACTOR_ID, "attributes_orm_orgrole.json");

        when(persistenceService.retrieveRoleAssignmentsByQueryRequest(any(), any(), any(), any(), any(), anyBoolean()))
            .thenReturn(roleAssignments);
        when(persistenceService.getActorCacheEntity(ACTOR_ID)).thenReturn(ActorCacheEntity.builder().actorId(ACTOR_ID)
                                                                              .etag(1L).build());
    }

    private void initAdvancedMock() throws Exception {
        List<Assignment> roleAssignments =
            TestDataBuilder.buildMultiAssignmentList(Status.LIVE, ACTOR_ID, "attributes_orm_orgrole.json");

        roleAssignments.get(1).setRoleName("tribunal-caseworker");

        when(persistenceService.retrieveRoleAssignmentsByQueryRequest(any(), any(), any(), any(), any(), anyBoolean()))
            .thenReturn(roleAssignments);
        when(persistenceService.getActorCacheEntity(ACTOR_ID)).thenReturn(ActorCacheEntity.builder().actorId(ACTOR_ID)
                                                                              .etag(1L).build());
    }
}
