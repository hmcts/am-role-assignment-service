package uk.gov.hmcts.reform.roleassignment.feignclients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import uk.gov.hmcts.reform.roleassignment.feignclients.configuration.DatastoreFeignClientConfiguration;
import uk.gov.hmcts.reform.roleassignment.feignclients.configuration.DatastoreFeignClientFallback;

@FeignClient(value = "datastoreclient", url = "http://localhost:4452",
             configuration = DatastoreFeignClientConfiguration.class,
             fallback = DatastoreFeignClientFallback.class)

public interface DataStoreFeignClient {

    @GetMapping(value = "/")
    public String getServiceStatus();

    @GetMapping(value = "/caseworkers/{uid}/jurisdictions/{jid}/case-types/{ctid}/cases/{cid}",
                produces = "application/json")
    public String getCaseData(@PathVariable("uid") String uid, @PathVariable("jid") String jurisdictionId,
                              @PathVariable("ctid") String caseTypeId, @PathVariable("cid") String caseId);
}
