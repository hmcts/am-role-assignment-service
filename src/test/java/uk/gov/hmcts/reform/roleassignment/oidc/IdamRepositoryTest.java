package uk.gov.hmcts.reform.roleassignment.oidc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.OAuth2Configuration;
import uk.gov.hmcts.reform.idam.client.models.TokenResponse;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
class IdamRepositoryTest {

    @Mock
    private final IdamClient idamClient = mock(IdamClient.class);

    @Mock
    private IdamApi idamApi = mock(IdamApi.class);

    @Mock
    private OIdcAdminConfiguration oidcAdminConfiguration = mock(OIdcAdminConfiguration.class);

    @Mock
    private OAuth2Configuration oauth2Configuration = mock(OAuth2Configuration.class);

    @InjectMocks
    IdamRepository idamRepository = new IdamRepository(idamClient, idamApi, oidcAdminConfiguration,
                                                       oauth2Configuration);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void getManageUserToken() {

        when(oauth2Configuration.getClientId()).thenReturn("clientId");
        when(oauth2Configuration.getClientSecret()).thenReturn("secret");
        when(oidcAdminConfiguration.getUserId()).thenReturn("userid");
        when(oidcAdminConfiguration.getPassword()).thenReturn("password");
        when(oidcAdminConfiguration.getScope()).thenReturn("scope");
        TokenResponse tokenResponse = new
            TokenResponse("a","1","1","a", "v","v");
        when(idamApi.generateOpenIdToken(any())).thenReturn(tokenResponse);

        assertNotNull(idamRepository.getManageUserToken());
    }
}
