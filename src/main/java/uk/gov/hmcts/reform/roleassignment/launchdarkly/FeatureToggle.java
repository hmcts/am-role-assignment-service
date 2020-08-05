package uk.gov.hmcts.reform.roleassignment.launchdarkly;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;

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
    private Map<String, String> launchDarklyMap;

    @Autowired
    public FeatureToggle(LDClient ldClient, @Value("${launchdarkly.sdk.user}") String userName) {
        this.ldClient = ldClient;
        this.userName = userName;
    }

    @PostConstruct
    public void mapServiceToFlag() {
        launchDarklyMap = new HashMap<>();
        launchDarklyMap.put("/am/role-assignments/ld/endpoint1", "get-ld-flag");
        launchDarklyMap.put("/am/role-assignments/ld/endpoint2", "delete-by-assignment-id-flag");
    }

    public boolean isFlagEnabled(String serviceName, String flag) {

        LDUser user = new LDUser.Builder(environment)
            .firstName(userName)
            .lastName("user")
            .custom("servicename", serviceName)
            .build();

        return ldClient.boolVariation(flag, user, false);
    }

    public Map<String, String> getLaunchDarklyMap() {
        return launchDarklyMap;
    }
}
