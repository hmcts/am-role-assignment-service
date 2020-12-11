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

class CcdDataStoreHealthIndicatorTest {

    private RestTemplate restTemplate = mock(RestTemplate.class);

    private CcdDataStoreHealthIndicator sut = new CcdDataStoreHealthIndicator(restTemplate);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void health() throws JsonProcessingException {
        String jsonString = "{\"status\": \"UP\"}";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode actualObj = mapper.readTree(jsonString);
        when(sut.health()).thenReturn(Health.up().build());
        when(restTemplate.getForObject("any_url",JsonNode.class)).thenReturn(actualObj);
        Health health = sut.health();
        assertNotNull(sut.checkServiceHealth(restTemplate,"url"));
    }
}
