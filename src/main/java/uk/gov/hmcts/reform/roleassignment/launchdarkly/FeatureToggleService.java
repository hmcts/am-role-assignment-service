package uk.gov.hmcts.reform.roleassignment.launchdarkly;

import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.server.LDClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

@Service
public class FeatureToggleService {

    public static final String USER = "user";
    public static final String SERVICE_NAME = "servicename";
    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String DELETE = "DELETE";

    @Autowired
    private final LDClient ldClient;

    @Value("${launchdarkly.sdk.environment}")
    private String environment;

    private final String userName;
    private static final HashMap<String, String> getRequestMap = new HashMap<>();
    private static final HashMap<String, String> postRequestMap = new HashMap<>();

    static {
        //Any new end point need to be placed in respective map.
        getRequestMap.put("/am/role-assignments/fetchFlagStatus","get-db-drools-flag");
        postRequestMap.put("/am/role-assignments/createFeatureFlag","get-db-drools-flag");
    }

    @Autowired
    public FeatureToggleService(LDClient ldClient, @Value("${launchdarkly.sdk.user}") String userName) {
        this.ldClient = ldClient;
        this.userName = userName;
    }

    public boolean isFlagEnabled(String serviceName, String flagName) {
        LDUser user = new LDUser.Builder(environment)
            .firstName(userName)
            .lastName(USER)
            .custom(SERVICE_NAME, serviceName)
            .build();

        return ldClient.boolVariation(flagName, user, false);
    }

    public boolean isValidFlag(String flagName) {
        return ldClient.isFlagKnown(flagName);
    }

    public String getLaunchDarklyFlag(HttpServletRequest request) {
        String uri = request.getRequestURI();
        switch (request.getMethod()) {
            case GET:
                if (getRequestMap.get(uri) != null) {
                    return getRequestMap.get(uri);
                }
                break;
            case POST:
                if (postRequestMap.get(uri) != null) {
                    return postRequestMap.get(uri);
                }
                break;
            case DELETE:
                break;
            default:
        }
        return null;
    }
}
