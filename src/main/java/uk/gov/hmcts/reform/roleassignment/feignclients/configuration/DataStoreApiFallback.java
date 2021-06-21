package uk.gov.hmcts.reform.roleassignment.feignclients.configuration;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignment.domain.model.Case;
import uk.gov.hmcts.reform.roleassignment.feignclients.DataStoreApi;

@Service
public class DataStoreApiFallback implements DataStoreApi {

    public static final String DATA_STORE_NOT_AVAILABLE = "The data store Service is not available";

    @Override
    public String getServiceStatus() {
        return DATA_STORE_NOT_AVAILABLE;
    }

    @Override
    public Case getCaseDataV2(String caseId) {
        Case dummyCase = null;

        if (caseId.equals("1234567890123456")){
            dummyCase = Case.builder().id(caseId)
                .caseTypeId("CARE_SUPERVISION_EPO")
                .jurisdiction("PUBLICLAW")
                .build();
        } else {
            dummyCase = Case.builder().id(caseId)
                .caseTypeId("Asylum")
                .jurisdiction("IA")
                .build();
        }
        return dummyCase;
    }
}
