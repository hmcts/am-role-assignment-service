package uk.gov.hmcts.reform.roleassignment.drool;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder.CLIENT_ID_AAC_MANAGE_CASE;
import static uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder.CLIENT_ID_CCD_CASE_DISPOSER;
import static uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder.CLIENT_ID_CCD_DATA;
import static uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder.CLIENT_ID_ORM;
import static uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder.CLIENT_ID_XUI;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleCategory;


@Slf4j
class CcdDeleteCaseRoleIntegrationTest extends BaseDroolIntegrationTest {

    // IDs from `/src/integrationTest/resources/sql/insert_case_role_assignments.sql`
    //  or from `/src/integrationTest/resources/sql/insert_creator_case_role_assignments.sql`
    //  or from `/src/integrationTest/resources/sql/insert_organisation_role_assignments.sql`
    static final String ADMIN_ASSIGNMENT_ID = "88321fd6-3482-49ff-a141-bb71b1fd72db";
    static final String ADMIN_ACTOR_ID = "42454e17-222b-4e91-8a6e-619654a0d361";

    static final String CTSC_ASSIGNMENT_ID = "25ad5b15-8569-483b-b890-1adb792a7fc9";
    static final String CTSC_ACTOR_ID = "22d33eaf-95a6-4856-b981-e8cde4ddda00";

    static final String LEGAL_OPERATIONS_ASSIGNMENT_ID = "df948472-0286-42f0-983c-25801923b4e8";
    static final String LEGAL_OPERATIONS_ACTOR_ID = "420ae207-93a2-4677-99d1-8cf3996bfbfa";

    static final String JUDICIAL_ASSIGNMENT_ID = "bd75d468-8b5f-47ec-aeac-823511e8b8f4";
    static final String JUDICIAL_ACTOR_ID = "ddf81529-5c67-4599-b4c5-40dcac04f8d2";

    static final String CITIZEN_ASSIGNMENT_ID = "d8edc22a-9995-41b2-9dc8-54256b806b8e";
    static final String CITIZEN_ACTOR_ID = "d4652c71-80f9-4f4a-b48e-83e17321438a";

    static final String PROFESSIONAL_ASSIGNMENT_ID = "cbe1e4f7-7964-4b78-b374-f612c43cac5c";
    static final String PROFESSIONAL_ACTOR_ID = "90ca2538-920c-4719-9ef9-52230231c037";

    @Nested
    @DisplayName("Citizen case role deletions")
    class CitizenCaseRoleDeletionTests {

