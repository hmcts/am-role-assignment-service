package uk.gov.hmcts.reform.roleassignment.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.InvalidRequest;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.roleassignment.data.DatabaseChangelogLockEntity;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PersistenceService;
import io.swagger.v3.oas.annotations.Hidden;

import static org.springdoc.core.Constants.SWAGGER_UI_URL;

@RestController
@Hidden
public class WelcomeController {

    @Autowired
    PersistenceService persistenceService;

    @GetMapping(value = "/swagger")
    public RedirectView swaggerRedirect() {
        return new RedirectView(SWAGGER_UI_URL, true, false);
    }


    @GetMapping("/exception/{type}")
    public ResponseEntity<String> getException(@PathVariable String type) {
        switch (type) {
            case "invalidRequest":
                throw new InvalidRequest("Invalid Request");
            case "resourceNotFoundException":
                throw new ResourceNotFoundException("Resource Not Found Exception");
            case "httpMessageConversionException":
                throw new HttpMessageConversionException("Http Message Conversion Exception");
            case "badRequestException":
                throw new BadRequestException("Bad Request Exception");
            default:
                return null;
        }

    }

    @GetMapping(value = "/welcome")
    public String welcome() {
        return "welcome to role assignment service";
    }

    @GetMapping("/db/releaselock")
    public ResponseEntity<DatabaseChangelogLockEntity> dbReleaseLock() {
        DatabaseChangelogLockEntity entity = persistenceService.releaseDatabaseLock(1);
        return ResponseEntity.ok(entity);
    }
}
