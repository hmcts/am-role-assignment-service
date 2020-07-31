package uk.gov.hmcts.reform.roleassignment.launchdarkly;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class LDCacheConfig {
    public static final String LDCONFIG = "ldconfig";

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(LDCONFIG);
    }
}
