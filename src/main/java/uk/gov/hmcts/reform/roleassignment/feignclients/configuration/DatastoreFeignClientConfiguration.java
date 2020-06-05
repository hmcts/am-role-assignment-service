package uk.gov.hmcts.reform.roleassignment.feignclients.configuration;

import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.roleassignment.feignclients.exceptionhandler.DatastoreFeignExceptionHandler;

@Configuration
public class DatastoreFeignClientConfiguration {
    @Bean
    public OkHttpClient client() {
        return new OkHttpClient();
    }

    @Bean
    public DatastoreFeignExceptionHandler errorDecoder() {
        return new DatastoreFeignExceptionHandler();
    }

}
