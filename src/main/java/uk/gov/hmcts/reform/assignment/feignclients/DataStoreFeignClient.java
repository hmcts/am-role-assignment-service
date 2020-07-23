package uk.gov.hmcts.reform.assignment.feignclients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import uk.gov.hmcts.reform.assignment.domain.model.Case;
import uk.gov.hmcts.reform.assignment.feignclients.configuration.DatastoreFeignClientConfiguration;
import uk.gov.hmcts.reform.assignment.feignclients.configuration.DatastoreFeignClientFallback;

@FeignClient(value = "datastoreclient", url = "${feign.client.config.datastoreclient.url}",
             configuration = DatastoreFeignClientConfiguration.class,
             fallback = DatastoreFeignClientFallback.class)

public interface DataStoreFeignClient {

    @GetMapping(value = "/")
    public String getServiceStatus();

    @GetMapping(value = "/caseworkers/{uid}/jurisdictions/{jid}/case-types/{ctid}/cases/{cid}")
    public String getCaseDataV1(@PathVariable("uid") String uid, @PathVariable("jid") String jurisdictionId,
                                @PathVariable("ctid") String caseTypeId, @PathVariable("cid") String caseId);

    @GetMapping(value = "/cases/{caseId}", headers = "experimental=true")
    public Case getCaseDataV2(@PathVariable("caseId") String caseId);

}
