package uk.gov.hmcts.reform.roleassignment.launchdarkly;

import com.launchdarkly.sdk.server.LDClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.roleassignment.util.Constants;

@Configuration
@Slf4j
public class LDConfiguration {
    public static final String LDCONFIG = "ldconfig";

    @Value("${launchdarkly.sdk.environment}")
    private String environment;

    @Value("${launchdarkly.sdk.testkey}")
    private String sdkTestKey;

    @Value("${launchdarkly.sdk.prodkey}")
    private String sdkProdKey;

    @Bean
    public LDClient getLdClient() {
        String sdkKey;
        if (environment.equalsIgnoreCase(Constants.AAT) || environment.equalsIgnoreCase(Constants.PROD)) {
            sdkKey = sdkProdKey;
        } else {
            sdkKey = sdkTestKey;
        }
        return new LDClient(sdkKey);
    }
}
