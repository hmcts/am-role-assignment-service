package uk.gov.hmcts.reform.roleassignment.drool.prmconfig;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.Request;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.ActorIdType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Classification;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.drool.BaseDroolIntegrationTest;
import uk.gov.hmcts.reform.roleassignment.drool.helper.ReportWriter;
import uk.gov.hmcts.reform.roleassignment.drool.model.PrmConfigTestArguments;
import uk.gov.hmcts.reform.roleassignment.drool.model.TestScenario;
import uk.gov.hmcts.reform.roleassignment.util.JacksonUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.roleassignment.util.JacksonUtils.convertValueJsonNode;


public class RunPrmConfigDroolIntegrationTests extends BaseDroolIntegrationTest {

    public static final String DROOL_PRM_TEST_OUTPUT_PATH = DROOL_TEST_OUTPUT_PATH + "PrmConfig/";

    static final String STEP_GRANT = "WHEN: Grant PRM-Role";
    static final String STEP_DELETE = "WHEN: Delete PRM-Role";
    static final String STEP_REJECT_GRANT = "WHEN: Reject Grant PRM-Role";
    static final String STEP_AFTER_GRANT = "THEN: After Grant";
    static final String STEP_AFTER_DELETE = "THEN: After Delete";
    static final String STEP_AFTER_REJECT = "THEN: After Reject";

    private static final String DISPLAY_NAME = "#{index} - {0}";

    private TestScenario testScenario;

    private static final List<TestScenario> testRun = new ArrayList<>();


    static class TestArgumentGenerator {

        private static List<PrmConfigTestArguments> getAllPrmConfigTestArguments() {
            List<PrmConfigTestArguments> arguments = new ArrayList<>();

            arguments.addAll(PossessionsPrmConfigIT.getAllTestArguments());

            return arguments;
        }

        static Stream<Arguments> getGroupRoleTestArguments() {
            return getAllPrmConfigTestArguments().stream()
                .filter(PrmConfigTestArguments::isGaGroupRole)
                .map(PrmConfigTestArguments::toArguments);
        }

        static Stream<Arguments> getOrgRoleTestArguments() {
            return getAllPrmConfigTestArguments().stream()
                .filter(PrmConfigTestArguments::isGaOrgRole)
                .map(PrmConfigTestArguments::toArguments);
        }

    }


    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    @BeforeAll
    static void beforeAllTests() throws IOException {
        File outputDirectory = new File(DROOL_PRM_TEST_OUTPUT_PATH);
        if (outputDirectory.exists()) {
            FileUtils.deleteDirectory(outputDirectory);
        }
        outputDirectory.mkdirs();
    }

    @AfterAll
    static void afterAllTests() {
        ReportWriter.writeTestReport(
            "PRM Config Drool Integration Tests",
            RunPrmConfigDroolIntegrationTests.HappyPathGaGroupRoleTests.SUMMARY
                + RunPrmConfigDroolIntegrationTests.NegativeGrantGaGroupRoleTests.SUMMARY
                + RunPrmConfigDroolIntegrationTests.HappyPathGaOrgRoleTests.SUMMARY
                + RunPrmConfigDroolIntegrationTests.NegativeGrantGaOrgRoleTests.SUMMARY,
            DROOL_PRM_TEST_OUTPUT_PATH,
            RunPrmConfigDroolIntegrationTests.testRun
        );
    }

    @AfterEach
    void afterEachTest() {
        if (this.testScenario != null) {
            this.testScenario.writeToFile();
            RunPrmConfigDroolIntegrationTests.testRun.add(testScenario);
        }
    }

    private void createTestScenario(String testName, String testDescription, PrmConfigTestArguments testArguments) {
        this.testScenario = new TestScenario(
            testName,
            testDescription,
            DROOL_PRM_TEST_OUTPUT_PATH + "%s/" + testName + "/",
            testArguments
        );
    }

    private void registerError(Error error) {
        if (this.testScenario != null) {
            this.testScenario.setError(error);
        }
    }

    @Nested
    class HappyPathGaGroupRoleTests extends TestArgumentGenerator {

        static final String GRANT_AND_DELETE = "Grant then Delete GA Group-role";

        static final String SUMMARY =
            """
                    <h2>Happy Paths - Group-Access Group role</h2>
                    <ul>
                    <li>%s</li>
                    </ul>
                """.formatted(GRANT_AND_DELETE);

        @MethodSource("getGroupRoleTestArguments")
        @ParameterizedTest(name = DISPLAY_NAME)
        void testGrantAndDeleteGaGroupRole(String ignoredDisplayName,
                                           PrmConfigTestArguments testArguments) throws Exception {

            createTestScenario(
                "testGrantAndDeleteGaGroupRole",
                GRANT_AND_DELETE,
                testArguments
            );

            try {
                runGrantAndDeletePrmRole(testArguments, true);
            } catch (AssertionError ex) {
                registerError(ex);
                throw ex;
            }
        }

    }


