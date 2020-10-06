package uk.gov.hmcts.reform.roleassignment;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.model.RequestResponsePact;
import com.google.common.collect.Maps;
import groovy.util.logging.Slf4j;
import io.restassured.http.ContentType;
import net.serenitybdd.rest.SerenityRest;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ExtendWith(PactConsumerTestExt.class)
@SpringBootTest
public class RoleAssignmentConsumerTest {

    private static final String RAS_GET_LIST_ROLES_URL = "/am/role-assignments/roles";
    private static final String ROLES = "roles";

    @Pact(provider = "am_role_assignment_service", consumer = "ccd")
    public RequestResponsePact executeGetListOfRolesAndGet200(PactDslWithProvider builder) {

        Map<String, String> responseHeaders = Maps.newHashMap();
        responseHeaders.put("Content-Type", "application/json");

        return builder
            .given("a list of roles are available in role assignment service")
            .uponReceiving("RAS takes s2s/auth token and returns list of roles to "
                               + "- CCD API")
            .path(RAS_GET_LIST_ROLES_URL)
            .method(HttpMethod.GET.toString())
            .willRespondWith()
            .status(HttpStatus.OK.value())
            .headers(responseHeaders)
            .body(createListRolesResponse())
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "executeGetListOfRolesAndGet200")
    void getListOfRolesAndGet200Test(MockServer mockServer)
        throws JSONException {
        String actualResponseBody =
            SerenityRest
                .given()
                .headers(getHttpHeaders())
                .contentType(ContentType.URLENC)
                .get(mockServer.getUrl() + RAS_GET_LIST_ROLES_URL)
                .then()
                .log().all().extract().asString();
        JSONObject response = new JSONObject(actualResponseBody);
        assertThat(response).isNotNull();
        //assertThat(response.getString(ROLES)).isNotBlank();
        //assertThat(response.getString(ROLES)).isEqualTo("caseworker");
    }

    private DslPart createListRolesResponse() {
        return new PactDslJsonBody()
            .array(ROLES)
            .stringType("caseworker")
            .closeArray();
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("ServiceAuthorization", "Bearer " + "1234");
        headers.add("Authorization", "Bearer " + "2345");
        return headers;
    }
}
