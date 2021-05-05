package uk.gov.hmcts.reform.roleassignment.feignclients.configuration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.roleassignment.domain.model.Case;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(MockitoJUnitRunner.class)
class DatastoreFeignClientFallbackTest {

    @InjectMocks
    DatastoreApiFallback datastoreFeignClientFallback = new DatastoreApiFallback();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void getServiceStatus() {
        assertEquals("The data store Service is not available",
                     datastoreFeignClientFallback.getServiceStatus());
    }

    @Test
    void getCaseDataV2() {
        String caseId = "1234";
        Case myCase =  datastoreFeignClientFallback.getCaseDataV2(caseId);
        assertEquals("1234", myCase.getId());
        assertEquals("Asylum", myCase.getCaseTypeId());
        assertEquals("IA", myCase.getJurisdiction());
    }
}
