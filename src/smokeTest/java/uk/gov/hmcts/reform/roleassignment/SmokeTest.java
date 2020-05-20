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

    @Test
    public void sample_test_setuo_should_receive_response_for_role_assignment_api() {

        RestAssured.baseURI = "http://localhost:4096";
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
}
