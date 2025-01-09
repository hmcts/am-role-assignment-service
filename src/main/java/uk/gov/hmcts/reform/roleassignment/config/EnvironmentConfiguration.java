package uk.gov.hmcts.reform.roleassignment.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
@Getter
public class EnvironmentConfiguration {
    private String environment;

    @Autowired
    public EnvironmentConfiguration(@Value("${launchdarkly.sdk.environment}") String launchDarklyEnvironment,
                                    @Value("${ras.environment}") String rasEnvironment) {
        if (StringUtils.isNotEmpty(rasEnvironment)) {
            this.environment = rasEnvironment;
            log.info("ras.environment used value: " + rasEnvironment);
        } else {
            this.environment =  launchDarklyEnvironment;
            log.info("launchdarkly.sdk.environment used value: " + launchDarklyEnvironment);
        }

    }

}
