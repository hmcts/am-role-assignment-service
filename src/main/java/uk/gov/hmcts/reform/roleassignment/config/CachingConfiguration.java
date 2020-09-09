package uk.gov.hmcts.reform.roleassignment.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizeConfig;
import com.hazelcast.config.NetworkConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.roleassignment.ApplicationParams;

@Configuration
public class CachingConfiguration {

    private final ApplicationParams applicationParams;

    public CachingConfiguration(ApplicationParams applicationParams) {
        this.applicationParams = applicationParams;
    }

    @Bean
    public Config hazelCastConfig() {

        Config config = new Config();
        NetworkConfig networkConfig = config.setInstanceName("hazelcast-instance-am-role-assignment")
            .getNetworkConfig();
        networkConfig.getJoin().getMulticastConfig().setEnabled(false);
        networkConfig.getJoin().getTcpIpConfig().setEnabled(false);
        configCaches(config);
        return config;
    }

    private void configCaches(Config config) {
        final int definitionCacheMaxIdle = applicationParams.getRoleCacheMaxIdleSecs();
        config.addMapConfig(newMapConfigWithMaxIdle("userRoles", definitionCacheMaxIdle));
        config.addMapConfig(newMapConfigWithMaxIdle("userInfoCache", applicationParams.getUserCacheTTLSecs()));

    }

    private MapConfig newMapConfigWithMaxIdle(final String name, final Integer maxIdle) {
        return newMapConfig(name).setMaxIdleSeconds(maxIdle);
    }

    private MapConfig newMapConfig(final String name) {
        return new MapConfig().setName(name)
            .setMaxSizeConfig(new MaxSizeConfig(
                applicationParams.getRoleCacheMaxSize(),
                MaxSizeConfig.MaxSizePolicy.PER_NODE
            ))
            .setEvictionPolicy(applicationParams.getRoleCacheEvictionPolicy());

    }
}
