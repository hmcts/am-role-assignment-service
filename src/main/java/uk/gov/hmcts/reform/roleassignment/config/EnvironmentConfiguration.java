package uk.gov.hmcts.reform.roleassignment.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
@Getter
public class EnvironmentConfiguration {

    private final String environment;

    @Autowired
    public EnvironmentConfiguration(@Value("${launchdarkly.sdk.environment}") String launchDarklyEnvironment,
                                    @Value("${ras.environment}") String rasEnvironment) {

        // NB: use legacy LD value if still supplied: but raise a warning
        if (StringUtils.isNotEmpty(launchDarklyEnvironment)) {
            this.environment =  launchDarklyEnvironment;
            log.warn("launchdarkly.sdk.environment used value: " + launchDarklyEnvironment
                + ".  Please switch to `RAS_ENV` environment variable instead of `LAUNCH_DARKLY_ENV`.");
        } else {
            this.environment = rasEnvironment;
            log.info("ras.environment used value: " + rasEnvironment);
        }

    }

}
