
package uk.gov.hmcts.reform.roleassignment.controller;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.Assert.assertEquals;

public class RoleAssignmentIntegrationTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(RoleAssignmentIntegrationTest.class);
    private static final String COUNT_HISTORY_RECORDS_QUERY = "SELECT count(1) as n FROM role_assignment_history";
    private static final String GET_ASSIGNMENT_STATUS_QUERY = "SELECT actor_id FROM role_assignment where id = ?";
    private transient MockMvc mockMvc;

    private JdbcTemplate template;

    @Before
    public void setUp() {
        template = new JdbcTemplate(db);
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
}

