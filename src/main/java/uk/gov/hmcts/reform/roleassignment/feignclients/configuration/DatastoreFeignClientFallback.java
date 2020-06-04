package uk.gov.hmcts.reform.roleassignment.feignclients.configuration;

import uk.gov.hmcts.reform.roleassignment.feignclients.DataStoreFeignClient;

public class DatastoreFeignClientFallback implements DataStoreFeignClient {

    @Override
    public String getServiceStatus() {
        return "Service is not available";
    }

    @Override
    public String getCaseDataV1(String uid, String jurisdictionId, String caseTypeId, String caseId) {
        return null;
    }

    @Override
    public String getCaseDataV2(String caseId) {
        return null;
    }
}
