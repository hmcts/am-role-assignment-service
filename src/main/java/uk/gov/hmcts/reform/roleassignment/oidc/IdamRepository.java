package uk.gov.hmcts.reform.roleassignment.oidc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.OAuth2Configuration;
import uk.gov.hmcts.reform.idam.client.models.TokenRequest;
import uk.gov.hmcts.reform.idam.client.models.TokenResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import static org.springframework.http.HttpMethod.GET;

@Component
@Slf4j
public class IdamRepository {

    private final IdamClient idamClient;
    private IdamApi idamApi;
    private OIdcAdminConfiguration oidcAdminConfiguration;
    private OAuth2Configuration oauth2Configuration;
    private RestTemplate restTemplate;
    @Value("${idam.api.url}")
    protected String idamUrl;

    @Autowired
    public IdamRepository(IdamClient idamClient,
                          IdamApi idamApi,
                          OIdcAdminConfiguration oidcAdminConfiguration,
                          OAuth2Configuration oauth2Configuration, RestTemplate restTemplate) {
        this.idamClient = idamClient;
        this.idamApi = idamApi;
        this.oidcAdminConfiguration = oidcAdminConfiguration;
        this.oauth2Configuration = oauth2Configuration;
        this.restTemplate = restTemplate;
    }

    public UserInfo getUserInfo(String jwtToken) {
        return idamClient.getUserInfo("Bearer " + jwtToken);
    }

    public UserDetails getUserByUserId(String jwtToken, String userId) {
        return idamClient.getUserByUserId("Bearer " + jwtToken, userId);
    }

    public ResponseEntity<Object> searchUserByUserId(String jwtToken, String userId) {
        ResponseEntity<Object> responseResult = new ResponseEntity<>(HttpStatus.OK);
        try {
            final HttpEntity<?> requestEntity = new HttpEntity<>(getHttpHeaders(jwtToken));
            String searchUserByUserIdUrl = String.format("%s/api/v1/users?query=%s", idamUrl, userId);
            log.info("searchUserByUserIdUrl : {}", searchUserByUserIdUrl);
            ResponseEntity<Object> response = restTemplate.exchange(
                searchUserByUserIdUrl,
                GET,
                requestEntity,
                Object.class
            );
            if (HttpStatus.OK.equals(response.getStatusCode())) {
                responseResult = response;
            }
        } catch (HttpClientErrorException exception) {
            log.info(exception.getMessage());
        }
        return responseResult;
    }

    private static HttpHeaders getHttpHeaders(String jwtToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken);
        return headers;
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
        log.info(tokenRequest.toString());
        TokenResponse tokenResponse = idamApi.generateOpenIdToken(tokenRequest);
        log.info(tokenResponse.accessToken);
        return tokenResponse.accessToken;
    }

}
