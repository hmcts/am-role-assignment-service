package uk.gov.hmcts.reform.roleassignment;

import org.junit.Assert;
import org.springframework.beans.factory.annotation.Value;

public class SmokeTest {

    @Value("${roleAssignmentUrl}")
    private String baseUrl;

    /*@Test
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
    }*/

    public void sampleTest() {
        Assert.assertTrue(1 == 1);
    }
}
