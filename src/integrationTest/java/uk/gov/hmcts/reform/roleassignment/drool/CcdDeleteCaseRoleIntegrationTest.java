package uk.gov.hmcts.reform.roleassignment.drool;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;


@Slf4j
public class CcdDeleteCaseRoleIntegrationTest extends BaseDroolIntegrationTest {

    static final String CCD_DATA_CLIENT_ID = "ccd_data";
    static final String AAC_MANAGE_CASE_CLIENT_ID = "aac_manage_case_assignment";
    static final String CCD_CASE_DISPOSER_CLIENT_ID = "ccd_case_disposer";

    // UID from `/src/integrationTest/resources/sql/insert_case_role_assignment.sql`
    static final String UID_SUCCESSFUL_ACTOR_ID = "42454e17-222b-4e91-8a6e-619654a0d361";
    static final String UID_SUCCESSFUL_ASSIGNEMT_ID = "88321fd6-3482-49ff-a141-bb71b1fd72db";

    // UID from `/src/integrationTest/resources/sql/insert_case_role_assignment_deletions_to_reject.sql`
    static final String UID_NO_CASE_ID_ACTOR_ID = "61abb5b3-7367-42d9-afc9-08244556547f";
    static final String UID_NO_CASE_ID_ASSIGNEMT_ID = "b1a41a4b-1f0e-4696-a808-4b65cc3c6faf";

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/init_role_assignment.sql",
        "classpath:sql/insert_case_role_assignment.sql"
    })
    public void shouldDeleteCaseForCcdDeleteCaseRole_ccdCaseDisposer() throws Exception {
        assertSuccessfulDeletion(CCD_CASE_DISPOSER_CLIENT_ID);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/init_role_assignment.sql",
        "classpath:sql/insert_case_role_assignment.sql"
    })
    public void shouldRejectDeleteCaseForCcdDeleteCaseRole_aacManageCase() throws Exception {
        assertRejectedDeletion(AAC_MANAGE_CASE_CLIENT_ID,
                               UID_SUCCESSFUL_ASSIGNEMT_ID,
                               UID_SUCCESSFUL_ACTOR_ID);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/init_role_assignment.sql",
        "classpath:sql/insert_case_role_assignment.sql"
    })
    public void shouldRejectDeleteCaseForCcdDeleteCaseRole_ccdData() throws Exception {
        assertRejectedDeletion(CCD_DATA_CLIENT_ID,
                               UID_SUCCESSFUL_ASSIGNEMT_ID,
                               UID_SUCCESSFUL_ACTOR_ID);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/init_role_assignment.sql",
        "classpath:sql/insert_case_role_assignment_deletions_to_reject.sql"
    })
    public void shouldRejectDeleteCaseForCcdDeleteCaseRole_MissingCaseId() throws Exception {
        assertRejectedDeletion(CCD_CASE_DISPOSER_CLIENT_ID,
                               UID_NO_CASE_ID_ASSIGNEMT_ID,
                               UID_NO_CASE_ID_ACTOR_ID);
    }

    //#region Successful deletion

    private void assertSuccessfulDeletion(String clientId) throws Exception {
        // GIVEN
        String url = buildUrl(UID_SUCCESSFUL_ASSIGNEMT_ID);

        // WHEN
        mockMvc.perform(delete(url)
                            .contentType(JSON_CONTENT_TYPE)
                            .headers(getHttpHeaders(clientId))
        ).andExpect(status().is(204)).andReturn(); // 204 - Success No Content

        // THEN
        // verify role assignment removed
        assertRoleAssignmentsInDb(UID_SUCCESSFUL_ACTOR_ID, 0);
    }

    //#region Rejected deletion

    private void assertRejectedDeletion(String clientId,
                                        String assignementId,
                                        String actorId) throws Exception {
        // GIVEN
        String url = buildUrl(assignementId);

        // WHEN
        mockMvc.perform(delete(url)
                            .contentType(JSON_CONTENT_TYPE)
                            .headers(getHttpHeaders(clientId))
        ).andExpect(status().is(422)).andReturn(); // 422 - rejected

        // THEN
        // verify role assignment deletion rejected
        assertRoleAssignmentsInDb(actorId, 1);
    }
    //#endregion

    private String buildUrl(String assignmentId) {
        return String.format("%s/%s", URL_DELETE_ROLES, assignmentId);
    }
}
