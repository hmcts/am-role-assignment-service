package uk.gov.hmcts.reform.roleassignment.util;

import com.launchdarkly.sdk.LDUser;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
@Getter
@Setter
@Slf4j
public class LDEventListener implements CommandLineRunner {

    @Autowired
    FeatureFlagListener featureFlagListener;

    @Autowired
    FeatureToggleService featureToggleService;

    @Value("${launchdarkly.sdk.environment}")
    private String environment;




    private Map<String, Boolean> droolFlagStates = new HashMap<>();

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
            log.info("Flag: {} has been initialised with value: {}", flag.getValue(),
                     droolFlagStates.get(flag.getValue()));
        }
    }

    public Map<String, Boolean> getDroolFlagStates() {
        return droolFlagStates;
    }
}
