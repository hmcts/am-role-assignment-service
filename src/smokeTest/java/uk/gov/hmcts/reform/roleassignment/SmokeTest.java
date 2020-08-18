package uk.gov.hmcts.reform.roleassignment;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import lombok.NoArgsConstructor;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.serenitybdd.rest.SerenityRest;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.apache.commons.io.IOUtils;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.roleassignment.v1.V1;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


@RunWith(SpringIntegrationSerenityRunner.class)
@NoArgsConstructor
@WithTags({@WithTag("testType:Smoke")})
public class SmokeTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(SmokeTest.class);

    UserTokenProviderConfig config;
    String accessToken;
    String serviceAuth;

    @Before
    public void setUp() {
        config = new UserTokenProviderConfig();
        accessToken = searchUserByUserId(config);
        serviceAuth = new BaseTest()
            .authTokenGenerator(
                config.getSecret(),
                config.getMicroService(),
                generateServiceAuthorisationApi(config.getS2sUrl())
                               ).generate();
    }

    @Test
    public void should_receive_response_for_get_by_query_params_case_id() {

        String targetInstance = config.getRoleAssignmentUrl()
                                + "/am/role-assignments?roleType=case&caseId=1234567890000000";
        RestAssured.useRelaxedHTTPSValidation();

        Response response = SerenityRest
            .given()
            .relaxedHTTPSValidation()
            .header("ServiceAuthorization", "Bearer " + serviceAuth)
            .header("Authorization", "Bearer " + accessToken)
            .when()
            .get(targetInstance)
            .andReturn();
        response.then().assertThat().statusCode(HttpStatus.NOT_FOUND.value())
                .body("errorDescription", Matchers.equalTo(V1.Error.ASSIGNMENT_RECORDS_NOT_FOUND));
        response.then().assertThat().body("errorMessage", Matchers.equalTo("Resource not found"));
    }

    @Test
    public void should_receive_response_for_get_static_roles() {

        String targetInstance = config.getRoleAssignmentUrl() + "/am/role-assignments/roles";
        RestAssured.useRelaxedHTTPSValidation();

        Response response = SerenityRest
            .given()
            .relaxedHTTPSValidation()
            .header("ServiceAuthorization", "Bearer " + serviceAuth)
            .header("Authorization", "Bearer " + accessToken)
            .when()
            .get(targetInstance)
            .andReturn();
        response.then().assertThat().statusCode(HttpStatus.OK.value());
    }

    @Test
    public void should_receive_response_for_get_by_query_params_actor_id() {

        String targetInstance = config.getRoleAssignmentUrl()
                                + "/am/role-assignments?roleType=case&actorId=0b00bfc0-bb00-00ea-b0de-0000ac000000";
        RestAssured.useRelaxedHTTPSValidation();

        Response response = SerenityRest
            .given()
            .relaxedHTTPSValidation()
            .header("ServiceAuthorization", "Bearer " + serviceAuth)
            .header("Authorization", "Bearer " + accessToken)
            .when()
            .get(targetInstance)
            .andReturn();
        response.then().assertThat().statusCode(HttpStatus.NOT_FOUND.value())
                .body("errorDescription", Matchers.equalTo(V1.Error.ASSIGNMENT_RECORDS_NOT_FOUND));
        response.then().assertThat().body("errorMessage", Matchers.equalTo("Resource not found"));
    }

    @Test
    public void should_receive_response_for_get_by_actor_id() {

        String targetInstance = config.getRoleAssignmentUrl()
                                + "/am/role-assignments/actors/0b00bfc0-bb00-00ea-b0de-0000ac000000";
        RestAssured.useRelaxedHTTPSValidation();

        Response response = SerenityRest
            .given()
            .relaxedHTTPSValidation()
            .header("ServiceAuthorization", "Bearer " + serviceAuth)
            .header("Authorization", "Bearer " + accessToken)
            .when()
            .get(targetInstance)
            .andReturn();
        response.then().assertThat().statusCode(HttpStatus.NOT_FOUND.value())
                .body(
                    "errorDescription",
                    Matchers.equalTo(
                        "Role Assignment not found for Actor 0b00bfc0-bb00-00ea-b0de-0000ac000000"));

        response.then().assertThat().body("errorMessage", Matchers.equalTo("Resource not found"));
    }

    @Test
    public void should_receive_response_for_delete_by_assignment_id() {

        String targetInstance = config.getRoleAssignmentUrl()
                                + "/am/role-assignments/dbd4177f-94f6-4e91-bb9b-591faa81dfd5";
        RestAssured.useRelaxedHTTPSValidation();

        Response response = SerenityRest
            .given()
            .relaxedHTTPSValidation()
            .header("Content-Type", "application/json")
            .header("ServiceAuthorization", "Bearer " + serviceAuth)
            .header("Authorization", "Bearer " + accessToken)
            .when()
            .delete(targetInstance)
            .andReturn();
        response.then().assertThat().statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    public void should_receive_response_for_delete_by_process_and_reference() {

        String targetInstance = config.getRoleAssignmentUrl() + "/am/role-assignments?process=p2&reference=r2";
        RestAssured.useRelaxedHTTPSValidation();

        Response response = SerenityRest
            .given()
            .relaxedHTTPSValidation()
            .header("ServiceAuthorization", "Bearer " + serviceAuth)
            .header("Authorization", "Bearer " + accessToken)
            .when()
            .delete(targetInstance)
            .andReturn();
        response.then().assertThat().statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    public void should_receive_response_for_add_role_assignment() throws IOException {

        String targetInstance = config.getRoleAssignmentUrl() + "/am/role-assignments";
        RestAssured.useRelaxedHTTPSValidation();

        Response response = SerenityRest
            .given()
            .relaxedHTTPSValidation()
            .header("Content-Type", "application/json")
            .header("ServiceAuthorization", "Bearer " + serviceAuth)
            .header("Authorization", "Bearer " + accessToken)
            .body("Hello")
            .when()
            .post(targetInstance)
            .andReturn();
        response.then().assertThat().statusCode(HttpStatus.BAD_REQUEST.value());
    }

    private String fetchRequestBody() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("create_request_body.json").getFile());
        FileInputStream fileInputStream = new FileInputStream(file);
        return IOUtils.toString(fileInputStream, "UTF-8");
    }
}
