package uk.gov.hmcts.reform.roleassignment.controller;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;
import uk.gov.hmcts.reform.roleassignment.util.UserTokenProviderConfig;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

import javax.inject.Inject;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RoleAssignmentCreateAndDeleteIntegrationTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(RoleAssignmentIntegrationTest.class);
    private static final String ACTOR_ID = "123e4567-e89b-42d3-a456-556642445612";
    private static final String COUNT_HISTORY_RECORDS_QUERY = "SELECT count(1) as n FROM role_assignment_history";
    private static final String COUNT_ASSIGNMENT_RECORDS_QUERY = "SELECT count(1) as n FROM role_assignment";
    private static final String GET_ACTOR_FROM_ASSIGNMENT_QUERY = "select actor_id from role_assignment where id in "
        + "(SELECT id from role_assignment_history where actor_id = ?)";
    private static final String GET_ASSIGNMENT_STATUS_QUERY = "SELECT status from role_assignment_history "
        + "where actor_id = ?";
    private transient MockMvc mockMvc;

    private JdbcTemplate template;

    @Inject
    private WebApplicationContext wac;

    UserTokenProviderConfig config;
    String accessToken;
    String serviceAuth;

    @Before
    public void setUp() {
        template = new JdbcTemplate(db);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();
        MockitoAnnotations.initMocks(this);

        config = new UserTokenProviderConfig();
        accessToken = searchUserByUserId(config);
        serviceAuth = authTokenGenerator(
                config.getSecret(),
                config.getMicroService(),
                generateServiceAuthorisationApi(config.getS2sUrl())
            ).generate();
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts =
        {"classpath:sql/role_assignment_clean_up.sql"})
    public void shouldCreateRoleAssignmentsWithReplaceExistingTrue() throws Exception {
        logger.info(" History record count before create assignment request {}", getHistoryRecordsCount());
        logger.info(" LIVE table record count before create assignment request {}", getAssignmentRecordsCount());
        AssignmentRequest assignmentRequest = TestDataBuilder.createRoleAssignmentRequest(
            false, false);
        logger.info(" assignmentRequest :  {}", mapper.writeValueAsString(assignmentRequest));
        final String url = "/am/role-assignments";

        mockMvc.perform(post(url)
                            .contentType(JSON_CONTENT_TYPE)
                            .headers(getHttpHeaders())
                            .content(mapper.writeValueAsBytes(assignmentRequest))
        ).andExpect(status().is(201)).andReturn();

        logger.info(" -- Role Assignment record created successfully -- ");
        List<String> statusList = getStatusFromHistory();
        assertEquals(3, statusList.size());
        assertEquals("CREATED", statusList.get(0).toString());
        assertEquals("APPROVED", statusList.get(1).toString());
        assertEquals("LIVE", statusList.get(2).toString());
        assertEquals(1, getAssignmentRecordsCount().longValue());
        assertEquals(ACTOR_ID, getActorFromAssignmentTable());
        logger.info(" History record count after create request : {}", getHistoryRecordsCount());
        logger.info(" LIVE table record count after create assignment request : {}", getAssignmentRecordsCount());
        logger.info(" LIVE table actor Id after create assignment request : {}", getActorFromAssignmentTable());

        //Insert role assignment records with replace existing is True
        AssignmentRequest assignmentRequestWithReplaceExistingTrue = TestDataBuilder.createRoleAssignmentRequest(
            true,
            true
        );
        logger.info(
            "** Creating another role assignment record with request :   {}",
            mapper.writeValueAsString(assignmentRequestWithReplaceExistingTrue)
        );
        mockMvc.perform(post(url)
                            .contentType(JSON_CONTENT_TYPE)
                            .headers(getHttpHeaders())
                            .content(mapper.writeValueAsBytes(assignmentRequestWithReplaceExistingTrue))
        ).andExpect(status().is(201)).andReturn();

        List<String> newStatusList = getStatusFromHistory();
        assertEquals(8, newStatusList.size());
        assertEquals("CREATED", newStatusList.get(0).toString());
        assertEquals("APPROVED", newStatusList.get(1).toString());
        assertEquals("LIVE", newStatusList.get(2).toString());
        assertEquals("DELETE_APPROVED", newStatusList.get(3).toString());
        assertEquals("CREATED", newStatusList.get(4).toString());
        assertEquals("APPROVED", newStatusList.get(5).toString());
        assertEquals("DELETED", newStatusList.get(6).toString());
        assertEquals("LIVE", newStatusList.get(7).toString());
        assertEquals(1, getAssignmentRecordsCount().longValue());
        assertEquals(ACTOR_ID, getActorFromAssignmentTable());
        logger.info(" History record count after create request : {}", getHistoryRecordsCount());
        logger.info(" LIVE table record count after create assignment request : {}", getAssignmentRecordsCount());
        logger.info(" LIVE table actor Id after create assignment request : {}", getActorFromAssignmentTable());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts =
        {"classpath:sql/role_assignment_clean_up.sql",
        "classpath:sql/insert_assignment_records_to_delete.sql"})
    public void shouldDeleteRoleAssignments() throws Exception {
        logger.info(" History record count before create assignment request : {}", getHistoryRecordsCount());
        logger.info(" LIVE table record count before create assignment request : {}", getAssignmentRecordsCount());
        final String url = "/am/role-assignments";

        mockMvc.perform(delete(url)
                            .contentType(JSON_CONTENT_TYPE)
                            .headers(getHttpHeaders())
                            .param("process", "S-052")
                            .param("reference", "S-052")
        )
            .andExpect(status().is(204))
            .andReturn();
        logger.info(" History record count after create assignment request : {}", getHistoryRecordsCount());
        logger.info(" LIVE table record count after create assignment request : {}", getAssignmentRecordsCount());
        List<String> statusList = getStatusFromHistory();
        assertEquals(5, statusList.size());
        assertEquals("CREATED", statusList.get(0).toString());
        assertEquals("APPROVED", statusList.get(1).toString());
        assertEquals("LIVE", statusList.get(2).toString());
        assertEquals("DELETE_APPROVED", statusList.get(3).toString());
        assertEquals("DELETED", statusList.get(4).toString());

    }

    private Integer getHistoryRecordsCount() {
        return template.queryForObject(COUNT_HISTORY_RECORDS_QUERY, Integer.class);
    }

    private Integer getAssignmentRecordsCount() {
        return template.queryForObject(COUNT_ASSIGNMENT_RECORDS_QUERY, Integer.class);
    }

    @NotNull
    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("ServiceAuthorization", "Bearer " + serviceAuth);
        headers.add("Authorization", "Bearer " + accessToken);
        return headers;
    }

    public List<String> getStatusFromHistory() {
        return template.queryForList(GET_ASSIGNMENT_STATUS_QUERY, new Object[]{ACTOR_ID}, String.class);
    }

    public String getActorFromAssignmentTable() {
        return template.queryForObject(GET_ACTOR_FROM_ASSIGNMENT_QUERY, new Object[]{ACTOR_ID}, String.class);
    }
}
