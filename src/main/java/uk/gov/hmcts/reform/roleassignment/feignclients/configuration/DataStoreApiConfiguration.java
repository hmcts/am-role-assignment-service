package uk.gov.hmcts.reform.roleassignment.feignclients.configuration;

import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataStoreApiConfiguration {
    @Bean
    public OkHttpClient client() {
        return new OkHttpClient();
    }
}
