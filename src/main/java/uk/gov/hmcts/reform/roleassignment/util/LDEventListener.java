package uk.gov.hmcts.reform.roleassignment.util;

import com.launchdarkly.sdk.LDUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.roleassignment.launchdarkly.FeatureFlagEnum;
import uk.gov.hmcts.reform.roleassignment.launchdarkly.FeatureFlagListener;
import uk.gov.hmcts.reform.roleassignment.launchdarkly.FeatureToggleService;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.roleassignment.launchdarkly.FeatureToggleService.SERVICE_NAME;

@Component
public class LDEventListener implements CommandLineRunner {


    private FeatureFlagListener featureFlagListener;

    private FeatureToggleService featureToggleService;

    @Value("${launchdarkly.sdk.environment}")
    private String environment;


    private Map<String, Boolean> droolFlagStates = new HashMap<>();

    public LDEventListener(FeatureFlagListener featureFlagListener, FeatureToggleService featureToggleService) {
        this.featureFlagListener = featureFlagListener;
        this.featureToggleService = featureToggleService;
    }

    @Override
    public void run(String... args) throws Exception {
        LDUser user = new LDUser.Builder(environment)
            .firstName("ras")
            .lastName("am")
            .custom(SERVICE_NAME, "am_role_assignment_service")
            .build();

        for (FeatureFlagEnum flag : FeatureFlagEnum.values()) {

            droolFlagStates.put(flag.getValue(), featureToggleService.isFlagEnabled(flag.getValue()));
            featureFlagListener.logWheneverOneFlagChangesForOneUser(flag.getValue(), user);

        }

    }

    public Map<String, Boolean> getDroolFlagStates() {
        return droolFlagStates;
    }
}
