package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import uk.gov.hmcts.reform.roleassignment.feignclients.DataStoreApi;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
class RetrieveDataServiceTest {


    private final DataStoreApi dataStoreApi = mock(DataStoreApi.class);

    private CacheManager cacheManager = mock(CacheManager.class);

    private CaffeineCache caffeineCacheMock = mock(CaffeineCache.class);

    private com.github.benmanes.caffeine.cache.Cache cache = mock(com.github.benmanes.caffeine.cache.Cache.class);

    RetrieveDataService sut;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        sut = new RetrieveDataService(dataStoreApi, cacheManager);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getCaseById() {

        org.springframework.test.util.ReflectionTestUtils.setField(
            sut, "cacheType", "caseId");

        when(dataStoreApi.getCaseDataV2("1234")).thenReturn(TestDataBuilder.buildCase());
        when(cacheManager.getCache(anyString())).thenReturn(caffeineCacheMock);
        when(caffeineCacheMock.getNativeCache()).thenReturn(cache);
        when(cache.estimatedSize()).thenReturn(anyLong());

        assertNotNull(sut.getCaseById("1234"));
        verify(cacheManager, times(1)).getCache(any());
        verify(caffeineCacheMock, times(1)).getNativeCache();
        verify(cache, times(1)).estimatedSize();
    }

    @Test
    @SuppressWarnings("unchecked")
    void getCaseById_cache_none() {

        org.springframework.test.util.ReflectionTestUtils.setField(
            sut, "cacheType", "none");

        when(dataStoreApi.getCaseDataV2("1234")).thenReturn(TestDataBuilder.buildCase());

        assertNotNull(sut.getCaseById("1234"));
        verify(cacheManager, times(0)).getCache(any());
        verify(caffeineCacheMock, times(0)).getNativeCache();
        verify(cache, times(0)).estimatedSize();

    }
}
