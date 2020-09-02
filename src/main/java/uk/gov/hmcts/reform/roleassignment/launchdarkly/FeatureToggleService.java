package uk.gov.hmcts.reform.roleassignment.launchdarkly;

import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.server.LDClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Service
public class FeatureToggleService {

    @Autowired
    private final LDClient ldClient;

    @Value("${launchdarkly.sdk.environment}")
    private String environment;

    private final String userName;
    private Map<String, String> launchDarklyMap;

    @Autowired
    public FeatureToggleService(LDClient ldClient, @Value("${launchdarkly.sdk.user}") String userName) {
        this.ldClient = ldClient;
        this.userName = userName;
    }

    @PostConstruct
    public void mapServiceToFlag() {
        launchDarklyMap = new HashMap<>();
        launchDarklyMap.put("/am/role-assignments/ld/endpoint", "get-ld-flag");
    }

    public boolean isFlagEnabled(String serviceName, String flagName) {
        LDUser user = new LDUser.Builder(environment)
            .firstName(userName)
            .lastName("user")
            .custom("servicename", serviceName)
            .build();

        return ldClient.boolVariation(flagName, user, false);
    }

    public Map<String, String> getLaunchDarklyMap() {
        return launchDarklyMap;
    }
}
