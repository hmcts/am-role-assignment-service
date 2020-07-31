package uk.gov.hmcts.reform.roleassignment;

import lombok.Getter;
import uk.gov.hmcts.reform.idam.client.models.TokenRequest;

@Getter
public class UserTokenProviderConfig {

    public static final UserTokenProviderConfig DEFAULT_INSTANCE = new UserTokenProviderConfig();

    private final String idamURL;
    private final String roleAssignmentUrl;
    private final String secret;
    private final String microService;
    private final String s2sUrl;

    private final String clientId;
    private final String clientSecret;
    private final String username;
    private final String password;
    private final String scope;
    private static final String MICROSERVICE_NAME = "am_role_assignment_service";
    private static final String USER_NAME = "befta.caseworker.2.solicitor.2@gmail.com";

    public UserTokenProviderConfig() {

        idamURL = EnvironmentVariableUtils.getRequiredVariable("IDAM_URL");
        roleAssignmentUrl = EnvironmentVariableUtils.getRequiredVariable("TEST_URL");
        secret = EnvironmentVariableUtils.getRequiredVariable("AM_ROLE_ASSIGNMENT_SERVICE_SECRET");
        microService = MICROSERVICE_NAME;
        s2sUrl = EnvironmentVariableUtils.getRequiredVariable("IDAM_S2S_URL");

        clientId = EnvironmentVariableUtils.getRequiredVariable("IDAM_CLIENT_ID");
        clientSecret = EnvironmentVariableUtils.getRequiredVariable("ROLE_ASSIGNMENT_IDAM_CLIENT_SECRET");
        username = USER_NAME;
        password = EnvironmentVariableUtils.getRequiredVariable("ROLE_ASSIGNMENT_IDAM_ADMIN_PASSWORD");
        scope = EnvironmentVariableUtils.getRequiredVariable("OPENID_SCOPE_VARIABLES");
    }


    public TokenRequest prepareTokenRequest() {

        TokenRequest tokenRequest = new TokenRequest(
            clientId,
            clientSecret,
            "password",
            "",
            username,
            password,
            scope,
            "4",
            ""
        );
        return tokenRequest;
    }
}
