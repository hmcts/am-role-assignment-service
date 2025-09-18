package uk.gov.hmcts.reform.roleassignment.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springdoc.core.utils.Constants.SWAGGER_UI_URL;

@ExtendWith(MockitoExtension.class)
class SwaggerRedirectControllerTest {

    private final SwaggerRedirectController controller = new SwaggerRedirectController();

    @Test
    void swaggerRedirect() {
        var response = controller.swaggerRedirect();

        assertNotNull(response);
        assertTrue(response.isRedirectView());
        assertEquals(SWAGGER_UI_URL, response.getUrl());
    }
}
