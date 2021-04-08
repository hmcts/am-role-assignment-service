package uk.gov.hmcts.reform.roleassignment.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.roleassignment.BaseTest;
import uk.gov.hmcts.reform.roleassignment.annotations.FeatureFlagToggle;
import uk.gov.hmcts.reform.roleassignment.domain.model.QueryRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentResource;
import uk.gov.hmcts.reform.roleassignment.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.roleassignment.util.Constants;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder.createQueryRequest;

public class RoleAssignmentIntegrationTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(RoleAssignmentIntegrationTest.class);
    private static final String COUNT_HISTORY_RECORDS_QUERY = "SELECT count(1) as n FROM role_assignment_history";

    private static final String GET_ASSIGNMENT_STATUS_QUERY = "SELECT actor_id FROM role_assignment where id = ?";
    private static final String ACTOR_ID = "123e4567-e89b-42d3-a456-556642445612";
    public static final String ROLE_ASSIGNMENT_ID = "2ef8ebf3-266e-45d3-a3b8-4ce1e5d93b9f";
    private MockMvc mockMvc;

    @Rule
    public FeatureFlagToggleEvaluator featureFlagToggleEvaluator = new FeatureFlagToggleEvaluator();
    private JdbcTemplate template;

    @MockBean
    private FeatureToggleService featureConditionEvaluator;

    @Inject
    private WebApplicationContext wac;

    @Autowired
    private DataSource ds;

    @Before
    public void setUp() {
        template = new JdbcTemplate(ds);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @FeatureFlagToggle(flagEnabled = true)
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/insert_role_assignment_request.sql",
        "classpath:sql/insert_role_assignment_history.sql"
    })
    public void shouldGetRecordCountFromHistoryTable() {
        final int count = template.queryForObject(COUNT_HISTORY_RECORDS_QUERY, Integer.class);
        logger.info(" Total number of records fetched from role assignment history table...{}", count);
        assertEquals(
            "role_assignment_history record count ", 15, count);
    }

    @Test
    @FeatureFlagToggle(flagEnabled = false)
    public void disableTestAsPerFlagValue() {
        assertRoleAssignmentRecordSize();
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_role_assignment.sql"})
    public void shouldGetRecordsFromRoleAssignmentTable() {
        assertRoleAssignmentRecordSize();
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts =
        {"classpath:sql/insert_role_assignment.sql",
            "classpath:sql/insert_actor_cache_control.sql"})
    public void shouldGetRoleAssignmentsBasedOnActorId() throws Exception {
        assertRoleAssignmentRecordSize();
        final String url = "/am/role-assignments/actors/" + ACTOR_ID;

        final MvcResult result = mockMvc.perform(get(url)
                                                     .contentType(JSON_CONTENT_TYPE)
                                                     .headers(getHttpHeaders()))
            .andExpect(status().is(200))
            .andReturn();

        RoleAssignmentResource response = mapper.readValue(
            result.getResponse().getContentAsString(),
            RoleAssignmentResource.class
        );

        assertNotNull(response.getRoleAssignmentResponse());
        if (!response.getRoleAssignmentResponse().isEmpty()) {
            assertEquals(1, response.getRoleAssignmentResponse().size());
            assertEquals(
                ROLE_ASSIGNMENT_ID,
                response.getRoleAssignmentResponse().get(0).getId().toString()
            );
            assertEquals(
                ACTOR_ID,
                response.getRoleAssignmentResponse().get(0).getActorId().toString()
            );
        }
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
                                                     .param("actorId", ACTOR_ID)
        )
            .andExpect(status().is(200))
            .andReturn();
        String responseAsString = result.getResponse().getContentAsString();

        List<RoleAssignment> response = mapper.readValue(responseAsString, new TypeReference<>() {
        });

        assertNotNull(response);
        if (!response.isEmpty()) {
            assertEquals(1, response.size());
            assertEquals(
                ROLE_ASSIGNMENT_ID,
                response.get(0).getId().toString()
            );
            assertEquals(
                ACTOR_ID,
                response.get(0).getActorId().toString()
            );
        }
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

        List<RoleAssignment> response = mapper.readValue(responseAsString, new TypeReference<>() {
        });
        assertNotNull(response);
        if (!response.isEmpty()) {
            assertEquals(1, response.size());
            assertEquals(
                ROLE_ASSIGNMENT_ID,
                response.get(0).getId().toString()
            );
            assertEquals(
                ACTOR_ID,
                response.get(0).getActorId().toString()
            );
        }
    }

    @Test
    public void shouldGetListOfRolesOrmJrdFlagEnabled() throws Exception {
        final String url = "/am/role-assignments/roles";

        doReturn(true).when(featureConditionEvaluator)
            .isFlagEnabled(Constants.ORM_SERVICE_NAME, Constants.ORM_JRD_ORG_ROLE_FLAG);

        final MvcResult result = mockMvc.perform(get(url)
                                                     .contentType(MediaType.APPLICATION_JSON)
                                                     .headers(getHttpHeaders())
        )
            .andExpect(status().is(200))
            .andReturn();
        String response = result.getResponse().getContentAsString();

        JsonNode jsonResponse = mapper.readValue(response, JsonNode.class);
        assertEquals(200, result.getResponse().getStatus());
        assertEquals(
            4,
            jsonResponse.size()
        );
        assertEquals(
            "salaried-judge",
            jsonResponse.get(3).get("name").asText()
        );
        assertEquals(
            "Judicial office holder able to do judicial case work",
            jsonResponse.get(3).get("description").asText()
        );
        assertEquals(
            "JUDICIAL",
            jsonResponse.get(3).get("category").asText()
        );
    }

    @Test
    public void shouldGetListOfRolesOrmJrdFlagDisabled() throws Exception {
        final String url = "/am/role-assignments/roles";

        doReturn(false).when(featureConditionEvaluator)
            .isFlagEnabled(Constants.ORM_SERVICE_NAME, Constants.ORM_JRD_ORG_ROLE_FLAG);

        final MvcResult result = mockMvc.perform(get(url)
                                                     .contentType(MediaType.APPLICATION_JSON)
                                                     .headers(getHttpHeaders())
        )
            .andExpect(status().is(200))
            .andReturn();
        String response = result.getResponse().getContentAsString();

        JsonNode jsonResponse = mapper.readValue(response, JsonNode.class);
        assertEquals(200, result.getResponse().getStatus());
        assertEquals(
            3,
            jsonResponse.size()
        );
        assertEquals(
            "judge",
            jsonResponse.get(0).get("name").asText()
        );
        assertEquals(
            "Judicial office holder able to do judicial case work",
            jsonResponse.get(0).get("description").asText()
        );
        assertEquals(
            "JUDICIAL",
            jsonResponse.get(0).get("category").asText()
        );
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_role_assignment.sql"})
    public void shouldGetRoleAssignmentsRecordsBasedOnDynamicQuery() throws Exception {

        QueryRequest queryRequest = QueryRequest.builder()
            .authorisations(Arrays.asList("dev"))
            .build();

        final String url = "/am/role-assignments/query";

        final MvcResult result = mockMvc.perform(post(url)
                                                     .contentType(JSON_CONTENT_TYPE)
                                                     .headers(getHttpHeaders())
                                                     .content(mapper.writeValueAsBytes(queryRequest))
        ).andExpect(status().is(200)).andReturn();


        String responseAsString = result.getResponse().getContentAsString();

        List<RoleAssignment> response = mapper.readValue(responseAsString, new TypeReference<>() {
        });

        assertNotNull(response);
        response.forEach(element -> assertAll(
            () -> assertEquals(element.getRoleType().toString(), "ORGANISATION"),
            () -> assertEquals(element.getRoleName(), "judge"),
            () -> assertEquals(element.getActorId(), "123e4567-e89b-42d3-a456-556642445614"),
            () -> assertEquals(element.getGrantType().toString(), "STANDARD"),
            () -> assertEquals(element.getClassification().toString(), "PUBLIC"),
            () -> assertEquals(element.getAuthorisations().size(), 1),
            () -> assertEquals(element.getAuthorisations().get(0), "dev")
        ));
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_role_assignment.sql"})
    public void shouldGetEmptyRoleAssignmentsRecordsBasedOnDynamicQuery() throws Exception {

        final String url = "/am/role-assignments/query";

        final MvcResult result = mockMvc.perform(post(url)
                                                     .contentType(JSON_CONTENT_TYPE)
                                                     .headers(getHttpHeaders())
                                                     .content(mapper.writeValueAsBytes(createQueryRequest()))
        ).andExpect(status().is(200)).andReturn();


        String responseAsString = result.getResponse().getContentAsString();

        List<RoleAssignment> response = mapper.readValue(responseAsString, new TypeReference<>() {
        });

        assertNotNull(response);
        assertEquals(response.size(), 0);

    }

    private void assertRoleAssignmentRecordSize() {
        final Object[] assignmentId = new Object[]{
            ROLE_ASSIGNMENT_ID
        };
        String actorId = template.queryForObject(GET_ASSIGNMENT_STATUS_QUERY, assignmentId, String.class);
        logger.info(" Role assignment actor id is...{}", actorId);
        assertEquals(
            "Role assignment actor Id", ACTOR_ID, actorId);
    }

    @NotNull
    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("ServiceAuthorization", "Bearer " + "1234");
        headers.add("Authorization", "Bearer " + "2345");
        return headers;
    }


}
