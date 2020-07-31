package uk.gov.hmcts.reform.roleassignment.launchdarkly;

import java.io.IOException;

import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.server.LDClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@EnableCaching
@CacheConfig(cacheNames = {"ldconfig" })
@EnableScheduling
@Configuration
@Slf4j
public class LDFlagChecker {

    @Value("${launchdarkly.sdk.environment}")
    private String environment;

    @Autowired
    CacheManager cacheManager;

    @Autowired
    private LDConfiguration ldConfiguration;

    @Cacheable({ "ldconfig"})
    public boolean verifyServiceAndFlag(String serviceName, String flagName) throws IOException {

        LDUser user = new LDUser.Builder(environment)
            .firstName(environment)
            .lastName("user")
            .custom("servicename", serviceName)
            .build();

        boolean showFeature = ldConfiguration.getLdClient().boolVariation(flagName, user, false);

        ldConfiguration.getLdClient().close();
        return showFeature;
    }

    @Scheduled(fixedRate = 6000)
    @CacheEvict(allEntries = true, value = "ldconfig")
    public void evictAllCaches() {
        log.info("cleaning caches");
        cacheManager.getCacheNames()
                    .parallelStream()
                    .forEach(cacheName -> cacheManager.getCache(cacheName).clear());
    }
}
