package uk.gov.hmcts.reform.roleassignment.feignclients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import uk.gov.hmcts.reform.roleassignment.feignclients.configuration.DatastoreFeignClientConfiguration;
import uk.gov.hmcts.reform.roleassignment.feignclients.configuration.DatastoreFeignClientFallback;

@FeignClient(value = "datastoreclient", url = "http://localhost:4452",
             configuration = DatastoreFeignClientConfiguration.class,
             fallback = DatastoreFeignClientFallback.class)

public interface DataStoreFeignClient {

    @GetMapping(value = "/getCaseDetails")
    public String getServiceStatus();

/*    @RequestMapping(method = RequestMethod.GET, value = "/posts/{postId}", produces = "application/json")
    Post getPostById(@PathVariable("postId") Long postId);*/
}
