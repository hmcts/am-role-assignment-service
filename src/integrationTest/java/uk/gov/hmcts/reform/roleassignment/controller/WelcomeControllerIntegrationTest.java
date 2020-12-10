package uk.gov.hmcts.reform.roleassignment.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.roleassignment.BaseTest;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

public class WelcomeControllerIntegrationTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(WelcomeControllerIntegrationTest.class);
    private static final String COUNT_RECORDS = "SELECT count(1) as n FROM role_assignment_request";
    private static final String GET_STATUS = "SELECT status FROM role_assignment_request where id = ?";
    private static final String REQUEST_ID = "077dc12a-02ba-4238-87c3-803ca26b515f";

    @Autowired
    private DataSource ds;

    private transient MockMvc mockMvc;

    private JdbcTemplate template;

    private static final MediaType JSON_CONTENT_TYPE = new MediaType(
        MediaType.APPLICATION_JSON.getType(),
        MediaType.APPLICATION_JSON.getSubtype(),
        StandardCharsets.UTF_8
    );

    @Autowired
    private transient WelcomeController welcomeController;

    @Before
    public void setUp() {
        this.mockMvc = standaloneSetup(this.welcomeController).build();
        template = new JdbcTemplate(ds);
        MockitoAnnotations.initMocks(this);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.createObjectNode();
        when(restTemplate.getForObject("url", JsonNode.class)).thenReturn(rootNode);
    }

    @Test
    public void welcomeApiTest() throws Exception {
        final String url = "/welcome";
        logger.info(" WelcomeControllerIntegrationTest : Inside  Welcome API Test method...{}", url);
        final MvcResult result = mockMvc.perform(get(url).contentType(JSON_CONTENT_TYPE))
            .andExpect(status().is(200))
            .andReturn();
        assertEquals(
            "Welcome service status", 200, result.getResponse().getStatus());
        assertEquals(
            "Welcome service message", "welcome to role assignment service", result.getResponse().getContentAsString());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
         scripts = {"classpath:sql/insert_role_assignment_request.sql"})
    public void shouldGetRecordCountFromRequestTable() {
        final int count = template.queryForObject(COUNT_RECORDS, Integer.class);
        logger.info(" Total number of records fetched from role assignment request table...{}", count);
        assertEquals(
            "role_assignment_request record count ", 5, count);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
         scripts = {"classpath:sql/insert_role_assignment_request.sql"})
    public void shouldGetRequestStatusFromRequestTable() throws Exception {
        final Object[] parameters = new Object[] {
            REQUEST_ID
        };
        String status = template.queryForObject(GET_STATUS, parameters, String.class);
        logger.info(" Role assignment request status is...{}", status);
        assertEquals(
            "Role assignment request status", "APPROVED", status);
    }

}
