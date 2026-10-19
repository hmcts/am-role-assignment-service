package uk.gov.hmcts.reform.roleassignment.oidc;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.OAuth2Configuration;
import uk.gov.hmcts.reform.idam.client.models.TokenRequest;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.UnauthorizedException;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.roleassignment.util.Constants.BEARER;

@Component
@Slf4j
public class IdamRepository {

    private IdamApi idamApi;
    private OIdcAdminConfiguration oidcAdminConfiguration;
    private OAuth2Configuration oauth2Configuration;

    @Value("${spring.cache.type}")
    protected String cacheType;

    private CacheManager cacheManager;


    @Autowired
    public IdamRepository(IdamApi idamApi,
                          OIdcAdminConfiguration oidcAdminConfiguration,
                          OAuth2Configuration oauth2Configuration,
                          CacheManager cacheManager) {
        this.idamApi = idamApi;
        this.oidcAdminConfiguration = oidcAdminConfiguration;
        this.oauth2Configuration = oauth2Configuration;
        this.cacheManager = cacheManager;
    }

    @Cacheable(value = "token")
    public UserInfo getUserInfo(String jwtToken) {
        if (cacheType != null && !cacheType.equals("none")) {
            var caffeineCache = (CaffeineCache) cacheManager.getCache("token");
            com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache = requireNonNull(caffeineCache)
                .getNativeCache();
            log.info("generating Bearer Token, current size of cache: {}", nativeCache.estimatedSize());
        }
        try {
            return idamApi.retrieveUserInfo(BEARER + jwtToken);
        } catch (FeignException feignException) {
            log.error("FeignException Unauthorized: retrieve user info ", feignException);
            throw new UnauthorizedException("User is not authorized", feignException);
        }
    }

    @Cacheable(value = "userToken")
    public String getManageUserToken(String userId) {
        if (cacheType != null && !cacheType.equals("none")) {
            var caffeineCache = (CaffeineCache) cacheManager.getCache("userToken");
            com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache = requireNonNull(caffeineCache)
                .getNativeCache();
            log.info("Generating system user Token, current size of cache: {}", nativeCache.estimatedSize());
        }
        var tokenRequest = new TokenRequest(
            oauth2Configuration.getClientId(),
            oauth2Configuration.getClientSecret(),
            "password",
            "",
            userId,
            oidcAdminConfiguration.getSecret(),
            oidcAdminConfiguration.getScope(),
            "4",
            ""
        );
        var tokenResponse = idamApi.generateOpenIdToken(tokenRequest);
        return tokenResponse.accessToken;
    }

}