        @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
            "classpath:sql/init_role_assignment.sql",
            "classpath:sql/insert_case_role_assignments.sql"
        })
        @ParameterizedTest
        @CsvSource({
            CLIENT_ID_AAC_MANAGE_CASE, // permitted in drool rule: ccd_delete_case_roles
            CLIENT_ID_CCD_DATA, // permitted in drool rule: ccd_delete_case_roles
            CLIENT_ID_CCD_CASE_DISPOSER //  permitted in drool rule: ccd_delete_case_roles
        })
        void shouldDeleteCitizenCaseRole_forPermittedClientId(String clientId) throws Exception {
            assertSuccessfulDeletion(clientId, RoleCategory.CITIZEN);
        }

        @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
            "classpath:sql/init_role_assignment.sql",
            "classpath:sql/insert_case_role_assignments.sql"
        })
        @ParameterizedTest
        @CsvSource({
            CLIENT_ID_ORM, // NB: no case role deletion allowed from ORM
            CLIENT_ID_XUI,
        })
        void shouldRejectDeleteCitizenCaseRole_forNonPermittedClientId(String clientId) throws Exception {
            assertRejectedDeletion(clientId, RoleCategory.CITIZEN);
        }

    }

    @Nested
    @DisplayName("Professional case role deletions")
    class ProfessionalCaseRoleDeletionTests {

        @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
            "classpath:sql/init_role_assignment.sql",
            "classpath:sql/insert_case_role_assignments.sql"
        })
        @ParameterizedTest
        @CsvSource({
            CLIENT_ID_AAC_MANAGE_CASE, // permitted in drool rule: ccd_delete_case_roles
            CLIENT_ID_CCD_DATA, // permitted in drool rule: ccd_delete_case_roles
            CLIENT_ID_CCD_CASE_DISPOSER, //  permitted in drool rule: ccd_delete_case_roles
        })
        void shouldDeleteProfessionalCaseRole_forPermittedClientId(String clientId) throws Exception {
            assertSuccessfulDeletion(clientId, RoleCategory.PROFESSIONAL);
        }

        @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
            "classpath:sql/init_role_assignment.sql",
            "classpath:sql/insert_case_role_assignments.sql"
        })
        @ParameterizedTest
        @CsvSource({
            CLIENT_ID_ORM, // NB: no case role deletion allowed from ORM
            CLIENT_ID_XUI,
        })
        void shouldRejectDeleteProfessionalCaseRole_forNonPermittedClientId(String clientId) throws Exception {
            assertRejectedDeletion(clientId, RoleCategory.PROFESSIONAL);
        }

    }

    @Nested
    @DisplayName("Judicial case role deletions")
    class JudicialCaseRoleDeletionTests {

        @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
            "classpath:sql/init_role_assignment.sql",
            "classpath:sql/insert_case_role_assignments.sql"
        })
        @ParameterizedTest
        @CsvSource({
            CLIENT_ID_CCD_CASE_DISPOSER, //  permitted in drool rule: v1_1_ccd_delete_case_roles
        })
        void shouldDeleteJudicialCaseRole_forPermittedClientId(String clientId) throws Exception {
            assertSuccessfulDeletion(clientId, RoleCategory.JUDICIAL);
        }

        @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
            "classpath:sql/init_role_assignment.sql",
            "classpath:sql/insert_case_role_assignments.sql"
        })
        @ParameterizedTest
        @CsvSource({
            CLIENT_ID_AAC_MANAGE_CASE, // NB: Judicial case-role deletion is not permitted for AAC
            CLIENT_ID_CCD_DATA, // NB: Judicial case-role deletion is not permitted for CCD
            CLIENT_ID_ORM, // NB: no case role deletion allowed from ORM
            CLIENT_ID_XUI, // NB: not permitted outside the case-allocation process from XUI
        })
        void shouldRejectDeleteJudicialCaseRole_forNonPermittedClientId(String clientId) throws Exception {
            assertRejectedDeletion(clientId, RoleCategory.JUDICIAL);
        }

    }

    @Nested
    @DisplayName("Staff case role deletions")
    class StaffCaseRoleDeletionTests {


        @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
            "classpath:sql/init_role_assignment.sql",
            "classpath:sql/insert_case_role_assignments.sql"
        })
        @ParameterizedTest
        @CsvSource({
            CLIENT_ID_CCD_CASE_DISPOSER, //  permitted in drool rule: v1_1_ccd_delete_case_roles
        })
        void shouldDeleteStaffCaseRole_forPermittedClientId(String clientId) throws Exception {
            assertSuccessfulDeletion(clientId, RoleCategory.ADMIN);
            assertSuccessfulDeletion(clientId, RoleCategory.CTSC);
            assertSuccessfulDeletion(clientId, RoleCategory.LEGAL_OPERATIONS);
        }

        @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
            "classpath:sql/init_role_assignment.sql",
            "classpath:sql/insert_case_role_assignments.sql"
        })
        @ParameterizedTest
        @CsvSource({
            CLIENT_ID_AAC_MANAGE_CASE, // NB: Staff case-role deletion is not permitted for AAC
            CLIENT_ID_CCD_DATA, // NB: Staff case-role deletion is not permitted for CCD
            CLIENT_ID_ORM, // NB: no case role deletion allowed from ORM
            CLIENT_ID_XUI, // NB: not permitted outside the case-allocation process from XUI
        })
        void shouldRejectDeleteStaffCaseRole_forNonPermittedClientId(String clientId) throws Exception {
            assertRejectedDeletion(clientId, RoleCategory.ADMIN);
            assertRejectedDeletion(clientId, RoleCategory.CTSC);
            assertRejectedDeletion(clientId, RoleCategory.LEGAL_OPERATIONS);
        }

    }

    @Nested
    @DisplayName("[CREATOR] case role deletions")
    class CreatorCaseRoleDeletionTests {

        @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
            "classpath:sql/init_role_assignment.sql",
            "classpath:sql/insert_creator_case_role_assignments.sql" // NB: [CREATOR] case-role sql
        })
        @ParameterizedTest
        @CsvSource({
            CLIENT_ID_AAC_MANAGE_CASE, // permitted in drool rule: ccd_delete_case_roles_creator
            CLIENT_ID_CCD_DATA, // permitted in drool rule: ccd_delete_case_roles_creator
            CLIENT_ID_CCD_CASE_DISPOSER, // permitted in drool rule: ccd_delete_case_roles_creator
        })
        void shouldDeleteCreatorCaseRole_forPermittedClientId(String clientId) throws Exception {
            // permitted in drool rule: ccd_delete_case_roles
            assertSuccessfulDeletion(clientId, RoleCategory.CITIZEN);
            assertSuccessfulDeletion(clientId, RoleCategory.PROFESSIONAL);
            // permitted in drool rule: ccd_delete_case_roles_creator
            assertSuccessfulDeletion(clientId, RoleCategory.JUDICIAL);
            assertSuccessfulDeletion(clientId, RoleCategory.LEGAL_OPERATIONS);
            assertSuccessfulDeletion(clientId, RoleCategory.ADMIN);
        }

        @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
            "classpath:sql/init_role_assignment.sql",
            "classpath:sql/insert_creator_case_role_assignments.sql" // NB: [CREATOR] case-role sql
        })
        @ParameterizedTest
        @CsvSource({
            CLIENT_ID_ORM, // NB: no case role deletion allowed from ORM
            CLIENT_ID_XUI,
        })
        void shouldRejectDeleteCreatorCaseRole_forNonPermittedClientId(String clientId) throws Exception {
            assertRejectedDeletion(clientId, RoleCategory.CITIZEN);
            assertRejectedDeletion(clientId, RoleCategory.PROFESSIONAL);
            assertRejectedDeletion(clientId, RoleCategory.JUDICIAL);
            assertRejectedDeletion(clientId, RoleCategory.LEGAL_OPERATIONS);
            assertRejectedDeletion(clientId, RoleCategory.ADMIN);
        }

    }

    @Nested
    @DisplayName("Organisation role deletions")
    class OrgRoleDeletionTests {

        @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
            "classpath:sql/init_role_assignment.sql",
            "classpath:sql/insert_organisation_role_assignments.sql" // NB: Organisation role sql
        })
        @ParameterizedTest
        @CsvSource({
            CLIENT_ID_ORM // permitted in drool: staff_organisational_role_mapping_service_delete & prm_delete_org_role
        })
        void shouldDeleteOrgRole_forPermittedClientId(String clientId) throws Exception {
            // NB: there are no CITIZEN org roles
            assertSuccessfulDeletion(clientId, RoleCategory.PROFESSIONAL); // i.e. PRM
            assertSuccessfulDeletion(clientId, RoleCategory.JUDICIAL);
            assertSuccessfulDeletion(clientId, RoleCategory.LEGAL_OPERATIONS);
            assertSuccessfulDeletion(clientId, RoleCategory.ADMIN);
            assertSuccessfulDeletion(clientId, RoleCategory.CTSC);
        }

        @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
            "classpath:sql/init_role_assignment.sql",
            "classpath:sql/insert_organisation_role_assignments.sql" // NB: Organisation role sql
        })
        @ParameterizedTest
        @CsvSource({
            // NB: Org roles can only be deleted by ORM
            CLIENT_ID_AAC_MANAGE_CASE,
            CLIENT_ID_CCD_DATA,
            CLIENT_ID_CCD_CASE_DISPOSER,
            CLIENT_ID_XUI,
        })
        void shouldRejectDeleteOrgRole_forNonPermittedClientId(String clientId) throws Exception {
            // NB: there are no CITIZEN org roles
            assertRejectedDeletion(clientId, RoleCategory.PROFESSIONAL); // i.e. PRM
            assertRejectedDeletion(clientId, RoleCategory.JUDICIAL);
            assertRejectedDeletion(clientId, RoleCategory.LEGAL_OPERATIONS);
            assertRejectedDeletion(clientId, RoleCategory.ADMIN);
            assertRejectedDeletion(clientId, RoleCategory.CTSC);
        }

    }

    private void assertSuccessfulDeletion(String clientId,
                                          RoleCategory category) throws Exception {
        // GIVEN
        String actorId = getActorId(category);
        String assignmentId = getAssignmentId(category);
        String url = buildUrl(assignmentId);

        // WHEN
        mockMvc.perform(delete(url)
                            .contentType(JSON_CONTENT_TYPE)
                            .headers(getHttpHeaders(clientId))
        ).andExpect(status().is(204)).andReturn(); // 204 - Success No Content

        // THEN
        // verify role assignment removed
        assertRoleAssignmentsInDb(actorId, 0);
    }

    private void assertRejectedDeletion(String clientId,
                                        RoleCategory category) throws Exception {
        // GIVEN
        String actorId = getActorId(category);
        String assignmentId = getAssignmentId(category);
        String url = buildUrl(assignmentId);

        // WHEN
        mockMvc.perform(delete(url)
                            .contentType(JSON_CONTENT_TYPE)
                            .headers(getHttpHeaders(clientId))
        ).andExpect(status().is(422)).andReturn(); // 422 - rejected

        // THEN
        // verify role assignment deletion rejected
        assertRoleAssignmentsInDb(actorId, 1);
    }

    private String buildUrl(String assignmentId) {
        return String.format("%s/%s", URL_DELETE_ROLES, assignmentId);
    }

    private String getActorId(RoleCategory category) {
        return switch (category) {
            case ADMIN -> ADMIN_ACTOR_ID;
            case CTSC -> CTSC_ACTOR_ID;
            case LEGAL_OPERATIONS -> LEGAL_OPERATIONS_ACTOR_ID;
            case JUDICIAL -> JUDICIAL_ACTOR_ID;
            case CITIZEN -> CITIZEN_ACTOR_ID;
            case PROFESSIONAL -> PROFESSIONAL_ACTOR_ID;
            default -> throw new IllegalArgumentException("Invalid role category: " + category);
        };
    }

    private String getAssignmentId(RoleCategory category) {
        return switch (category) {
            case ADMIN -> ADMIN_ASSIGNMENT_ID;
            case CTSC -> CTSC_ASSIGNMENT_ID;
            case LEGAL_OPERATIONS -> LEGAL_OPERATIONS_ASSIGNMENT_ID;
            case JUDICIAL -> JUDICIAL_ASSIGNMENT_ID;
            case CITIZEN -> CITIZEN_ASSIGNMENT_ID;
            case PROFESSIONAL -> PROFESSIONAL_ASSIGNMENT_ID;
            default -> throw new IllegalArgumentException("Invalid role category: " + category);
        };
    }

}
