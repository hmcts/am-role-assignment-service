package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cache.CacheManager;
import uk.gov.hmcts.reform.roleassignment.feignclients.DataStoreApi;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RetrieveDataServiceTest {

    private final DataStoreApi dataStoreApi = mock(DataStoreApi.class);
    @Mock
    private CacheManager cacheManager = mock(CacheManager.class);

    private RetrieveDataService sut = new RetrieveDataService(dataStoreApi, cacheManager);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void getCaseById() {
        when(dataStoreApi.getCaseDataV2("1234")).thenReturn(TestDataBuilder.buildCase());
        assertNotNull(sut.getCaseById("1234"));
    }
}
