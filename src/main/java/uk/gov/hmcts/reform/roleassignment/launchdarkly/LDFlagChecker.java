package uk.gov.hmcts.reform.roleassignment.launchdarkly;

import java.io.IOException;

import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.server.LDClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.roleassignment.util.Constants;

@Cacheable(cacheNames = "ldconfig")
@Configuration
@Slf4j
public class LDFlagChecker {

    @Value("${launchdarkly.sdk.environment}")
    private String environment;

    @Value("${launchdarkly.sdk.testkey}")
    private String sdkTestKey;

    @Value("${launchdarkly.sdk.prodkey}")
    private String sdkProdKey;

    public boolean verifyServiceAndFlag(String serviceName, String flagName) throws IOException {
        String sdkKey;
        if (environment.equalsIgnoreCase(Constants.AAT) || environment.equalsIgnoreCase(Constants.PROD)) {
            sdkKey = sdkProdKey;
        } else {
            sdkKey = sdkTestKey;
        }

        LDClient client = new LDClient(sdkKey);

        LDUser user = new LDUser.Builder(environment)
            .firstName(environment)
            .lastName("user")
            .custom("servicename", serviceName)
            .build();

        boolean showFeature = client.boolVariation(flagName, user, false);

        client.close();
        return showFeature;
    }
}
