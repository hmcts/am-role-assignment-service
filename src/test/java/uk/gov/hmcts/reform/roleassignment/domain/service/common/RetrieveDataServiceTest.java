package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.roleassignment.feignclients.DataStoreFeignClient;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RetrieveDataServiceTest {

    private final DataStoreFeignClient dataStoreFeignClient = mock(DataStoreFeignClient.class);

    private RetrieveDataService sut = new RetrieveDataService(dataStoreFeignClient);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void getCaseById() {
        when(dataStoreFeignClient.getCaseDataV2("1234")).thenReturn(TestDataBuilder.buildCase());
        assertNotNull(sut.getCaseById("1234"));
    }
}
