package uk.gov.hmcts.reform.roleassignment.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;
import io.swagger.v3.oas.annotations.Hidden;

@RestController
@Hidden
public class SwaggerRedirectController {

    static final String SWAGGER_UI_URL = "/swagger-ui/index.html?url=";

    @GetMapping(value = "/swagger")
    public RedirectView swaggerRedirect() {
        return new RedirectView(SWAGGER_UI_URL, true, false);
    }
}
