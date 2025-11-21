package uk.gov.hmcts.reform.roleassignment.drool;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;


@Slf4j
public class CcdDeleteCaseRoleIntegrationTest extends BaseDroolIntegrationTest {

    static final String CCD_DATA_CLIENT_ID = "ccd_data";

    // UID from `/src/integrationTest/resources/sql/insert_case_role_assignment_civil.sql`
    static final String UID_CASE_ACTOR_ID = "42454e17-222b-4e91-8a6e-619654a0d361";
    static final String UID_ASSIGNEMT_ID = "88321fd6-3482-49ff-a141-bb71b1fd72db";

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/init_role_assignment.sql",
        "classpath:sql/insert_case_role_assignment.sql"
    })
    public void shouldDeleteCaseForCcdDeleteCaseRole() throws Exception {

        // GIVEN
        String url = String.format("%s/%s", URL_DELETE_ROLES, UID_ASSIGNEMT_ID);

        // WHEN
        mockMvc.perform(delete(url)
                            .contentType(JSON_CONTENT_TYPE)
                            .headers(getHttpHeaders(CCD_DATA_CLIENT_ID))
        ).andExpect(status().is(204)).andReturn();

        // THEN
        // verify role assignment removed
        assertRoleAssignmentsInDb(UID_CASE_ACTOR_ID, 0);
    }

}
