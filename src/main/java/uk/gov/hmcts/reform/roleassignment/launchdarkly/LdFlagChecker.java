package uk.gov.hmcts.reform.roleassignment.launchdarkly;

import java.io.IOException;

import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.server.LDClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LdFlagChecker {

    @Value("${launchdarkly.sdk.key}")
    private String sdkKey;

    public boolean verifyServiceAndFlag(String serviceName, String flagName) throws IOException {

        String environment =
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
