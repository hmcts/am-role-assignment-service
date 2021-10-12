package uk.gov.hmcts.reform.roleassignment.befta.utils;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import uk.gov.hmcts.reform.idam.client.models.TokenRequest;
import uk.gov.hmcts.reform.roleassignment.util.EnvironmentVariableUtils;

@Getter
@Setter
@ToString
@Builder
public class UserTokenProviderConfig {

    String idamURL;
    String roleAssignmentUrl;

    String secret;
    String microService;
    String s2sUrl;

    String clientId;
    String clientSecret;
    String username;
    String password;
    String scope;
    static String MICRO_SERVICE_NAME = "am_role_assignment_service";
    static String USER_NAME = "TEST_AM_USER2_BEFTA@test.local";

    public UserTokenProviderConfig() {

        idamURL = EnvironmentVariableUtils.getRequiredVariable("IDAM_URL");
        roleAssignmentUrl = EnvironmentVariableUtils.getRequiredVariable("TEST_URL");
        secret = EnvironmentVariableUtils.getRequiredVariable("AM_ROLE_ASSIGNMENT_SERVICE_SECRET");
        microService = MICRO_SERVICE_NAME;
        s2sUrl = EnvironmentVariableUtils.getRequiredVariable("IDAM_S2S_URL");
        clientSecret = EnvironmentVariableUtils.getRequiredVariable("ROLE_ASSIGNMENT_IDAM_CLIENT_SECRET");
        clientId = EnvironmentVariableUtils.getRequiredVariable("IDAM_CLIENT_ID");
        username = USER_NAME;
        password = EnvironmentVariableUtils.getRequiredVariable("TEST_AM_USER2_BEFTA_PWD");
        scope = EnvironmentVariableUtils.getRequiredVariable("OPENID_SCOPE_VARIABLES");
    }


    public TokenRequest prepareTokenRequest() {

        return new TokenRequest(
            clientId,
            clientSecret,
            "password",
            "",
            username,
            password,
            "openid roles profile authorities",
            "4",
            ""
        );
    }

    public String getIdamURL() {
        return idamURL;
    }

    public String getRoleAssignmentUrl() {
        return roleAssignmentUrl;
    }

    public String getSecret() {
        return secret;
    }

    public String getMicroService() {
        return microService;
    }

    public String getS2sUrl() {
        return s2sUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getScope() {
        return scope;
    }

    public static String getMicroServiceName() {
        return MICRO_SERVICE_NAME;
    }

    public static String getUserName() {
        return USER_NAME;
    }


    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public void setMicroService(String microService) {
        this.microService = microService;
    }

}
