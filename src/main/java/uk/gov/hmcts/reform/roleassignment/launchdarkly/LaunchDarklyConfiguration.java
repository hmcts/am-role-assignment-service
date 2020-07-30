package uk.gov.hmcts.reform.roleassignment.launchdarkly;

import java.io.IOException;
import java.time.Duration;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.launchdarkly.sdk.server.Components;
import com.launchdarkly.sdk.server.LDClient;
import com.launchdarkly.sdk.server.LDConfig;
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
        LDConfig config = new LDConfig.Builder()
            .http(Components.httpConfiguration()
                          .connectTimeout(Duration.ofSeconds(3))
                          .socketTimeout(Duration.ofSeconds(3)))
            .events(Components.sendEvents()
                          .flushInterval(Duration.ofSeconds(1)))
            .build();
        client = new LDClient(sdkKey, config);
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