    @Nested
    class HappyPathGaOrgRoleTests extends TestArgumentGenerator {

        static final String GRANT_AND_DELETE = "Grant then Delete GA Org-role";

        static final String SUMMARY =
            """
                    <h2>Happy Paths - Group-Access Organisational role</h2>
                    <ul>
                    <li>%s</li>
                    </ul>
                """.formatted(GRANT_AND_DELETE);

        @MethodSource("getOrgRoleTestArguments")
        @ParameterizedTest(name = DISPLAY_NAME)
        void testGrantAndDeleteGaOrgRole(String ignoredDisplayName,
                                         PrmConfigTestArguments testArguments) throws Exception {

            createTestScenario(
                "testGrantAndDeleteGaOrgRole",
                GRANT_AND_DELETE,
                testArguments
            );

            try {
                runGrantAndDeletePrmRole(testArguments, false);
            } catch (AssertionError ex) {
                registerError(ex);
                throw ex;
            }
        }

    }


    @Nested
    class NegativeGrantGaGroupRoleTests extends TestArgumentGenerator {

        static final String REJECT_BAD_JURISDICTION = "Reject GA Group-role - bad jurisdiction";
        static final String REJECT_MISSING_CASE_TYPE = "Reject GA Group-role - missing case-type";
        static final String REJECT_MISSING_CASE_ACCESS_GROUP_ID = "Reject GA Group-role - missing caseAccessGroupId";

        static final String SUMMARY =
            """
                    <h2>Negative Paths - Group-Access Group role</h2>
                    <ul>
                    <li>%s</li>
                    <li>%s</li>
                    <li>%s</li>
                    </ul>
                """.formatted(
                REJECT_BAD_JURISDICTION,
                REJECT_MISSING_CASE_TYPE,
                REJECT_MISSING_CASE_ACCESS_GROUP_ID
            );

        @MethodSource("getGroupRoleTestArguments")
        @ParameterizedTest(name = DISPLAY_NAME)
        void testRejectPrmGaGroupRole_badJurisdiction(String ignoredDisplayName,
                                                      PrmConfigTestArguments testArguments) throws Exception {

            createTestScenario(
                "testRejectPrmGaGroupRole_badJurisdiction",
                REJECT_BAD_JURISDICTION,
                testArguments
            );

            try {
                runRejectPrmRole_badRequest(testArguments, true, false, false);
            } catch (AssertionError ex) {
                registerError(ex);
                throw ex;
            }
        }

        @MethodSource("getGroupRoleTestArguments")
        @ParameterizedTest(name = DISPLAY_NAME)
        void testRejectPrmGaGroupRole_missingCaseType(String ignoredDisplayName,
                                                      PrmConfigTestArguments testArguments) throws Exception {

            createTestScenario(
                "testRejectPrmGaGroupRole_missingCaseType",
                REJECT_MISSING_CASE_TYPE,
                testArguments
            );

            try {
                runRejectPrmRole_badRequest(testArguments, false, true, false);
            } catch (AssertionError ex) {
                registerError(ex);
                throw ex;
            }
        }

        @MethodSource("getGroupRoleTestArguments")
        @ParameterizedTest(name = DISPLAY_NAME)
        void testRejectPrmGaGroupRole_missingCaseAccessGroupId(String ignoredDisplayName,
                                                               PrmConfigTestArguments testArguments) throws Exception {

            createTestScenario(
                "testRejectPrmGaGroupRole_missingCaseAccessGroupId",
                REJECT_MISSING_CASE_ACCESS_GROUP_ID,
                testArguments
            );

            try {
                runRejectPrmRole_badRequest(testArguments, false, false, true);
            } catch (AssertionError ex) {
                registerError(ex);
                throw ex;
            }
        }

    }


    @Nested
    class NegativeGrantGaOrgRoleTests extends TestArgumentGenerator {

        static final String REJECT_BAD_JURISDICTION = "Reject GA Org-role - bad jurisdiction";
        static final String REJECT_MISSING_CASE_TYPE = "Reject GA Org-role - missing case-type";

        static final String SUMMARY =
            """
                    <h2>Negative Paths - Group-Access Organisational role</h2>
                    <ul>
                    <li>%s</li>
                    <li>%s</li>
                    </ul>
                """.formatted(
                REJECT_BAD_JURISDICTION,
                REJECT_MISSING_CASE_TYPE
            );

