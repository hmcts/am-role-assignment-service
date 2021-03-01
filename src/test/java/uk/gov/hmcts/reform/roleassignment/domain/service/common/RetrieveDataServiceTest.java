package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cache.CacheManager;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.feignclients.DataStoreFeignClient;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RetrieveDataServiceTest {

    private final DataStoreFeignClient dataStoreFeignClient = mock(DataStoreFeignClient.class);
    @Mock
    private CacheManager cacheManager = mock(CacheManager.class);

    private RetrieveDataService sut = new RetrieveDataService(dataStoreFeignClient, cacheManager);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void getCaseById() {
        when(dataStoreFeignClient.getCaseDataV2("1234")).thenReturn(TestDataBuilder.buildCase());
        assertNotNull(sut.getCaseById("1234", RoleAssignment.builder()
            .attributes(new HashMap<String, JsonNode>()).build()));
    }
}
