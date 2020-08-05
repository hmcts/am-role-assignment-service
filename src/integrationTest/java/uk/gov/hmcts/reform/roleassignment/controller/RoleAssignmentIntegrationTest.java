package uk.gov.hmcts.reform.roleassignment.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentResource;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RoleAssignmentIntegrationTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(RoleAssignmentIntegrationTest.class);
    private static final String COUNT_HISTORY_RECORDS_QUERY = "SELECT count(1) as n FROM role_assignment_history";

    private static final String GET_ASSIGNMENT_STATUS_QUERY = "SELECT actor_id FROM role_assignment where id = ?";

    private transient MockMvc mockMvc;

    private JdbcTemplate template;

    @Inject
    private WebApplicationContext wac;

    @Inject
    protected DataSource db;

    @Before
    public void setUp() {
        template = new JdbcTemplate(db);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/insert_role_assignment_request.sql",
        "classpath:sql/insert_role_assignment_history.sql"
    })
    public void shouldGetRecordCountFromHistoryTable() throws Exception {
        final int count = template.queryForObject(COUNT_HISTORY_RECORDS_QUERY, Integer.class);
        logger.info(" Total number of records fetched from role assignment history table...{}", count);
        assertEquals(
            "role_assignment_history record count ", 15, count);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_role_assignment.sql"})
    public void shouldGetRecordsFromRoleAssignmentTable() throws Exception {
        final Object[] parameters = new Object[]{
            "638e8e7a-7d7c-4027-9d53-ea4b1095eab1"
        };
        String actorId = template.queryForObject(GET_ASSIGNMENT_STATUS_QUERY, parameters, String.class);
        logger.info(" Role assignment actor id is...{}", actorId);
        assertEquals(
            "Role assignment actor Id", "123e4567-e89b-42d3-a456-556642445613", actorId);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts =
        {"classpath:sql/insert_role_assignment.sql",
            "classpath:sql/insert_actor_cache_control.sql"})
    public void shouldGetRoleAssignmentsBasedOnActorId() throws Exception {
        assertRoleAssignmentRecordSize();
        final String url = "/am/role-assignments/actors/123e4567-e89b-42d3-a456-556642445612";

        final MvcResult result = mockMvc.perform(get(url)
                                                     .contentType(JSON_CONTENT_TYPE)
                                                     .headers(getHttpHeaders()))
            .andExpect(status().is(200))
            .andReturn();

        RoleAssignmentResource response = mapper.readValue(
            result.getResponse().getContentAsString(),
            RoleAssignmentResource.class
        );
        assertEquals(200, result.getResponse().getStatus());
        assertEquals(
            "2ef8ebf3-266e-45d3-a3b8-4ce1e5d93b9f",
            response.getRoleAssignmentResponse().get(0).getId().toString()
        );
        assertEquals(
            "123e4567-e89b-42d3-a456-556642445612",
            response.getRoleAssignmentResponse().get(0).getActorId().toString()
        );
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_role_assignment.sql"})
    public void shouldGetRoleAssignmentsBasedOnRoleTypeAndActorId() throws Exception {
        assertRoleAssignmentRecordSize();
        final String url = "/am/role-assignments";

        final MvcResult result = mockMvc.perform(get(url)
                                                     .contentType(MediaType.APPLICATION_JSON)
                                                     .headers(getHttpHeaders())
                                                     .param("roleType", "case")
                                                     .param("actorId", "123e4567-e89b-42d3-a456-556642445612")
        )
            .andExpect(status().is(200))
            .andReturn();
        String responseAsString = result.getResponse().getContentAsString();

        List<RoleAssignment> response = Arrays.asList(mapper.readValue(responseAsString, RoleAssignment[].class));
        String contentAsString = result.getResponse().getContentAsString();
        assertEquals(200, result.getResponse().getStatus());
        assertEquals(
            "2ef8ebf3-266e-45d3-a3b8-4ce1e5d93b9f",
            response.get(0).getId().toString()
        );
        assertEquals(
            "123e4567-e89b-42d3-a456-556642445612",
            response.get(0).getActorId().toString()
        );
    }


    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_role_assignment.sql"})
    public void shouldGetRoleAssignmentsBasedOnRoleTypeAndCaseId() throws Exception {
        assertRoleAssignmentRecordSize();
        final String url = "/am/role-assignments";

        final MvcResult result = mockMvc.perform(get(url)
                                                     .contentType(MediaType.APPLICATION_JSON)
                                                     .headers(getHttpHeaders())
                                                     .param("roleType", "case")
                                                     .param("caseId", "1234567890123456")
        )
            .andExpect(status().is(200))
            .andReturn();
        String responseAsString = result.getResponse().getContentAsString();

        List<RoleAssignment> response = Arrays.asList(mapper.readValue(responseAsString, RoleAssignment[].class));
        String contentAsString = result.getResponse().getContentAsString();
        assertEquals(200, result.getResponse().getStatus());
        assertEquals(
            "2ef8ebf3-266e-45d3-a3b8-4ce1e5d93b9f",
            response.get(0).getId().toString()
        );
        assertEquals(
            "123e4567-e89b-42d3-a456-556642445612",
            response.get(0).getActorId().toString()
        );
    }

    @Test
    public void shouldGetListOfRoles() throws Exception {
        final String url = "/am/role-assignments/roles";

        final MvcResult result = mockMvc.perform(get(url)
                                                     .contentType(MediaType.APPLICATION_JSON)
                                                     .headers(getHttpHeaders())
        )
            .andExpect(status().is(200))
            .andReturn();
        String response = result.getResponse().getContentAsString();

        JsonNode jsonResonse = mapper.readValue(response, JsonNode.class);
        assertEquals(200, result.getResponse().getStatus());
        assertEquals(
            2,
            jsonResonse.size()
        );
        assertEquals(
            "judge",
            jsonResonse.get(0).get("name").asText()
        );
        assertEquals(
            "Judicial office holder able to do judicial case work",
            jsonResonse.get(0).get("description").asText()
        );
        assertEquals(
            "JUDICIAL",
            jsonResonse.get(0).get("category").asText()
        );
    }

    private void assertRoleAssignmentRecordSize() {
        final Object[] parameters = new Object[]{
            "2ef8ebf3-266e-45d3-a3b8-4ce1e5d93b9f"
        };
        String actorId = template.queryForObject(GET_ASSIGNMENT_STATUS_QUERY, parameters, String.class);
        logger.info(" Role assignment actor id is...{}", actorId);
        assertEquals(
            "Role assignment actor Id", "123e4567-e89b-42d3-a456-556642445612", actorId);
    }

    @NotNull
    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("ServiceAuthorization", "Bearer " + "1234");
        headers.add("Authorization", "Bearer " + "2345");
        return headers;
    }
}
