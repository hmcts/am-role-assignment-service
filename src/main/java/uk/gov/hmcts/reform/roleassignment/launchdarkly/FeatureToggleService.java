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
    public static final String URI_GET_ASSIGNMENTS_BY_ACTOR_ID = "/am/role-assignments/actors/";
    public static final String LD_FLAG_GET_ROLE_ASSIGNMENTS_BY_ACTOR_ID = "get-role-assignments-by-actor-id";
    public static final String URI_DELETE_ASSIGNMENTS_BY_ID = "/am/role-assignments/";
    public static final String LD_FLAG_DELETE_ROLE_ASSIGNMENTS_BY_ID = "delete-role-assignments-by-id";
    public static final String LD_FLAG_GET_ASSIGNMENTS_BY_QUERY_PARAMS = "get-assignments-by-query-params";

    @Autowired
    private final LDClient ldClient;

    @Value("${launchdarkly.sdk.environment}")
    private String environment;

    private final String userName;
    private static final HashMap<String, String> getRequestMap = new HashMap<>();
    private static final HashMap<String, String> postRequestMap = new HashMap<>();
    private static final HashMap<String, String> deleteRequestMap = new HashMap<>();

    public static final String AM_ROLE_ASSIGNMENTS = "/am/role-assignments";
    public static final String QUERY_REQUEST_ROLE_ASSIGNMENTS = "/am/role-assignments/query";

    static {
        //Get Map
        getRequestMap.put("/am/role-assignments/ld/endpoint","get-ld-flag");
        getRequestMap.put("/am/role-assignments/roles","get-list-of-roles");
        //Post Map
        postRequestMap.put(AM_ROLE_ASSIGNMENTS, "create-role-assignments");
        postRequestMap.put(QUERY_REQUEST_ROLE_ASSIGNMENTS, "search-by-query");
        //Delete Map
        deleteRequestMap.put(AM_ROLE_ASSIGNMENTS, "delete-role-assignments");
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
    public boolean isFlagEnabled(String flagName) {
        LDUser user = new LDUser.Builder(environment)
            .firstName(userName)
            .lastName(USER)
            .custom(SERVICE_NAME, "am_role_assignment_service")
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
                } else if (uri.contains(URI_GET_ASSIGNMENTS_BY_ACTOR_ID)) {
                    return LD_FLAG_GET_ROLE_ASSIGNMENTS_BY_ACTOR_ID;
                } else if (uri.contains(AM_ROLE_ASSIGNMENTS)) {
                    return LD_FLAG_GET_ASSIGNMENTS_BY_QUERY_PARAMS;
                }
                break;
            case POST:
                if (postRequestMap.get(uri) != null) {
                    return postRequestMap.get(uri);
                }
                break;
            case DELETE:
                if (deleteRequestMap.get(uri) != null) {
                    return deleteRequestMap.get(uri);
                } else if (uri.contains(URI_DELETE_ASSIGNMENTS_BY_ID)) {
                    return LD_FLAG_DELETE_ROLE_ASSIGNMENTS_BY_ID;
                }
                break;

            default:
        }
        return null;
    }
}