        @MethodSource("getOrgRoleTestArguments")
        @ParameterizedTest(name = DISPLAY_NAME)
        void testRejectPrmGaOrgRole_badJurisdiction(String ignoredDisplayName,
                                                    PrmConfigTestArguments testArguments) throws Exception {

            createTestScenario(
                "testRejectPrmGaOrgRole_badJurisdiction",
                REJECT_BAD_JURISDICTION,
                testArguments
            );

            try {
                runRejectPrmRole_badRequest(testArguments, true, false, false);
            } catch (AssertionError ex) {
                registerError(ex);
                throw ex;
            }
        }

        @MethodSource("getOrgRoleTestArguments")
        @ParameterizedTest(name = DISPLAY_NAME)
        void testRejectPrmGaOrgRole_missingCaseType(String ignoredDisplayName,
                                                    PrmConfigTestArguments testArguments) throws Exception {

            createTestScenario(
                "testRejectPrmGaOrgRole_missingCaseType",
                REJECT_MISSING_CASE_TYPE,
                testArguments
            );

            try {
                runRejectPrmRole_badRequest(testArguments, false, true, false);
            } catch (AssertionError ex) {
                registerError(ex);
                throw ex;
            }
        }

    }


    private void runGrantAndDeletePrmRole(PrmConfigTestArguments testArguments,
                                          boolean isGaGroupRoleTest) throws Exception {

        // GIVEN
        var uidAssignee = UUID.randomUUID().toString();

        // create PRM role assignment request
        AssignmentRequest assignmentRequestPrmRole = createPrmRoleAssignmentRequest(
            uidAssignee,
            testArguments.getJurisdiction(),
            testArguments.getCaseType(),
            testArguments.getRoleName(),
            testArguments.getCaseAccessGroupId() // NB: is none-null for a GA Group Role
        );

        // NB: CaseAccessGroupId always non-null for Group Role test and null for Org role test
        assertEquals(
            isGaGroupRoleTest,
            StringUtils.isNotBlank(testArguments.getCaseAccessGroupId()),
            "Unexpected CaseAccessGroupId value for test"
        );

        // WHEN (Grant)
        MvcResult result = mockMvc.perform(post(URL_CREATE_ROLES)
                                               .contentType(JSON_CONTENT_TYPE)
                                               .headers(getHttpHeaders(AUTHORISED_SERVICE_ORM))
                                               .content(mapper.writeValueAsBytes(assignmentRequestPrmRole))
        ).andExpect(status().is(201)).andReturn();
        testScenario.addRasFilesToStep(STEP_GRANT, result);

        // THEN (Grant)
        assertCreateRoleAssignmentResponseStatus(Status.APPROVED, result, 1);

        // load role assignments
        List<RoleAssignment> assigneeRolesAfterGrant = assertRoleAssignmentsInDb(uidAssignee, 1);
        testScenario.addFileToStep(STEP_AFTER_GRANT, "assigneeRoles_afterGrant", assigneeRolesAfterGrant);

        RoleAssignment prmRole = assertPrmRoleAssignmentValues(
            uidAssignee,
            assigneeRolesAfterGrant,
            testArguments.getJurisdiction(),
            testArguments.getCaseType(),
            testArguments.getRoleName(),
            testArguments.getCaseAccessGroupId() // NB: is none-null for a GA Group Role
        );

        // WHEN / THEN (Delete)
        List<RoleAssignment> assigneeRolesAfterDelete = assertSuccessfulPrmRoleDeletion(prmRole);
        testScenario.addFileToStep(STEP_AFTER_DELETE, "assigneeRoles_afterDelete", assigneeRolesAfterDelete);
    }


    private void runRejectPrmRole_badRequest(PrmConfigTestArguments testArguments,
                                             boolean overrideJurisdiction,
                                             boolean missingCaseType,
                                             boolean missingCaseAccessGroupId) throws Exception {

        // GIVEN
        var uidAssignee = UUID.randomUUID().toString();

        // create PRM role assignment request
        AssignmentRequest assignmentRequestPrmRole = createPrmRoleAssignmentRequest(
            uidAssignee,
            overrideJurisdiction ? "bad-jurisdiction" : testArguments.getJurisdiction(),
            missingCaseType ? null : testArguments.getCaseType(),
            testArguments.getRoleName(),
            missingCaseAccessGroupId ? null : testArguments.getCaseAccessGroupId()
        );

        // WHEN (Grant)
        MvcResult result = mockMvc.perform(post(URL_CREATE_ROLES)
                                               .contentType(JSON_CONTENT_TYPE)
                                               .headers(getHttpHeaders(AUTHORISED_SERVICE_ORM))
                                               .content(mapper.writeValueAsBytes(assignmentRequestPrmRole))
        ).andExpect(status().is(422)).andReturn();
        testScenario.addRasFilesToStep(STEP_REJECT_GRANT, result);

        // THEN (Grant)
        assertCreateRoleAssignmentResponseStatus(Status.REJECTED, result, 1);

        // load role assignments
        List<RoleAssignment> assigneeRolesAfterReject = assertRoleAssignmentsInDb(uidAssignee, 0);
        testScenario.addFileToStep(STEP_AFTER_REJECT, "assigneeRoles_afterReject", assigneeRolesAfterReject);
    }


