package uk.gov.hmcts.reform.roleassignment;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.Pact;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import java.util.Map;
import java.util.TreeMap;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ExtendWith(PactConsumerTestExt.class)
@SpringBootTest
public class IdamConsumerTest {
    private static final String IDAM_OPEN_ID_TOKEN_URL = "/o/token";

    private PactDslJsonBody createAuthResponse() {
        return new PactDslJsonBody()
            .stringType("access_token", "eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FI")
            .stringType("refresh_token", "eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2lkIjoiYi9PNk92V")
            .stringType("scope", "openid profile roles")
            .stringType("id_token", "eyJ0eXAiOiJKV1QiLCJraWQiOiJiL082T3ZWdjEre")
            .stringType("token_type", "Bearer")
            .stringType("expires_in", "28799");
    }
    @Pact(provider = "Idam_api", consumer = "am_role_assignment_service")
    public RequestResponsePact executeGetIdamAccessTokenAndGet200(PactDslWithProvider builder) throws JSONException {
        String[] rolesArray = new String[1];
        rolesArray[0] = "caseworker";
        Map<String, String> responseHeaders = Maps.newHashMap();
        responseHeaders.put("Content-Type", "application/json");
        Map<String, Object> params = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        params.put("email", "am_pact_user@mailtest.gov.uk");
        params.put("password", "Pa55word11");
        params.put("forename", "testfirstname");
        params.put("surname", "testsurname");
        params.put("roles", rolesArray);
        return builder
            .given("a user exists", params)
            .uponReceiving("Provider takes user/pwd and returns Access Token to AM "
                               + "- ROLE ASSIGNMENT API")
            .path(IDAM_OPEN_ID_TOKEN_URL)
            .method(HttpMethod.POST.toString())
            .body(
                "client_id=pact"
                    + "&client_secret=pactsecret"
                    + "&grant_type=password"
                    + "&scope=openid profile roles"
                    + "&username=am_pact_user@mailtest.gov.uk"
                    + "&password=Pa55word11",
                "application/x-www-form-urlencoded"
            )
            .willRespondWith()
            .status(HttpStatus.OK.value())
            .headers(responseHeaders)
            .body(createAuthResponse())
            .toPact();
    }
    @Test
    @PactTestFor(pactMethod = "executeGetIdamAccessTokenAndGet200")
    public void should_post_to_token_endpoint_and_receive_access_token_with_200_response(MockServer mockServer)
        throws JSONException {
        String actualResponseBody =
            SerenityRest
                .given()
                .contentType(ContentType.URLENC)
                .formParam("client_id", "pact")
                .formParam("grant_type", "password")
                .formParam("username", "am_pact_user@mailtest.gov.uk")
                .formParam("password", "Pa55word11")
                .formParam("client_secret", "pactsecret")
                .formParam(
                    "scope",
                    "openid profile roles"
                )
                .post(mockServer.getUrl() + IDAM_OPEN_ID_TOKEN_URL)
                .then()
                .log().all().extract().asString();
        JSONObject response = new JSONObject(actualResponseBody);
        assertThat(response).isNotNull();
        assertThat(response.getString("access_token")).isNotBlank();
        assertThat(response.getString("token_type")).isEqualTo("Bearer");
        assertThat(response.getString("expires_in")).isNotBlank();
    }
}
