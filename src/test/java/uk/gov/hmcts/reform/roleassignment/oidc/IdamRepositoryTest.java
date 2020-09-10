package uk.gov.hmcts.reform.roleassignment.oidc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.OAuth2Configuration;
import uk.gov.hmcts.reform.idam.client.models.TokenResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
class IdamRepositoryTest {

    @Mock
    private IdamApi idamApi = mock(IdamApi.class);

    @Mock
    private OIdcAdminConfiguration oidcAdminConfiguration = mock(OIdcAdminConfiguration.class);

    @Mock
    private OAuth2Configuration oauth2Configuration = mock(OAuth2Configuration.class);

    @Mock
    private RestTemplate restTemplate = mock(RestTemplate.class);

    @InjectMocks
    IdamRepository idamRepository = new IdamRepository(idamApi, oidcAdminConfiguration,
                                                       oauth2Configuration, restTemplate);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void getUserInfo() {
        UserInfo userInfo = new UserInfo("1", "2", "3", "4", "5", List.of("6","7"));
        when(idamApi.retrieveUserInfo(anyString())).thenReturn(userInfo);
        UserInfo returnedUserInfo = idamRepository.getUserInfo("Test");
        assertNotNull(returnedUserInfo);
    }

    @Test
    void getUserRolesBlankResponse() throws IOException {
        String userId = "003352d0-e699-48bc-b6f5-5810411e60af";
        UserDetails userDetails = UserDetails.builder().email("black@betty.com").forename("ram").surname("jam").id(
            "1234567890123456")
            .roles(null).build();

        when(idamApi.getUserByUserId(any(), any())).thenReturn(userDetails);

        assertNotNull(idamRepository.getUserByUserId("Test", userId));
    }

    @Test
    void getManageUserToken() {

        when(oauth2Configuration.getClientId()).thenReturn("clientId");
        when(oauth2Configuration.getClientSecret()).thenReturn("secret");
        when(oidcAdminConfiguration.getUserId()).thenReturn("userid");
        when(oidcAdminConfiguration.getPassword()).thenReturn("password");
        when(oidcAdminConfiguration.getScope()).thenReturn("scope");
        TokenResponse tokenResponse = new
            TokenResponse("a", "1", "1", "a", "v", "v");
        when(idamApi.generateOpenIdToken(any())).thenReturn(tokenResponse);

        String result = idamRepository.getManageUserToken();

        assertNotNull(result);
        assertFalse(result.isBlank());
        assertFalse(result.isEmpty());
    }

    @Test
    void shouldThrowNullPointerException() {

        String token = "eyJhbGciOiJIUzUxMiJ9.Eim7hdYejtBbWXnqCf1gntbYpWHRX8BRzm4zIC_oszmC3D5QlNmkIetVPcMINg";
        String userId = "4dc7dd3c-3fb5-4611-bbde-5101a97681e0";


        doThrow(NullPointerException.class)
            .when(restTemplate)
            .exchange(anyString(), any(), any(), (Class<?>) any(Class.class));

        assertThrows(NullPointerException.class, () -> idamRepository.searchUserByUserId(token, userId));


    }
}
