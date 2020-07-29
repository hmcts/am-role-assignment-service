package uk.gov.hmcts.reform.roleassignment;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import lombok.NoArgsConstructor;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.serenitybdd.rest.SerenityRest;
import org.apache.commons.lang.StringUtils;
import org.hamcrest.Matchers;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.idam.client.models.TokenRequest;
import uk.gov.hmcts.reform.roleassignment.v1.V1;

@RunWith(SpringIntegrationSerenityRunner.class)
@NoArgsConstructor
@TestPropertySource(value = "classpath:application.yaml")
public class SmokeTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(SmokeTest.class);

    String roleAssignmentUrl = System.getenv("TEST_URL");
    String secret = System.getenv("AM_ROLE_ASSIGNMENT_SERVICE_SECRET");
    String microService = "am_role_assignment_service";
    String s2sUrl = System.getenv("IDAM_S2S_URL");

    String clientId = System.getenv("ROLE_ASSIGNMENT_CLIENT");
    String clientSecret = System.getenv("ROLE_ASSIGNMENT_CLIENT_SECRET");
    String username = System.getenv("TEST_USER");
    String password = System.getenv("TEST_USER_PASSWORD");
    String scope = System.getenv("OAUTH2_SCOPE_VARIABLES");

    @Test
    public void should_receive_response_for_get_by_query_params_case_id() {

        String accessToken = searchUserByUserId(getManageUserToken());
        String serviceAuth = new BaseTest()
            .authTokenGenerator(secret, microService, generateServiceAuthorisationApi(s2sUrl)).generate();
        String targetInstance = roleAssignmentUrl + "/am/role-assignments?roleType=case&caseId=1234567890000000";

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

        String accessToken = searchUserByUserId(getManageUserToken());
        String serviceAuth = new BaseTest()
            .authTokenGenerator(secret, microService, generateServiceAuthorisationApi(s2sUrl)).generate();
        String targetInstance = roleAssignmentUrl + "/am/role-assignments/roles";
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
        String accessToken = searchUserByUserId(getManageUserToken());
        String serviceAuth = new BaseTest()
            .authTokenGenerator(secret, microService, generateServiceAuthorisationApi(s2sUrl)).generate();
        String targetInstance = roleAssignmentUrl
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
        String accessToken = searchUserByUserId(getManageUserToken());
        String serviceAuth = new BaseTest()
            .authTokenGenerator(secret, microService, generateServiceAuthorisationApi(s2sUrl)).generate();
        String targetInstance = roleAssignmentUrl + "/am/role-assignments/actors/0b00bfc0-bb00-00ea-b0de-0000ac000000";
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
            .body("errorDescription",
                  Matchers.equalTo("Role Assignment not found for Actor 0b00bfc0-bb00-00ea-b0de-0000ac000000"));
        response.then().assertThat().body("errorMessage", Matchers.equalTo("Resource not found"));
    }

    /* @Test
    public void should_receive_response_for_delete_by_assignment_id() {
        String accessToken = searchUserByUserId(getManageUserToken());
        String serviceAuth = new BaseTest()
            .authTokenGenerator(secret, microService, generateServiceAuthorisationApi(s2sUrl)).generate();
        String targetInstance = roleAssignmentUrl + "/am/role-assignments/dbd4177f-94f6-4e91-bb9b-591faa81dfd5";

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
    }*/

    /*@Test
    public void should_receive_response_for_delete_by_process_and_reference() {
        String accessToken = searchUserByUserId(getManageUserToken());
        String serviceAuth = new BaseTest()
            .authTokenGenerator(secret, microService, generateServiceAuthorisationApi(s2sUrl)).generate();
        String targetInstance = roleAssignmentUrl + "/am/role-assignments?process=p2&reference=r2";

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
    }*/

    /* @Test
    public void should_receive_response_for_add_role_assignment() throws IOException {
        String accessToken = searchUserByUserId(getManageUserToken());
        String serviceAuth = new BaseTest()
            .authTokenGenerator(secret, microService, generateServiceAuthorisationApi(s2sUrl)).generate();
        String targetInstance = roleAssignmentUrl + "/am/role-assignments?process=p2&reference=r2";

        //FileInputStream fileInputStream = new FileInputStream(new File("resources/RequestBody.td.json"));
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("RequestBody.td.json").getFile());
        FileInputStream fileInputStream = new FileInputStream(file);

        RestAssured.useRelaxedHTTPSValidation();

        Response response = SerenityRest
            .given()
            .relaxedHTTPSValidation()
            .header("Content-Type", "application/json")
            .header("ServiceAuthorization", "Bearer " + serviceAuth)
            .header("Authorization", "Bearer " + accessToken)
            .body(IOUtils.toString(fileInputStream, "UTF-8"))
            .when()
            .post(targetInstance)
            .andReturn();
        response.then().assertThat().statusCode(HttpStatus.CREATED.value());
    }*/

    public TokenRequest getManageUserToken() {

        TokenRequest tokenRequest = new TokenRequest(
            clientId,
            clientSecret,
            "password",
            "",
            username,
            password,
            getScope(scope),
            "4",
            ""
        );
        return tokenRequest;
    }

    @NotNull
    private String getScope(String scope) {
        if (!StringUtils.isEmpty(scope)) {
            return scope.replaceAll(" ", "+");
        }
        return scope;
    }
}
