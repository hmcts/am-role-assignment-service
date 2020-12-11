package uk.gov.hmcts.reform.roleassignment.health;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.actuate.health.Health;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IdamServiceHealthIndicatorTest {

    private RestTemplate restTemplate = mock(RestTemplate.class);

    private IdamServiceHealthIndicator sut = new IdamServiceHealthIndicator(restTemplate);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void health() throws JsonProcessingException {
        String jsonString = "{\"status\": \"UP\"}";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode actualObj = mapper.readTree(jsonString);
        when(restTemplate.getForObject("url" + "/health", JsonNode.class)).thenReturn(actualObj);
        Health health1 = sut.checkServiceHealth(restTemplate, "url");
        assertNotNull(health1);
        //when(sut.health()).thenReturn(Health.up().build());
        Health health2 = sut.health();
        assertNotNull(health2);
        //assertNotNull(sut.checkServiceHealth(restTemplate,"url"));
    }
}
