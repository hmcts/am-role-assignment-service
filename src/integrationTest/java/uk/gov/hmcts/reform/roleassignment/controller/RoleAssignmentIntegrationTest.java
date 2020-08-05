
package uk.gov.hmcts.reform.roleassignment.controller;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
import uk.gov.hmcts.reform.roleassignment.util.SecurityUtils;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import javax.inject.Inject;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class RoleAssignmentIntegrationTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(RoleAssignmentIntegrationTest.class);
    private static final String COUNT_HISTORY_RECORDS_QUERY = "SELECT count(1) as n FROM role_assignment_history";
    private static final String GET_ASSIGNMENT_STATUS_QUERY = "SELECT actor_id FROM role_assignment where id = ?";
    private transient MockMvc mockMvc;

    private JdbcTemplate template;

    @Inject
    private WebApplicationContext wac;

    @Mock
    private SecurityUtils securityUtilsMock = mock(SecurityUtils.class);

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
    public void shoudGetRecordCountFromHistoryTable() throws Exception {
        final int count = template.queryForObject(COUNT_HISTORY_RECORDS_QUERY, Integer.class);
        logger.info(" Total number of records fetched from role assignment history table...{}", count);
        assertEquals(
            "role_assignment_history record count ", 15, count);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_role_assignment.sql"})
    public void shoudGetRecordsFromRoleAssignmentTable() throws Exception {
        final Object[] parameters = new Object[]{
            "638e8e7a-7d7c-4027-9d53-ea4b1095eab1"
        };
        String actorId = template.queryForObject(GET_ASSIGNMENT_STATUS_QUERY, parameters, String.class);
        logger.info(" Role assignment actor id is...{}", actorId);
        assertEquals(
            "Role assignment actor Id", "123e4567-e89b-42d3-a456-556642445613", actorId);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_assignment_records_to_delete.sql" })
    public void shouldDeleteRoleAssignments() throws Exception {
        String clientId = "client_id";
        UUID userId = UUID.fromString("21334a2b-79ce-44eb-9168-2d49a744be9c");
        when(securityUtilsMock.getServiceName()).thenReturn(clientId);
        when(securityUtilsMock.getUserId()).thenReturn(userId.toString());

        final String url = "/am/role-assignments";
        HttpHeaders headers = new HttpHeaders();
        headers.add("ServiceAuthorization", "Bearer " + "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJjY2RfZ3ciLCJleHAiOjE1OTYyMjIwNDZ9.6dyTWiXaaSjfOZpqg9ieHakQARgG5rdlJjfoolI70RJD4V0gRFMFIJ6ULF3c-SBoQAZk0rhlixf7Id74DA7iBQ");
        headers.add("Authorization", "Bearer " + "eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2lkIjoiYi9PNk92VnYxK3krV2dySDVVaTlXVGlvTHQwPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJiZWZ0YS5jYXNld29ya2VyLjIuc29saWNpdG9yLjJAZ21haWwuY29tIiwiYXV0aF9sZXZlbCI6MCwiYXVkaXRUcmFja2luZ0lkIjoiODI4ZTkwNTAtMzlmYy00YmI3LWFiYjgtZWYzMTgyYzgzMDI5IiwiaXNzIjoiaHR0cDovL2ZyLWFtOjgwODAvb3BlbmFtL29hdXRoMi9obWN0cyIsInRva2VuTmFtZSI6ImFjY2Vzc190b2tlbiIsInRva2VuX3R5cGUiOiJCZWFyZXIiLCJhdXRoR3JhbnRJZCI6IjY5ZDRjNDhmLTFiNzMtNGFkMy1iOTEwLTIzZDZjZDNkMWUxYSIsImF1ZCI6ImFtX2RvY2tlciIsIm5iZiI6MTU5NjQ4OTk5NiwiZ3JhbnRfdHlwZSI6InBhc3N3b3JkIiwic2NvcGUiOlsib3BlbmlkIiwicHJvZmlsZSIsInJvbGVzIiwic2VhcmNoLXVzZXIiLCJhdXRob3JpdGllcyJdLCJhdXRoX3RpbWUiOjE1OTY0ODk5OTYsInJlYWxtIjoiL2htY3RzIiwiZXhwIjoxNTk2NTE4Nzk2LCJpYXQiOjE1OTY0ODk5OTYsImV4cGlyZXNfaW4iOjI4ODAwLCJqdGkiOiIzY2U5MDA0NC05MjliLTQxNzYtOWFhZC1lYjE4MGI5NWQ4NGEifQ.SCEfZVwTmlq1SGcBtb7kpDM6P8b6dbj1v7prWLXytik2ksohJ_HFNaP2Z0yPJOKjrnrlmci0NpqiC6ORn-LQCYo7Cm8mzERvynfpJt63uwxxy1Bp6OktuaLcwyMyqcXPhV3DQYz1bYF4Ccl1XbfH9LXsfmQJig3dF90Bl8YWBGIKQj8Z-RtEGo9JCoQ0BQtqHfk5xUXivkDJ9jIbBswetUBS9XDIkTgLXkmAfB9W_wOavlLaGJTX8_Id9QgkXUgbUhTs2kyx54ArPo3n840EX6p9M2IISryyZXX89TLh8NjVwxrvEVpBkfKUkjqNGYWisroSdZrz4BiDpMYqYbDdhg");

        final MvcResult result = mockMvc.perform(delete(url)
                                                     .contentType(MediaType.APPLICATION_JSON)
                                                     .headers(headers)
                                                     .param("process", "S-052")
                                                     .param("reference", "S-052")
        )
            .andExpect(status().is(204))
            .andReturn();
        String contentAsString = result.getResponse().getContentAsString();
        logger.info(" contentAsString...{}", contentAsString);

    }
}

