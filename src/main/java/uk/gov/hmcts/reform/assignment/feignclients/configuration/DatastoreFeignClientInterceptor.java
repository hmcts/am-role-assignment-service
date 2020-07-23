package uk.gov.hmcts.reform.assignment.feignclients.configuration;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.assignment.util.SecurityUtils;

@Service
public class DatastoreFeignClientInterceptor {

    @Autowired
    SecurityUtils securityUtils;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
        };
    }
}
