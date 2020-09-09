package uk.gov.hmcts.reform.roleassignment;

import com.hazelcast.config.EvictionPolicy;
import org.springframework.beans.factory.annotation.Value;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;

@Named
@Singleton
public class ApplicationParams {

    @Value("${audit.log.enabled:true}")
    private boolean auditLogEnabled;

    @Value("#{'${audit.log.ignore.statues}'.split(',')}")
    private List<Integer> auditLogIgnoreStatuses;

    @Value("${user.cache.ttl.secs}")
    private Integer userCacheTTLSecs;

    @Value("${role.cache.max.size}")
    private Integer roleCacheMaxSize;

    @Value("${role.cache.eviction.policy}")
    private EvictionPolicy roleCacheEvictionPolicy;


    @Value("${role.cache.max-idle.secs}")
    private Integer roleCacheMaxIdleSecs;

    public boolean isAuditLogEnabled() {
        return auditLogEnabled;
    }

    public List<Integer> getAuditLogIgnoreStatuses() {
        return auditLogIgnoreStatuses;
    }

    public Integer getUserCacheTTLSecs() {
        return userCacheTTLSecs;
    }

    public int getRoleCacheMaxSize() {
        return roleCacheMaxSize;
    }

    public EvictionPolicy getRoleCacheEvictionPolicy() {
        return roleCacheEvictionPolicy;
    }

    public int getRoleCacheMaxIdleSecs() {
        return roleCacheMaxIdleSecs;
    }
}
