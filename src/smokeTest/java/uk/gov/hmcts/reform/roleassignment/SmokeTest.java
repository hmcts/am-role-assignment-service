package uk.gov.hmcts.reform.roleassignment;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.serenitybdd.rest.SerenityRest;
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

    @Value("${idam.s2s-auth.totp_secret}")
    String secret;
    @Value("${idam.s2s-auth.microservice}")
    String microService;
    @Value("${idam.s2s-auth.url}")
    String s2sUrl;

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
            .authTokenGenerator(secret, microService, generateServiceAuthorisationApi(s2sUrl)).generate();
        String targetInstance = roleAssignmentUrl + "/am/role-assignments?roleType=case&caseId=1234567890000000";

        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();

        Response response = SerenityRest
            .given()
            .relaxedHTTPSValidation()
            .header("ServiceAuthorization", "Bearer " + serviceAuth)
            .header(
                "Authorization",
                "Bearer "
                +
                "eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2lkIjoiYi9PNk92VnYxK3krV2dySDVVaTlXVGlvTHQwPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJhbS5kb2NrZXIuZGVmYXVsdEBobWN0cy5uZXQiLCJhdXRoX2xldmVsIjowLCJhdWRpdFRyYWNraW5nSWQiOiJiYWE1MGYwOC1kMjk5LTQxNTgtYTk1Ny0zYjAyNDZmZmE2ZGQiLCJpc3MiOiJodHRwOi8vZnItYW06ODA4MC9vcGVuYW0vb2F1dGgyL2htY3RzIiwidG9rZW5OYW1lIjoiYWNjZXNzX3Rva2VuIiwidG9rZW5fdHlwZSI6IkJlYXJlciIsImF1dGhHcmFudElkIjoiZDJiNTFlZDMtZTg1Ni00OTZhLTliYjAtMjhkZmQ5MmUyMDYxIiwiYXVkIjoiYW1fZG9ja2VyIiwibmJmIjoxNTk1NDA5MzY0LCJncmFudF90eXBlIjoicGFzc3dvcmQiLCJzY29wZSI6WyJvcGVuaWQiLCJwcm9maWxlIiwicm9sZXMiLCJhdXRob3JpdGllcyJdLCJhdXRoX3RpbWUiOjE1OTU0MDkzNjQsInJlYWxtIjoiL2htY3RzIiwiZXhwIjoxNTk1NDM4MTY0LCJpYXQiOjE1OTU0MDkzNjQsImV4cGlyZXNfaW4iOjI4ODAwLCJqdGkiOiI4MTc1ZDJjNy0yODgxLTQzZjktOWZhZC1lNTQ5ZTZjMDhjNzUifQ.n53QPbw60N3RB76a-yJjG4j6p28jiamX7wj7WJPlLVmPYhMssQLLOW4-ddsZe3jfgm-nhzYjd6teb6bChwXFKCkQFJaEO9ZfE72nWQH934UBiDxD9UMLeglxySlNoKoCDWrBO2lQcuaMCWSlKrQHxBywG_Ubj3Gee3LheXloUN5kWgQyPlYEsQisWCB4uUzfqle1DdyZjOeSxnmAxK19tbpcQwb65YjRe1nsaphQAhcI3TWBtlUSyYth-RlumbIPuCowWhDULH6ekLxeqFz3x9_UkK2qYi8o5J1zsOZZKhg6Voy6Bt_uCwZ5wE2modbkt98MKB18aS2UuZGtAnviAg")
            .when()
            .get("am/role-assignments?roleType=case&actorId=123e4567-e89b-42d3-a456-556642445612")
            .andReturn();
        response.then().assertThat().statusCode(HttpStatus.NOT_FOUND.value());
        //.body("message", Matchers.equalTo("No Assignment records found for given criteria"));
    }
}
