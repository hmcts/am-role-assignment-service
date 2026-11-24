package uk.gov.hmcts.reform.roleassignment.oidc;

import feign.FeignException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.OAuth2Configuration;
import uk.gov.hmcts.reform.idam.client.models.TokenResponse;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.UnauthorizedException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdamRepositoryTest {

    @Mock
    private IdamApi idamApi = mock(IdamApi.class);

    @Mock
    private OIdcAdminConfiguration oidcAdminConfiguration = mock(OIdcAdminConfiguration.class);

    @Mock
    private OAuth2Configuration oauth2Configuration = mock(OAuth2Configuration.class);

    @Mock
    private CacheManager cacheManager = mock(CacheManager.class);

    IdamRepository idamRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        idamRepository = new IdamRepository(idamApi,
                                            oidcAdminConfiguration,
                                            oauth2Configuration,
                                            cacheManager);
        ReflectionTestUtils.setField(
            idamRepository,
            "cacheType", ""

        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void getUserInfo() {
        UserInfo userInfo = mock(UserInfo.class);
        CaffeineCache caffeineCacheMock = mock(CaffeineCache.class);
        var cache = mock(com.github.benmanes.caffeine.cache.Cache.class);

        when(idamApi.retrieveUserInfo(anyString())).thenReturn(userInfo);
        when(cacheManager.getCache(anyString())).thenReturn(caffeineCacheMock);
        when(caffeineCacheMock.getNativeCache()).thenReturn(cache);
        when(cache.estimatedSize()).thenReturn(anyLong());

        UserInfo returnedUserInfo = idamRepository.getUserInfo("Test");
        assertNotNull(returnedUserInfo);
        verify(idamApi, times(1)).retrieveUserInfo(any());
        verify(cacheManager, times(1)).getCache(any());
        verify(caffeineCacheMock, times(1)).getNativeCache();
        verify(cache, times(1)).estimatedSize();

    }

    @Test
    void getUserInfo_cacheNone() {
        UserInfo userInfo = mock(UserInfo.class);


        ReflectionTestUtils.setField(
            idamRepository,
            "cacheType", "none"

        );

        when(idamApi.retrieveUserInfo(anyString())).thenReturn(userInfo);

        UserInfo returnedUserInfo = idamRepository.getUserInfo("Test");
        assertNotNull(returnedUserInfo);
        verify(idamApi, times(1)).retrieveUserInfo(any());
        verify(cacheManager, times(0)).getCache(any());

        CaffeineCache caffeineCacheMock = mock(CaffeineCache.class);
        var cache = mock(com.github.benmanes.caffeine.cache.Cache.class);

        verify(caffeineCacheMock, times(0)).getNativeCache();
        verify(cache, times(0)).estimatedSize();

    }

    @Test
    void getUserInfo_cacheNull() {
        UserInfo userInfo = mock(UserInfo.class);


        ReflectionTestUtils.setField(
            idamRepository,
            "cacheType", null

        );

        when(idamApi.retrieveUserInfo(anyString())).thenReturn(userInfo);

        UserInfo returnedUserInfo = idamRepository.getUserInfo("Test");
        assertNotNull(returnedUserInfo);
        verify(idamApi, times(1)).retrieveUserInfo(any());
        verify(cacheManager, times(0)).getCache(any());

        CaffeineCache caffeineCacheMock = mock(CaffeineCache.class);
        var cache = mock(com.github.benmanes.caffeine.cache.Cache.class);

        verify(caffeineCacheMock, times(0)).getNativeCache();
        verify(cache, times(0)).estimatedSize();

    }

    @Test
    void getUserInfo_HandleHttpResponse() {
        ReflectionTestUtils.setField(
            idamRepository,
            "cacheType", "none"

        );
        when(idamApi.retrieveUserInfo(any())).thenThrow(FeignException.Unauthorized.class);

        Assertions.assertThrows(UnauthorizedException.class, () ->
            idamRepository.getUserInfo("Bearer invalid")
        );
    }

    @Test
    void getUserInfo_BadRequest() {
        ReflectionTestUtils.setField(
            idamRepository,
            "cacheType", "none"

        );
        when(idamApi.retrieveUserInfo(any())).thenThrow(FeignException.BadRequest.class);

        Assertions.assertThrows(UnauthorizedException.class, () ->
            idamRepository.getUserInfo("Bearer invalid")
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void getManageUserToken() {
        CaffeineCache caffeineCacheMock = mock(CaffeineCache.class);
        var cache = mock(com.github.benmanes.caffeine.cache.Cache.class);

        when(cacheManager.getCache(anyString())).thenReturn(caffeineCacheMock);
        when(caffeineCacheMock.getNativeCache()).thenReturn(cache);
        when(cache.estimatedSize()).thenReturn(1L);

        when(oauth2Configuration.getClientId()).thenReturn("clientId");
        when(oauth2Configuration.getClientSecret()).thenReturn("secret");
        when(oidcAdminConfiguration.getSecret()).thenReturn("password");
        when(oidcAdminConfiguration.getScope()).thenReturn("scope");
        TokenResponse tokenResponse = new
            TokenResponse("a", "1", "1", "a", "v", "v");
        when(idamApi.generateOpenIdToken(any())).thenReturn(tokenResponse);

        String result = idamRepository.getManageUserToken("123");

        assertNotNull(result);
        assertFalse(result.isBlank());
        assertFalse(result.isEmpty());
    }

    @Test
    void getManageUserToken_cacheNone() {

        ReflectionTestUtils.setField(
            idamRepository,
            "cacheType", "none"

        );

        when(oauth2Configuration.getClientId()).thenReturn("clientId");
        when(oauth2Configuration.getClientSecret()).thenReturn("secret");
        when(oidcAdminConfiguration.getSecret()).thenReturn("password");
        when(oidcAdminConfiguration.getScope()).thenReturn("scope");
        TokenResponse tokenResponse = new
            TokenResponse("a", "1", "1", "a", "v", "v");
        when(idamApi.generateOpenIdToken(any())).thenReturn(tokenResponse);

        String result = idamRepository.getManageUserToken("123");

        assertNotNull(result);
        assertFalse(result.isBlank());
        assertFalse(result.isEmpty());
        verify(cacheManager, times(0)).getCache(any());

        CaffeineCache caffeineCacheMock = mock(CaffeineCache.class);
        var cache = mock(com.github.benmanes.caffeine.cache.Cache.class);

        verify(caffeineCacheMock, times(0)).getNativeCache();
        verify(cache, times(0)).estimatedSize();
    }

    @Test
    void getManageUserToken_cacheNull() {

        ReflectionTestUtils.setField(
            idamRepository,
            "cacheType", null
        );

        when(oauth2Configuration.getClientId()).thenReturn("clientId");
        when(oauth2Configuration.getClientSecret()).thenReturn("secret");
        when(oidcAdminConfiguration.getSecret()).thenReturn("password");
        when(oidcAdminConfiguration.getScope()).thenReturn("scope");
        TokenResponse tokenResponse = new
            TokenResponse("a", "1", "1", "a", "v", "v");
        when(idamApi.generateOpenIdToken(any())).thenReturn(tokenResponse);

        String result = idamRepository.getManageUserToken("123");

        assertNotNull(result);
        assertFalse(result.isBlank());
        assertFalse(result.isEmpty());
        verify(cacheManager, times(0)).getCache(any());

        CaffeineCache caffeineCacheMock = mock(CaffeineCache.class);
        var cache = mock(com.github.benmanes.caffeine.cache.Cache.class);

        verify(caffeineCacheMock, times(0)).getNativeCache();
        verify(cache, times(0)).estimatedSize();
    }

    @Test
    @SuppressWarnings("unchecked")
    void getUserInfoException() {
        UserInfo userInfo = mock(UserInfo.class);
        CaffeineCache caffeineCacheMock = mock(CaffeineCache.class);
        var cache = mock(com.github.benmanes.caffeine.cache.Cache.class);

        when(idamApi.retrieveUserInfo(anyString())).thenReturn(userInfo);
        when(cacheManager.getCache(anyString())).thenReturn(caffeineCacheMock);
        when(caffeineCacheMock.getNativeCache()).thenReturn(cache);
        FeignException.Unauthorized unauthorized = mock(FeignException.Unauthorized.class);
        when(idamRepository.getUserInfo("invalid token")).thenThrow(unauthorized);

        assertThrows(UnauthorizedException.class, () -> idamRepository.getUserInfo("invalid token"));
    }

}
