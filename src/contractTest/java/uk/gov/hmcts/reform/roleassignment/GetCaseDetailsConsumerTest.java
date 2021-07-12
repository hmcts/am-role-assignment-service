package uk.gov.hmcts.reform.roleassignment;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.google.common.collect.Maps;
import groovy.util.logging.Slf4j;
import io.restassured.http.ContentType;
import net.serenitybdd.rest.SerenityRest;
import org.apache.http.client.fluent.Executor;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.roleassignment.domain.model.Case;

import java.util.Map;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactTestFor(providerName = "ccdDataStoreAPI_Cases")
@PactFolder("pacts")
@SpringBootTest
public class GetCaseDetailsConsumerTest {

    private static final String CASE_ID = "1234";
    private static final String CCD_GET_CASE_DETAILS = "/cases/" + CASE_ID;

    @BeforeEach
    public void setUpEachTest() throws InterruptedException {
        Thread.sleep(2000);
    }

    @After
    void teardown() {
        Executor.closeIdleConnections();
    }

    @Pact(provider = "ccdDataStoreAPI_Cases", consumer = "accessMgmt_roleAssignmentService")
    public RequestResponsePact executeGetCaseDetailsAndGet200 (PactDslWithProvider builder) {

        return builder
            .given("A Get Case is requested")
            .uponReceiving("CCD data store takes s2s/auth token and caseId, then returns case detail information")
            .path(CCD_GET_CASE_DETAILS)
            .method(HttpMethod.GET.toString())
            .willRespondWith()
            .status(HttpStatus.OK.value())
            .headers(getResponseHeaders())
            .body(createResponse())
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "executeGetCaseDetailsAndGet200")
    void getCaseDetailsAndGet200Test(MockServer mockServer) {
        String actualResponseBody =
            SerenityRest
                .given()
                .headers(getHttpHeaders())
                .get(mockServer.getUrl() + CCD_GET_CASE_DETAILS)
                .then()
                .log().all().extract().asString();
        JSONObject jsonResponse = new JSONObject(actualResponseBody);
        JSONArray ccdResponse = (JSONArray) jsonResponse.get("ccdResponse");
        JSONObject first = (JSONObject) ccdResponse.get(0);
        assertThat(first.get("id"), equalTo(CASE_ID));
    }

    private DslPart createResponse() {
        return newJsonBody(o -> o
            .minArrayLike("ccdResponse", 1, 1,
                          ccdResponse -> ccdResponse
                              .stringValue("id", CASE_ID)
                              .stringValue("case_type", "IA")
                              .stringValue("jurasdiction", "Asylum")
            )).build();
    }

    @NotNull
    private Map<String, String> getResponseHeaders() {
        Map<String, String> responseHeaders = Maps.newHashMap();
        responseHeaders.put("Content-Type",
                            "application/vnd.uk.gov.hmcts.role-assignment-service.get-assignments+json;charset=UTF-8;version=1.0");
        return responseHeaders;
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("ServiceAuthorization", "Bearer " + "1234");
        headers.add("Authorization", "Bearer " + "2345");
        return headers;
    }
}
