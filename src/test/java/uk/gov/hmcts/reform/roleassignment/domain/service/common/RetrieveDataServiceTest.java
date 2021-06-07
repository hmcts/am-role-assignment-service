package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.roleassignment.feignclients.DataStoreApi;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;
import com.github.benmanes.caffeine.cache.Cache;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RetrieveDataServiceTest {

    private final DataStoreApi dataStoreApi = mock(DataStoreApi.class);

    private final CacheManager cacheManager = mock(CacheManager.class);

    private final CaffeineCache caffeineCache = mock(CaffeineCache.class);

    @Mock
    Cache<Object, Object> nativeCache;

    @InjectMocks
    private final RetrieveDataService sut = new RetrieveDataService(dataStoreApi, cacheManager);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void getCaseById() {
        when(dataStoreApi.getCaseDataV2("1234")).thenReturn(TestDataBuilder.buildCase());
        assertNotNull(sut.getCaseById("1234"));
    }

    @Test
    void getCaseById_withValidCache() {
        ReflectionTestUtils.setField(sut, "cacheType", "caseId");
        doReturn(caffeineCache).when(cacheManager).getCache(anyString());
        when(caffeineCache.getNativeCache()).thenReturn(nativeCache);
        when(dataStoreApi.getCaseDataV2("1234")).thenReturn(TestDataBuilder.buildCase());
        assertNotNull(sut.getCaseById("1234"));
    }

    @Test
    void getCaseById_withInValidCache() {
        ReflectionTestUtils.setField(sut, "cacheType", "none");
        when(dataStoreApi.getCaseDataV2("1234")).thenReturn(TestDataBuilder.buildCase());
        assertNotNull(sut.getCaseById("1234"));
    }
}
