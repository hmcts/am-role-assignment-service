package uk.gov.hmcts.reform.roleassignment.launchdarkly;

import java.util.Date;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableCaching
@EnableScheduling
@Slf4j
public class LDCacheConfig {
    public static final String LDCONFIG = "ldconfig";

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(LDCONFIG);
    }

    @CacheEvict(allEntries = true, value = {LDCONFIG})
    @Scheduled(fixedDelayString = "${launchdarkly.sdk.fixedDelayString}")
    public void reportCacheEvict() {
        log.info("Refreshing launch darkly configuration at " + (new Date()));
    }
}
