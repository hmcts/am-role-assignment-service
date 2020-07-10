
package uk.gov.hmcts.reform.roleassignment1.domain.service.common;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignment1.domain.model.Case;
import uk.gov.hmcts.reform.roleassignment1.feignclients.DataStoreFeignClient;

@Service
public class RetrieveDataService {
    //1. getting required case details(ccd data store)
    //2. getting required role details(some static reference data??)
    //3. getting required ticket details(Authorization table in JRD)
    //4. getting some location reference data


    private final DataStoreFeignClient dataStoreFeignClient;


    public RetrieveDataService(DataStoreFeignClient dataStoreFeignClient) {
        this.dataStoreFeignClient = dataStoreFeignClient;
    }


    public Case getCaseById(String caseId) {

        return dataStoreFeignClient.getCaseDataV2(caseId);


    }
}
