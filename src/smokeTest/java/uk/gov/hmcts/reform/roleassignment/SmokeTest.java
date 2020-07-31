package uk.gov.hmcts.reform.roleassignment;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import net.serenitybdd.rest.SerenityRest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;

public class SmokeTest {

    @Value("${roleAssignmentUrl}")
    private String baseUrl;

    //@Test
    public void shouldGetHealthStatus() {
        RestAssured.baseURI = baseUrl;
        RestAssured.useRelaxedHTTPSValidation();
        Response response = SerenityRest
            .given()
            .relaxedHTTPSValidation()
            .when()
            .get("/health")
            .andReturn();
        response.then().assertThat().statusCode(HttpStatus.OK.value());
    }
}
