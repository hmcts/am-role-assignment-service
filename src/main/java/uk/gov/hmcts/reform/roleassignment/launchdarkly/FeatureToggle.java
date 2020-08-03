package uk.gov.hmcts.reform.roleassignment.launchdarkly;

import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.server.LDClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FeatureToggle {
    @Autowired
    private final LDClient ldClient;

    @Value("${launchdarkly.sdk.environment}")
    private String environment;

    private final String userName;

    @Autowired
    public FeatureToggle(LDClient ldClient, @Value("${launchdarkly.sdk.user}") String userName) {
        this.ldClient = ldClient;
        this.userName = userName;
    }

    public boolean isFlagEnabled(String serviceName, String flag) {

        LDUser user = new LDUser.Builder(environment)
            .firstName(userName)
            .lastName("user")
            .custom("servicename", serviceName)
            .build();

        return ldClient.boolVariation(flag, user, false);
    }
}
