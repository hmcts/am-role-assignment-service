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
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.roleassignment.controller.endpoints.GetAssignmentController;
import uk.gov.hmcts.reform.roleassignment.domain.service.getroles.RetrieveRoleAssignmentOrchestrator;
import org.springframework.context.annotation.Import;
import java.io.IOException;

@ExtendWith(SpringExtension.class)
@Provider("am_role_assignment_service")
@PactBroker(scheme = "http", host = "localhost", port = "9292")
@Import(RoleAssignmentProviderTestConfiguration.class)
public class RoleAssignmentProviderTest {

    @Autowired
    private RetrieveRoleAssignmentOrchestrator retrieveRoleAssignmentServiceMock;

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        context.verifyInteraction();
    }

    @BeforeEach
    void before(PactVerificationContext context) {
        MockMvcTestTarget testTarget = new MockMvcTestTarget();

        testTarget.setControllers(new GetAssignmentController(
            retrieveRoleAssignmentServiceMock
        ));
        context.setTarget(testTarget);

    }

    @State({"A list of roles are available in role assignment service"})
    public void toGetlistOfRolesWithSuccess() throws IOException, JSONException {

    }
}
