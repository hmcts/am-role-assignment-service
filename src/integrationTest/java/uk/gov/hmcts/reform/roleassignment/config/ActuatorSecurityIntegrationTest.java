package uk.gov.hmcts.reform.roleassignment.config;

import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.roleassignment.BaseTest;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@TestPropertySource(properties = {"ras.environment=pr"})
class ActuatorSecurityIntegrationTest extends BaseTest {

    private MockMvc mockMvc;

    @Inject
    private WebApplicationContext wac;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    void shouldAllowAnonymousHealthEndpoint() throws Exception {
        MvcResult result = mockMvc.perform(get("/health"))
            .andReturn();

        int status = result.getResponse().getStatus();
        assertTrue(status == 200 || status == 503);
    }

    @Test
    void shouldNotExposeLoggersEndpointAnonymously() throws Exception {
        MvcResult result = mockMvc.perform(get("/loggers"))
            .andReturn();

        int status = result.getResponse().getStatus();
        assertTrue(status == 401 || status == 404);
    }
}