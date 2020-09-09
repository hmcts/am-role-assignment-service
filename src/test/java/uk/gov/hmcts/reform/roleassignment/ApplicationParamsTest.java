package uk.gov.hmcts.reform.roleassignment;

import com.hazelcast.config.EvictionPolicy;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ApplicationParamsTest {

    private ApplicationParams applicationParams = new ApplicationParams();

    @Test
     void shouldGetAuditLogEnabled() {
        List<Integer> statusCodes = Arrays.asList(404);
        ReflectionTestUtils.setField(applicationParams, "auditLogIgnoreStatuses", statusCodes);
        List<Integer> result = applicationParams.getAuditLogIgnoreStatuses();
        assertNotNull(result);
        assertThat(result).isEqualTo(statusCodes);

    }

    @Test
     void shouldCheckAuditLogEnabled() {
        ReflectionTestUtils.setField(applicationParams, "auditLogEnabled", true);
        Boolean result = applicationParams.isAuditLogEnabled();
        assertNotNull(result);
        assertThat(result).isEqualTo(Boolean.TRUE);

    }

    @Test
    void shouldCheckRoleCacheMaxSize() {
        ReflectionTestUtils.setField(applicationParams, "roleCacheMaxSize", 100);
        Integer result = applicationParams.getRoleCacheMaxSize();
        assertNotNull(result);
        assertThat(result).isEqualTo(100);

    }

    @Test
    void shouldCheckUserCacheTTL() {
        ReflectionTestUtils.setField(applicationParams, "userCacheTTLSecs", 1800);
        Integer result = applicationParams.getUserCacheTTLSecs();
        assertNotNull(result);
        assertThat(result).isEqualTo(1800);

    }

    @Test
    void shouldCheckRoleCacheMaxIdle() {
        ReflectionTestUtils.setField(applicationParams, "roleCacheMaxIdleSecs", 250000);
        Integer result = applicationParams.getRoleCacheMaxIdleSecs();
        assertNotNull(result);
        assertThat(result).isEqualTo(250000);

    }

    @Test
    void shouldCheckEvictionPolicy() {
        ReflectionTestUtils.setField(applicationParams, "roleCacheEvictionPolicy", EvictionPolicy.NONE);
        EvictionPolicy result = applicationParams.getRoleCacheEvictionPolicy();
        assertNotNull(result);
        assertThat(result).isEqualTo(EvictionPolicy.NONE);

    }


}
