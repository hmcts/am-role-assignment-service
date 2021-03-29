package uk.gov.hmcts.reform.roleassignment.controller;

import com.launchdarkly.sdk.LDUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.InvalidRequest;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.roleassignment.data.DatabaseChangelogLockEntity;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.roleassignment.launchdarkly.FeatureFlagListener;
import uk.gov.hmcts.reform.roleassignment.launchdarkly.FeatureToggleService;

import java.util.concurrent.ExecutionException;

import static uk.gov.hmcts.reform.roleassignment.launchdarkly.FeatureToggleService.SERVICE_NAME;

@RestController
public class WelcomeController {

    @Autowired
    PersistenceService persistenceService;

    @Autowired
    FeatureToggleService featureToggleService;

    @Autowired
    FeatureFlagListener featureFlagListner;


    @GetMapping(value = "/swagger")
    public String index() {
        return "redirect:swagger-ui.html";
    }

    static{

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

        boolean flagStatus = featureToggleService.isFlagEnabled("get-list-of-roles");
        LDUser user = new LDUser.Builder("pr")
            .firstName("sdsd")
            .lastName("sdsd")
            .custom(SERVICE_NAME, "am_role_assignment_service")
            .build();
        featureFlagListner.logWheneverOneFlagChangesForOneUser("get-list-of-roles",user);
        featureFlagListner.logWheneverOneFlagChangesForOneUser("get-ld-flag",user);

        if(flagStatus) {
            return "welcome to role assignment service";
        }else{
            return "flag is disabled the feature";
        }
    }

    @GetMapping("/db/releaselock")
    public ResponseEntity<DatabaseChangelogLockEntity> dbReleaseLock() {
        DatabaseChangelogLockEntity entity = persistenceService.releaseDatabaseLock(1);

        return ResponseEntity.ok(entity);
    }
}
