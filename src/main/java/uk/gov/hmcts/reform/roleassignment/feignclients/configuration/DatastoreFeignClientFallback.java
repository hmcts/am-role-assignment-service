package uk.gov.hmcts.reform.roleassignment.feignclients.configuration;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.roleassignment.domain.model.Case;
import uk.gov.hmcts.reform.roleassignment.feignclients.DataStoreFeignClient;

@Component
public class DatastoreFeignClientFallback implements DataStoreFeignClient {

    public static final String DATA_STORE_NOT_AVAILABLE = "The data store Service is not available";

    @Override
    public String getServiceStatus() {
        return DATA_STORE_NOT_AVAILABLE;
    }

    @Override
    public String getCaseDataV1(String uid, String jurisdictionId, String caseTypeId, String caseId) {
        return DATA_STORE_NOT_AVAILABLE;
    }

    @Override
    public Case getCaseDataV2(String caseId) {
        return Case.builder().id(caseId)
            .caseTypeId("sample").build();
    }
}
