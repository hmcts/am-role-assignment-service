package uk.gov.hmcts.reform.roleassignment;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import lombok.NoArgsConstructor;
import net.serenitybdd.rest.SerenityRest;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.roleassignment.v1.V1;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@NoArgsConstructor
@TestPropertySource(value = "classpath:application.yaml")
public class SmokeTest {
    @Value("${roleAssignmentUrl}")
    String roleAssignmentUrl;

    @Value("${idam.s2s-auth.totp_secret}")
    String secret;
    @Value("${idam.s2s-auth.microservice}")
    String microService;
    @Value("${idam.s2s-auth.url}")
    String s2sUrl;

    @Autowired
    BaseTest baseTest;

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

        String serviceAuth = new BaseTest()
            .authTokenGenerator(secret, microService, baseTest.generateServiceAuthorisationApi(s2sUrl)).generate();
        String targetInstance = roleAssignmentUrl + "/am/role-assignments?roleType=case&caseId=1234567890000000";

        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();

        Response response = SerenityRest
            .given()
            .relaxedHTTPSValidation()
            .header("ServiceAuthorization", "Bearer " + serviceAuth)
            .header("Authorization", "Bearer " + baseTest.getManageUserToken())
            .when()
            .get("am/role-assignments?roleType=case&actorId=123e4567-e89b-42d3-a456-556642445612")
            .andReturn();
        response.then().assertThat().statusCode(HttpStatus.NOT_FOUND.value())
                .body("message", Matchers.equalTo(V1.Error.ASSIGNMENT_RECORDS_NOT_FOUND));
    }
}
