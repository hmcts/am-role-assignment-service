package uk.gov.hmcts.reform.roleassignment.health;


import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthContributor;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class IdamServiceHealthIndicator implements HealthIndicator, HealthContributor {

    private RestTemplate restTemplate;
    @Value("${idam.api.url:}") String URL;

    @Autowired
    public IdamServiceHealthIndicator(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Health health() {
        try {
            JsonNode resp = restTemplate.getForObject(URL + "/health", JsonNode.class);
            if (resp.get("status").asText().equalsIgnoreCase("UP")) {
                return Health.up().build();
            }
        } catch (Exception ex) {
            return Health.down(ex).build();
        }
        return Health.down().build();
    }
}
