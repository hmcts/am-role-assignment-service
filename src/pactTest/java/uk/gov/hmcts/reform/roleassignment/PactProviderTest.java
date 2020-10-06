package uk.gov.hmcts.reform.roleassignment;

import au.com.dius.pact.provider.junit.PactRunner;
import au.com.dius.pact.provider.junit.target.HttpTarget;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.VerificationReports;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.target.Target;
import au.com.dius.pact.provider.junitsupport.target.TestTarget;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.roleassignment.controller.endpoints.GetAssignmentController;
import uk.gov.hmcts.reform.roleassignment.domain.service.getroles.RetrieveRoleAssignmentOrchestrator;
import uk.gov.hmcts.reform.roleassignment.util.SecurityUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

@Provider("am_role_assignment_service")
@PactBroker(host = "localhost", port = "9292")
@VerificationReports({"console", "markdown"})
@RunWith(PactRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = {
    "server.port=4096"
})
public class PactProviderTest {

    @MockBean
    GetAssignmentController getAssignmentController = mock(GetAssignmentController.class);

    @MockBean
    RetrieveRoleAssignmentOrchestrator retrieveRoleAssignmentService
        = mock(RetrieveRoleAssignmentOrchestrator.class);

    @MockBean
    private final SecurityUtils securityUtils = mock(SecurityUtils.class);

    @TestTarget
    public final Target target = new HttpTarget("http", "localhost", 4096, "/");

    @State("a list of roles are available in role assignment service")
    public void listOfRoles() throws IOException {

        when(securityUtils.getUserToken()).thenReturn("Bearer 1234");
        when(securityUtils.authorizationHeaders()).thenReturn(getHttpHeaders());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(getRoles().toString());
        when(retrieveRoleAssignmentService.getListOfRoles()).thenReturn(jsonNode);

        when(getAssignmentController.getListOfRoles(any()))
            .thenReturn(ResponseEntity.status(HttpStatus.OK)
                            .body(getRoles()));

    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("ServiceAuthorization", "Bearer " + "1234");
        headers.add("Authorization", "Bearer " + "2345");
        return headers;
    }

    private JSONObject getRoles() {
        JSONArray locArr = new JSONArray();
        locArr.put("caseworker");
        JSONObject main = new JSONObject();
        main.put("roles", locArr);
        return main;
    }
}
