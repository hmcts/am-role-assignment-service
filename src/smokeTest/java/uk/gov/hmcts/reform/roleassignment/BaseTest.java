/*
package uk.gov.hmcts.reform.roleassignment;

import feign.Feign;
import feign.jackson.JacksonEncoder;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.ServiceAuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.TokenRequest;
import uk.gov.hmcts.reform.idam.client.models.TokenResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

@Service
public class BaseTest {

    @Autowired
    private IdamClient idamClient;
    @Autowired
    private IdamApi idamApi;

    @Value("${client.id}")
    private String clientId;
    @Value("${client.secret}")
    private String clientSecret;
    @Value("${client.redirectUri}")
    private String redirectUri;

    @Value("${user.username}")
    private String username;
    @Value("${user.password}")
    private String password;
    @Value("${user.scope}")
    private  String scope;

    public ServiceAuthorisationApi generateServiceAuthorisationApi(final String s2sUrl) {
        return Feign.builder()
                    .encoder(new JacksonEncoder())
                    .contract(new SpringMvcContract())
                    .target(ServiceAuthorisationApi.class, s2sUrl);
    }

    public ServiceAuthTokenGenerator authTokenGenerator(
        final String secret,
        final String microService,
        final ServiceAuthorisationApi serviceAuthorisationApi) {
        return new ServiceAuthTokenGenerator(secret, microService, serviceAuthorisationApi);
    }

    public UserInfo getUserInfo(String jwtToken) {
        return idamClient.getUserInfo("Bearer " + jwtToken);
    }

    public UserDetails getUserByUserId(String jwtToken, String userId) {
        return idamClient.getUserByUserId("Bearer " + jwtToken, userId);
    }

    public String getManageUserToken() {
        TokenRequest tokenRequest = new TokenRequest(
            clientId,
            clientSecret,
            "password",
            redirectUri,
            username,
            password,
            scope,
            "4",
            ""
        );
        TokenResponse tokenResponse = idamApi.generateOpenIdToken(tokenRequest);
        return tokenResponse.accessToken;
    }
}
*/
