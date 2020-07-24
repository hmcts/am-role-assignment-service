package uk.gov.hmcts.reform.roleassignment.launchdarkly;

import java.io.IOException;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.launchdarkly.sdk.server.LDClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LaunchDarklyConfiguration {

    private LDClient client;

    @Value("${launchdarkly.sdk.key}")
    private String sdkKey;

    @PostConstruct
    void launchDarkly() {
        client = new LDClient(sdkKey);
    }

    @Bean
    public LDClient ldClient() {
        return client;
    }

    @PreDestroy
    void close() throws IOException {
        client.flush();
        client.close();
    }
}
