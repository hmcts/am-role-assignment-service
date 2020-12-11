package uk.gov.hmcts.reform.roleassignment.health;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.boot.actuate.health.Health;
import org.springframework.web.client.RestTemplate;

public interface BaseHealthIndicator {

    default Health checkServiceHealth(RestTemplate restTemplate, String url) {
        try {
            JsonNode resp = restTemplate.getForObject(url + "/health", JsonNode.class);
            if (resp.get("status").asText().equalsIgnoreCase("UP")) {
                return Health.up().build();
            }
        } catch (Exception ex) {
            return Health.down(ex).build();
        }
        return Health.down().build();
    }
}
