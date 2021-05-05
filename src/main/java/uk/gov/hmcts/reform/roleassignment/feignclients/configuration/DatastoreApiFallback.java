package uk.gov.hmcts.reform.roleassignment.feignclients.configuration;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.roleassignment.feignclients.DataStoreApi;
import uk.gov.hmcts.reform.roleassignment.domain.model.Case;

@Component
public class DatastoreApiFallback implements DataStoreApi {

    public static final String DATA_STORE_NOT_AVAILABLE = "The data store Service is not available";

    @Override
    public String getServiceStatus() {
        return DATA_STORE_NOT_AVAILABLE;
    }

    @Override
    public Case getCaseDataV2(String caseId) {
        return Case.builder().id(caseId)
            .caseTypeId("Asylum")
            .jurisdiction("IA")
            .build();
    }
}
