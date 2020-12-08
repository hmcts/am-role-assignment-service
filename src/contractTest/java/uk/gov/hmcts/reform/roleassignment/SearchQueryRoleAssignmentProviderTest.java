package uk.gov.hmcts.reform.roleassignment;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.roleassignment.controller.endpoints.QueryAssignmentController;
import uk.gov.hmcts.reform.roleassignment.data.ActorCacheEntity;
import uk.gov.hmcts.reform.roleassignment.domain.model.Assignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.roleassignment.domain.service.queryroles.QueryRoleAssignmentOrchestrator;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@Provider("am_role_assignment_service_search_query")
@PactBroker(scheme = "${PACT_BROKER_SCHEME:http}", host = "${PACT_BROKER_URL:localhost}",
    port = "${PACT_BROKER_PORT:9292}")
@Import(RoleAssignmentProviderTestConfiguration.class)
public class SearchQueryRoleAssignmentProviderTest {

    @Autowired
  private PersistenceService persistenceService;

    @Autowired
  private QueryRoleAssignmentOrchestrator queryRoleAssignmentOrchestrator;

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
  void pactVerificationTestTemplate(PactVerificationContext context) {
        context.verifyInteraction();
    }

    @BeforeEach
  void before(PactVerificationContext context) {
        MockMvcTestTarget testTarget = new MockMvcTestTarget();
        System.getProperties().setProperty("pact.verifier.publishResults", "true");
        testTarget.setControllers(new QueryAssignmentController(
            queryRoleAssignmentOrchestrator
        ));
        context.setTarget(testTarget);

    }

    @State({"A list of role assignments for the search query"})
  public void searchQueryByActorIdWithSuccess() throws Exception {
        setInitiMock();
    }

    private void setInitiMock() throws Exception {
        String actorId = "234873";
        List<Assignment> roleAssignments
            = TestDataBuilder.buildAssignmentList(Status.LIVE, actorId, "attributesSearchQuery.json");

        when(persistenceService.retrieveRoleAssignmentsByQueryRequest(any(),any(),any(),any(),any(),anyBoolean()))
            .thenReturn(roleAssignments);
        when(persistenceService.getActorCacheEntity(actorId)).thenReturn(ActorCacheEntity.builder().actorId(actorId)
            .etag(1L).build());
    }
}
