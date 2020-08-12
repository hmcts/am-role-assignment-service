package uk.gov.hmcts.reform.roleassignment.util;

import lombok.Getter;
import uk.gov.hmcts.reform.idam.client.models.TokenRequest;

@Getter
public class UserTokenProviderConfig {

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
    private final String microServiceName = "am_role_assignment_service";
    private final String userName = "befta.caseworker.2.solicitor.2@gmail.com";

    public UserTokenProviderConfig() {

        idamURL = EnvironmentVariableUtils.getRequiredVariable("IDAM_URL");
        roleAssignmentUrl = EnvironmentVariableUtils.getRequiredVariable("TEST_URL");
        secret = EnvironmentVariableUtils.getRequiredVariable("AM_ROLE_ASSIGNMENT_SERVICE_SECRET");
        microService = microServiceName;
        s2sUrl = EnvironmentVariableUtils.getRequiredVariable("IDAM_S2S_URL");

        clientId = EnvironmentVariableUtils.getRequiredVariable("IDAM_CLIENT_ID");
        clientSecret = EnvironmentVariableUtils.getRequiredVariable("IDAM_CLIENT_SECRET");
        username = userName;
        password = EnvironmentVariableUtils.getRequiredVariable("CCD_BEFTA_CASEWORKER_2_SOLICITOR_2_PWD");
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
