package uk.gov.hmcts.reform.roleassignment;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.serenitybdd.rest.SerenityRest;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.roleassignment.apihelper.Constants;
import uk.gov.hmcts.reform.roleassignment.utils.IdamUtils;

@SpringBootTest
@RunWith(SpringIntegrationSerenityRunner.class)
//@ConfigurationProperties()
public class SmokeTest extends BaseTest {

    @Value("${idam.s2s-auth.totp_secret}")
    String secret;
    @Value("${idam.s2s-auth.microservice}")
    String microService;
    @Value("${idam.s2s-auth.url}")
    String s2sUrl;
    @Value("${roleAssignmentUrl}")
    String roleAssignmentUrl;

    IdamUtils idamUtils = new IdamUtils();

    String username = "befta.caseworker.2.solicitor.2@gmail.com";
    String password = "Pa55word11";
    String userToken = idamUtils.getIdamOauth2Token(username, password);
    String documentId = "00000000-0000-0000-0000-000000000000";


    @Test
    public void should_receive_response_for_a_get_document_meta_data() {

        RestAssured.baseURI = roleAssignmentUrl + "/cases/documents/" + documentId;
        RestAssured.useRelaxedHTTPSValidation();

        Response response = SerenityRest
            .given()
            .relaxedHTTPSValidation()
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .header(Constants.SERVICE_AUTHORIZATION2, Constants.BEARER + getServiceAuth())
            .header(Constants.AUTHORIZATION, Constants.BEARER + userToken)
            .when()
            .get("/")
            .andReturn();
        response.then().assertThat().statusCode(HttpStatus.NOT_FOUND.value())
                .body("message", Matchers.equalTo("Resource not found " + documentId));
    }

    private String getServiceAuth() {
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            s2sUrl = "http://localhost:4502";
        } else {
            s2sUrl = "http://rpe-service-auth-provider-aat.service.core-compute-aat.internal";
        }

        return new BaseTest().authTokenGenerator(secret, microService,
                                                 generateServiceAuthorisationApi(s2sUrl)
                                                ).generate();
    }
}
