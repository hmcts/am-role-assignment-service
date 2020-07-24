package uk.gov.hmcts.reform.roleassignment;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import lombok.NoArgsConstructor;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.serenitybdd.rest.SerenityRest;
import org.apache.commons.io.IOUtils;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.roleassignment.v1.V1;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@SpringBootTest
@RunWith(SpringIntegrationSerenityRunner.class)
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
    public void should_receive_response_for_get_by_query_params_case_id() {

        String serviceAuth = new BaseTest()
            .authTokenGenerator(secret, microService, baseTest.generateServiceAuthorisationApi(s2sUrl)).generate();
        String targetInstance = roleAssignmentUrl + "/am/role-assignments?roleType=case&caseId=1234567890000000";

        RestAssured.useRelaxedHTTPSValidation();

        Response response = SerenityRest
            .given()
            .relaxedHTTPSValidation()
            .header("ServiceAuthorization", "Bearer " + serviceAuth)
            .header("Authorization", "Bearer " + baseTest.getManageUserToken())
            .when()
            .get(targetInstance)
            .andReturn();
        response.then().assertThat().statusCode(HttpStatus.NOT_FOUND.value())
                .body("errorDescription", Matchers.equalTo(V1.Error.ASSIGNMENT_RECORDS_NOT_FOUND));
        response.then().assertThat().body("errorMessage", Matchers.equalTo("Resource not found"));
    }

    @Test
    public void should_receive_response_for_get_by_query_params_actor_id() {

        String serviceAuth = new BaseTest()
            .authTokenGenerator(secret, microService, baseTest.generateServiceAuthorisationApi(s2sUrl)).generate();
        String targetInstance = roleAssignmentUrl
            + "/am/role-assignments?roleType=case&actorId=0b00bfc0-bb00-00ea-b0de-0000ac000000";

        RestAssured.useRelaxedHTTPSValidation();

        Response response = SerenityRest
            .given()
            .relaxedHTTPSValidation()
            .header("ServiceAuthorization", "Bearer " + serviceAuth)
            .header("Authorization", "Bearer " + baseTest.getManageUserToken())
            .when()
            .get(targetInstance)
            .andReturn();
        response.then().assertThat().statusCode(HttpStatus.NOT_FOUND.value())
            .body("errorDescription", Matchers.equalTo(V1.Error.ASSIGNMENT_RECORDS_NOT_FOUND));
        response.then().assertThat().body("errorMessage", Matchers.equalTo("Resource not found"));
    }

    @Test
    public void should_receive_response_for_get_by_actor_id() {

        String serviceAuth = new BaseTest()
            .authTokenGenerator(secret, microService, baseTest.generateServiceAuthorisationApi(s2sUrl)).generate();
        String targetInstance = roleAssignmentUrl + "/am/role-assignments/actors/0b00bfc0-bb00-00ea-b0de-0000ac000000";

        RestAssured.useRelaxedHTTPSValidation();

        Response response = SerenityRest
            .given()
            .relaxedHTTPSValidation()
            .header("ServiceAuthorization", "Bearer " + serviceAuth)
            .header("Authorization", "Bearer " + baseTest.getManageUserToken())
            .when()
            .get(targetInstance)
            .andReturn();
        response.then().assertThat().statusCode(HttpStatus.NOT_FOUND.value())
            .body("errorDescription",
                Matchers.equalTo("Role Assignment not found for Actor 0b00bfc0-bb00-00ea-b0de-0000ac000000"));
        response.then().assertThat().body("errorMessage", Matchers.equalTo("Resource not found"));
    }

    @Test
    public void should_receive_response_for_get_static_roles() {

        String serviceAuth = new BaseTest()
            .authTokenGenerator(secret, microService, baseTest.generateServiceAuthorisationApi(s2sUrl)).generate();
        String targetInstance = roleAssignmentUrl + "/am/role-assignments/roles";

        RestAssured.useRelaxedHTTPSValidation();

        Response response = SerenityRest
            .given()
            .relaxedHTTPSValidation()
            .header("ServiceAuthorization", "Bearer " + serviceAuth)
            .header("Authorization", "Bearer " + baseTest.getManageUserToken())
            .when()
            .get(targetInstance)
            .andReturn();
        response.then().assertThat().statusCode(HttpStatus.OK.value());
    }

    @Test
    public void should_receive_response_for_delete_by_assignment_id() {

        String serviceAuth = new BaseTest()
            .authTokenGenerator(secret, microService, baseTest.generateServiceAuthorisationApi(s2sUrl)).generate();
        String targetInstance = roleAssignmentUrl + "/am/role-assignments/dbd4177f-94f6-4e91-bb9b-591faa81dfd5";

        RestAssured.useRelaxedHTTPSValidation();

        Response response = SerenityRest
            .given()
            .relaxedHTTPSValidation()
            .header("ServiceAuthorization", "Bearer " + serviceAuth)
            .header("Authorization", "Bearer " + baseTest.getManageUserToken())
            .when()
            .delete(targetInstance)
            .andReturn();
        response.then().assertThat().statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    public void should_receive_response_for_delete_by_process_and_reference() {

        String serviceAuth = new BaseTest()
            .authTokenGenerator(secret, microService, baseTest.generateServiceAuthorisationApi(s2sUrl)).generate();
        String targetInstance = roleAssignmentUrl + "/am/role-assignments?process=p2&reference=r2";

        RestAssured.useRelaxedHTTPSValidation();

        Response response = SerenityRest
            .given()
            .relaxedHTTPSValidation()
            .header("ServiceAuthorization", "Bearer " + serviceAuth)
            .header("Authorization", "Bearer " + baseTest.getManageUserToken())
            .when()
            .delete(targetInstance)
            .andReturn();
        response.then().assertThat().statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    public void should_receive_response_for_add_role_assignment() throws IOException {

        String serviceAuth = new BaseTest()
            .authTokenGenerator(secret, microService, baseTest.generateServiceAuthorisationApi(s2sUrl)).generate();
        String targetInstance = roleAssignmentUrl + "/am/role-assignments?process=p2&reference=r2";

        FileInputStream fileInputStream = new FileInputStream(new File("\\resources\\RequestBody.td.json"));

        RestAssured.useRelaxedHTTPSValidation();

        Response response = SerenityRest
            .given()
            .relaxedHTTPSValidation()
            .header("ServiceAuthorization", "Bearer " + serviceAuth)
            .header("Authorization", "Bearer " + baseTest.getManageUserToken())
            .body(IOUtils.toString(fileInputStream, "UTF-8"))
            .when()
            .post(targetInstance)
            .andReturn();
        response.then().assertThat().statusCode(HttpStatus.CREATED.value());
    }

}
