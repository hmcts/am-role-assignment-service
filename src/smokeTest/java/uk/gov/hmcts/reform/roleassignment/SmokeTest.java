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


@SpringBootTest
@RunWith(SpringIntegrationSerenityRunner.class)
//@ConfigurationProperties()
public class SmokeTest extends BaseTest {
    @Value("${roleAssignmentUrl}")
    String roleAssignmentUrl;
    @Test
    public void sample_test_setup_should_receive_response_for_role_assignment_api() {
        RestAssured.baseURI = roleAssignmentUrl;
        RestAssured.useRelaxedHTTPSValidation();
        Response response = SerenityRest
            .given()
            .relaxedHTTPSValidation()
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .when()
            .get("/")
            .andReturn();
        response.then().assertThat().statusCode(HttpStatus.OK.value());
    }

    @Test
    public void should_receive_response_for_a_get_document_meta_data() {

        String serviceAuth = new BaseTest().authTokenGenerator(secret, microService, generateServiceAuthorisationApi(s2sUrl)).generate();
        String targetInstance = roleAssignmentUrl + "/am/role-assignments?roleType=case&caseId=1234567890000000";

        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();

        Response response = SerenityRest
            .given()
            .relaxedHTTPSValidation()
            .header( "ServiceAuthorization", "Bearer " + serviceAuth)
            .header( "Authorization", "Bearer " + "Authorization")
            .when()
            .get("/")
            .andReturn();
        response.then().assertThat().statusCode( HttpStatus.NOT_FOUND.value())
            .body("message", Matchers.equalTo("No Assignment records found for given criteria"));
    }
}
