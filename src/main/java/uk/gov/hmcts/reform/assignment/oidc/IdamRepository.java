package uk.gov.hmcts.reform.assignment.oidc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.OAuth2Configuration;
import uk.gov.hmcts.reform.idam.client.models.TokenRequest;
import uk.gov.hmcts.reform.idam.client.models.TokenResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

@Component
@Slf4j
public class IdamRepository {

    private final IdamClient idamClient;
    private IdamApi idamApi;
    private OIdcAdminConfiguration oidcAdminConfiguration;
    private OAuth2Configuration oauth2Configuration;


    @Autowired
    public IdamRepository(IdamClient idamClient,
                          IdamApi idamApi,
                          OIdcAdminConfiguration oidcAdminConfiguration,
                          OAuth2Configuration oauth2Configuration) {
        this.idamClient = idamClient;
        this.idamApi = idamApi;
        this.oidcAdminConfiguration = oidcAdminConfiguration;

        this.oauth2Configuration = oauth2Configuration;
    }

    public UserInfo getUserInfo(String jwtToken) {
        return idamClient.getUserInfo("Bearer " + jwtToken);
    }

    public UserDetails getUserByUserId(String jwtToken, String userId) {
        return idamClient.getUserByUserId("Bearer " + jwtToken, userId);
    }

    public String getManageUserToken() {
        TokenRequest tokenRequest = new TokenRequest(
            oauth2Configuration.getClientId(),
            oauth2Configuration.getClientSecret(),
            "password",
            "",
            oidcAdminConfiguration.getUserId(),
            oidcAdminConfiguration.getPassword(),
            oidcAdminConfiguration.getScope(),
            "4",
            ""
        );
        TokenResponse tokenResponse = idamApi.generateOpenIdToken(tokenRequest);
        return tokenResponse.accessToken;
    }

}