    private RoleAssignment assertPrmRoleAssignmentValues(String actorId,
                                                         List<RoleAssignment> roleAssignments,
                                                         String jurisdiction,
                                                         String caseType,
                                                         String roleName,
                                                         String caseAccessGroupId) {
        RoleAssignment prmRole = findPrmRole(roleAssignments, roleName);

        assertNotNull(prmRole, "PRM Role not found");
        assertEquals(actorId, prmRole.getActorId());
        assertEquals(ActorIdType.IDAM, prmRole.getActorIdType());
        assertEquals(RoleType.ORGANISATION, prmRole.getRoleType());
        assertEquals(RoleCategory.PROFESSIONAL, prmRole.getRoleCategory());
        assertEquals(roleName, prmRole.getRoleName());
        assertEquals(Classification.RESTRICTED, prmRole.getClassification());
        assertEquals(GrantType.STANDARD, prmRole.getGrantType());
        assertFalse(CollectionUtils.isEmpty(prmRole.getAttributes()));
        assertEquals(jurisdiction, prmRole.getAttributes().get("jurisdiction").asText());
        assertEquals(caseType, prmRole.getAttributes().get("caseType").asText());

        if (caseAccessGroupId != null) {
            assertEquals(caseAccessGroupId, prmRole.getAttributes().get("caseAccessGroupId").asText());
        } else {
            assertFalse(
                prmRole.getAttributes().containsKey("caseAccessGroupId"),
                "caseAccessGroupId should not be present"
            );
        }

        return prmRole;
    }

    private List<RoleAssignment> assertSuccessfulPrmRoleDeletion(RoleAssignment roleAssignment) throws Exception {

        // GIVEN
        String actorId = roleAssignment.getActorId();
        String assignmentId = roleAssignment.getId().toString();

        // WHEN
        MvcResult result = mockMvc.perform(delete(URL_DELETE_ROLES + "/" + assignmentId)
                                               .contentType(JSON_CONTENT_TYPE)
                                               .headers(getHttpHeaders(AUTHORISED_SERVICE_ORM))
        ).andExpect(status().is(204)).andReturn();
        testScenario.addRasFilesToStep(STEP_DELETE, result);

        // THEN
        // verify role assignment removed
        List<RoleAssignment> roleAssignmentsAfter = assertRoleAssignmentsInDb(actorId, 0);
        assertTrue(
            roleAssignmentsAfter.stream()
                .noneMatch(ra -> ra.getId().toString().equals(assignmentId)),
            "Role assignment should be deleted"
        );
        return roleAssignmentsAfter;
    }

    private RoleAssignment findPrmRole(List<RoleAssignment> roleAssignments,
                                        String prmRoleName) {
        return roleAssignments.stream()
            .filter(role ->
                        role.getRoleType() == RoleType.ORGANISATION
                            && role.getRoleCategory() == RoleCategory.PROFESSIONAL
                            && role.getRoleName().equals(prmRoleName))
            .findFirst()
            .orElse(null);
    }

    protected AssignmentRequest createPrmRoleAssignmentRequest(String actorId,
                                                               String jurisdiction,
                                                               String caseType,
                                                               String roleName,
                                                               String caseAccessGroupId) {

        Map<String, JsonNode> attributes = new HashMap<>();

        attributes.put("jurisdiction", convertValueJsonNode(jurisdiction));
        if (caseType != null) {
            attributes.put("caseType", convertValueJsonNode(caseType));
        }
        if (caseAccessGroupId != null) {
            attributes.put("caseAccessGroupId", convertValueJsonNode(caseAccessGroupId));
        }

        var request = Request.builder()
            .assignerId(TEST_AUTH_USER_ID)
            .process("professional-organisational-role-mapping")
            .reference(actorId)
            .replaceExisting(true)
            .build();

        var roleAssignment = RoleAssignment.builder()
            .actorId(actorId)
            .actorIdType(ActorIdType.IDAM)
            .roleType(RoleType.ORGANISATION)
            .roleCategory(RoleCategory.PROFESSIONAL)
            .roleName(roleName)
            .classification(Classification.RESTRICTED)
            .grantType(GrantType.STANDARD)
            .attributes(JacksonUtils.convertValue(attributes))
            .build();

        return AssignmentRequest.builder()
            .request(request)
            .requestedRoles(List.of(roleAssignment))
            .build();
    }

}
