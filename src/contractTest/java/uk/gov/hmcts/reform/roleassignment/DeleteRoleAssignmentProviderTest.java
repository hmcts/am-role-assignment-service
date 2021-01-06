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
import uk.gov.hmcts.reform.roleassignment.controller.endpoints.DeleteAssignmentController;
import uk.gov.hmcts.reform.roleassignment.domain.service.deleteroles.DeleteRoleAssignmentOrchestrator;

@ExtendWith(SpringExtension.class)
@Provider("am_role_assignment_service_delete")
@PactBroker(scheme = "${PACT_BROKER_SCHEME:http}", host = "${PACT_BROKER_URL:localhost}",
    port = "${PACT_BROKER_PORT:9292}")
@Import(RoleAssignmentProviderTestConfiguration.class)
public class DeleteRoleAssignmentProviderTest {

    @Autowired
    private DeleteRoleAssignmentOrchestrator deleteRoleAssignmentOrchestrator;

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        context.verifyInteraction();
    }

    @BeforeEach
    void beforeCreate(PactVerificationContext context) {
        MockMvcTestTarget testTarget = new MockMvcTestTarget();
        System.getProperties().setProperty("pact.verifier.publishResults", "true");
        testTarget.setControllers(new DeleteAssignmentController(
            deleteRoleAssignmentOrchestrator
        ));
        context.setTarget(testTarget);
    }

    @State({"An actor with provided id is available in role assignment service"})
    public void createRoleAssignmentReplaceExistingFalse() {
        setInitiMock();
    }

    private void setInitiMock() {
//        when(persistenceService.persistRequest(any())).thenReturn(createEntity());
//        when(securityUtils.getUserId()).thenReturn("14a21569-eb80-4681-b62c-6ae2ed069e2f");
//        when(correlationInterceptorUtil.preHandle(any())).thenReturn("14a21569-eb80-4681-b62c-6ae2ed069e2d");
    }
}
