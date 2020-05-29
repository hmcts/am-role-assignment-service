/*
package uk.gov.hmcts.reform.roleassignment.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class WelcomeTest {
    private transient WelcomeController welcomeController = new WelcomeController();

    @Test
    public void shouldReturnWelcomeMessage() {
        ResponseEntity<String> caseDocumentControllerResponse = welcomeController.welcome();
        assertNotNull(caseDocumentControllerResponse, "No Response from WelcomeController");
        assertEquals(HttpStatus.OK, caseDocumentControllerResponse.getStatusCode(), "Status code is NOT OK");
        assertEquals("Welcome to Role Assignment Service Controller", caseDocumentControllerResponse.getBody(),
                     "Response body does not have expected value"
                    );
    }
}
*/
