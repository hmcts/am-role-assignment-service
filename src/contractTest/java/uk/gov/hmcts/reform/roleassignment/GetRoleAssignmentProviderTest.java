package uk.gov.hmcts.reform.roleassignment;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.roleassignment.controller.endpoints.CreateAssignmentController;
import uk.gov.hmcts.reform.roleassignment.controller.endpoints.GetAssignmentController;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ParseRequestService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ValidationModelService;
import uk.gov.hmcts.reform.roleassignment.domain.service.createroles.CreateRoleAssignmentOrchestrator;
import uk.gov.hmcts.reform.roleassignment.domain.service.createroles.CreateRoleAssignmentService;
import uk.gov.hmcts.reform.roleassignment.domain.service.getroles.RetrieveRoleAssignmentOrchestrator;
import org.springframework.context.annotation.Import;
import uk.gov.hmcts.reform.roleassignment.util.PersistenceUtil;

import java.io.IOException;

@ExtendWith(SpringExtension.class)
@Provider("am_role_assignment_service")
@PactBroker(scheme = "${PACT_BROKER_SCHEME:http}", host = "${PACT_BROKER_URL:localhost}",
    port = "${PACT_BROKER_PORT:9292}")
@Import(RoleAssignmentProviderTestConfiguration.class)
public class GetRoleAssignmentProviderTest {

    @Autowired
    private RetrieveRoleAssignmentOrchestrator retrieveRoleAssignmentServiceMock;


    @Autowired
    private CreateRoleAssignmentOrchestrator createRoleAssignmentOrchestrator;


    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        context.verifyInteraction();
    }

    void before(PactVerificationContext context) {
        MockMvcTestTarget testTarget = new MockMvcTestTarget();
        System.getProperties().setProperty("pact.verifier.publishResults", "true");
        testTarget.setControllers(new GetAssignmentController(
            retrieveRoleAssignmentServiceMock
        ));
        context.setTarget(testTarget);

    }

    void beforeCreate(PactVerificationContext context) {
        MockMvcTestTarget testTarget = new MockMvcTestTarget();
        System.getProperties().setProperty("pact.verifier.publishResults", "true");
        testTarget.setControllers(new CreateAssignmentController(
            createRoleAssignmentOrchestrator
        ));
        context.setTarget(testTarget);

    }
    @State({"A list of roles are available in role assignment service"})
    public void toGetlistOfRolesWithSuccess() throws IOException, JSONException {
    }

    @State({"The assignment request is valid with one requested role and replaceExisting flag as false"})
    public void toGetlistOfRolesWithSuccess1() throws IOException, JSONException {
        System.out.println("toGetlistOfRolesWithSuccess.......");
    }
}
