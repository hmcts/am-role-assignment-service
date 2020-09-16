package uk.gov.hmcts.reform.roleassignment.launchdarkly;

import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.server.LDClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
public class FeatureToggleService {

    public static final String USER = "user";
    public static final String SERVICENAME = "servicename";
    @Autowired
    private final LDClient ldClient;

    @Value("${launchdarkly.sdk.environment}")
    private String environment;

    private final String userName;

    @Autowired
    public FeatureToggleService(LDClient ldClient, @Value("${launchdarkly.sdk.user}") String userName) {
        this.ldClient = ldClient;
        this.userName = userName;
    }

    public boolean isFlagEnabled(String serviceName, String flagName) {
        LDUser user = new LDUser.Builder(environment)
            .firstName(userName)
            .lastName(USER)
            .custom(SERVICENAME, serviceName)
            .build();

        return ldClient.boolVariation(flagName, user, false);
    }

    public boolean isValidFlag(String flagName) {
        return ldClient.isFlagKnown(flagName);
    }

    public String getLaunchDarklyFlag(HttpServletRequest request) {
        String uri = request.getRequestURI();
        switch (request.getMethod()) {
            case "GET":
                if (uri.contains("/am/role-assignments/actors/")) {
                    return "get-role-assignments-by-actor-id";
                }
                if (uri.contains("/am/role-assignments/ld/endpoint")) {
                    return "get-ld-flag";
                } else if (uri.equalsIgnoreCase("/am/role-assignments/roles")) {
                    return "get-list-of-roles";
                } else if (uri.equalsIgnoreCase("/am/role-assignments")) {
                    return "get-assignments-by-case-actor-id";
                }
                break;
            case "POST":
                if (uri.equalsIgnoreCase("/am/role-assignments")) {
                    return "create-role-assignments";
                }
                break;
            case "DELETE":
                if (uri.equalsIgnoreCase("/am/role-assignments")) {
                    return "delete-role-assignments";
                } else if (uri.contains("am/role-assignments/")) {
                    return "delete-role-assignments-by-id";
                }
                break;

            default:
        }
        return null;
    }
}
