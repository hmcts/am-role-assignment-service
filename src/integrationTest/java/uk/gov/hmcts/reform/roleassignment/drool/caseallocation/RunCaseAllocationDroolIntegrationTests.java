package uk.gov.hmcts.reform.roleassignment.drool.caseallocation;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.Case;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.drool.BaseDroolIntegrationTest;
import uk.gov.hmcts.reform.roleassignment.drool.helper.ReportWriter;
import uk.gov.hmcts.reform.roleassignment.drool.model.CaseAllocatorTestArguments;
import uk.gov.hmcts.reform.roleassignment.drool.model.TestScenario;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
class RunCaseAllocationDroolIntegrationTests extends BaseDroolIntegrationTest {

    public static final String DROOL_CA_TEST_OUTPUT_PATH = DROOL_TEST_OUTPUT_PATH + "CaseAllocation/";

    static final String STEP_BEFORE_GRANT = "GIVEN: Before Grant";
    static final String STEP_BEFORE_DELETE = "GIVEN: Before Delete";
    static final String STEP_GRANT = "WHEN: Grant Case-Role";
    static final String STEP_DELETE = "WHEN: Delete Case-Role";
    static final String STEP_REJECT_GRANT = "WHEN: Reject Grant Case-Role";
    static final String STEP_REJECT_DELETE = "WHEN: Reject Delete Case-Role";
    static final String STEP_AFTER_GRANT = "THEN: After Grant";
    static final String STEP_AFTER_DELETE = "THEN: After Delete";
    static final String STEP_AFTER_REJECT = "THEN: After Reject";

    private static final String DISPLAY_NAME = "#{index} - {0}";

    private TestScenario testScenario;

    private static final List<TestScenario> testRun = new ArrayList<>();


    static class TestArgumentGenerator {

        static List<CaseAllocatorTestArguments> getAllCaseAllocatorTestArguments() {
            List<CaseAllocatorTestArguments> arguments = new ArrayList<>();

            arguments.addAll(FrCaseAllocatorIT.getAllTestArguments());

            return arguments;
        }

        static Stream<Arguments> getTestArguments() {
            return getAllCaseAllocatorTestArguments().stream()
                .map(CaseAllocatorTestArguments::toArguments);
        }

        static Stream<Arguments> getTestArguments_withExistingRoleCaseType() {
            return getAllCaseAllocatorTestArguments().stream()
                .filter(arg -> arg.getExistingRoleCaseType() != null)
                .map(CaseAllocatorTestArguments::toArguments);
        }

        static Stream<Arguments> getTestArguments_withCaAlwaysUseCaseType() {
            return getAllCaseAllocatorTestArguments().stream()
                .filter(CaseAllocatorTestArguments::isCaAlwaysUseCaseType)
                .map(CaseAllocatorTestArguments::toArguments);
        }
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    @BeforeAll
    static void beforeAllTests() throws IOException {
        File outputDirectory = new File(DROOL_CA_TEST_OUTPUT_PATH);
        if (outputDirectory.exists()) {
            FileUtils.deleteDirectory(outputDirectory);
        }
        outputDirectory.mkdirs();
    }

    @AfterAll
    static void afterAllTests() {
        ReportWriter.writeTestReport("Case Allocator Drool Integration Tests",
                                     HappyPathTests.SUMMARY
                                         + NegativeDeleteCaseRoleTests.SUMMARY
                                         + NegativeGrantCaseRoleTests.SUMMARY,
                                     DROOL_CA_TEST_OUTPUT_PATH,
                                     RunCaseAllocationDroolIntegrationTests.testRun);
    }

    @BeforeEach
    void beforeEachTest() {
        // NB: authenticated user is the assigner, i.e. case-allocator: reset their role-assignments before test
        persistenceService.deleteRoleAssignmentByActorId(TEST_AUTH_USER_ID);
    }

    @AfterEach
    void afterEachTest() {
        if (this.testScenario != null) {
            this.testScenario.writeToFile();
            RunCaseAllocationDroolIntegrationTests.testRun.add(testScenario);
        }
    }

    private void createTestScenario(String testName, String testDescription, CaseAllocatorTestArguments testArguments) {
        this.testScenario = new TestScenario(
            testName,
            testDescription,
            DROOL_CA_TEST_OUTPUT_PATH + "%s/" +  testName + "/",
            testArguments);
    }

    private void registerError(Error error) {
        if (this.testScenario != null) {
            this.testScenario.addError(error);
        }
    }

    @Nested
    class HappyPathTests extends TestArgumentGenerator {

        static final String GRANT_AND_DELETE = "Grant then Delete case-role";
        static final String GRANT_AND_DELETE_WITH_CA_REGION = GRANT_AND_DELETE + " - with CA region filter";

        static final String SUMMARY =
            """
                <h2>HappyPaths</h2>
                <ul>
                <li>%s (with CA case-type filter if required)</li>
                <li>%s</li>
                </ul>
            """.formatted(GRANT_AND_DELETE, GRANT_AND_DELETE_WITH_CA_REGION);

        @MethodSource("getTestArguments")
        @ParameterizedTest(name = DISPLAY_NAME)
        void testGrantAndDeleteCaseRole(String ignoredDisplayName,
                                        CaseAllocatorTestArguments testArguments) throws Exception {

            createTestScenario("testGrantAndDeleteCaseRole",
                               GRANT_AND_DELETE,
                               testArguments);

            try {
                runGrantAndDeleteCaseRoleHappyPath(testArguments, false);
            } catch (AssertionError ex) {
                registerError(ex);
                throw ex;
            }
        }


        @MethodSource("getTestArguments")
        @ParameterizedTest(name = DISPLAY_NAME)
        void testGrantAndDeleteCaseRole_withCaRegionFilter(String ignoredDisplayName,
                                                           CaseAllocatorTestArguments testArguments) throws Exception {

            createTestScenario("testGrantAndDeleteCaseRole_withCaRegionFilter",
                               GRANT_AND_DELETE_WITH_CA_REGION,
                               testArguments);

            try {
                runGrantAndDeleteCaseRoleHappyPath(testArguments, true);
            } catch (AssertionError ex) {
                registerError(ex);
                throw ex;
            }
        }

        private void runGrantAndDeleteCaseRoleHappyPath(CaseAllocatorTestArguments testArguments,
                                                        boolean useCaRegionFilter) throws Exception {

            // GIVEN

            // ASSIGNER
            var uidAssigner = TEST_AUTH_USER_ID; // NB: authenticated user is the assigner, i.e. case-allocator
            before_registerAndVerifyAssignerAsCaseAllocator(
                uidAssigner,
                testArguments,
                useCaRegionFilter ? CASE_REGION_ID : null
            );

            // ASSIGNEE
            var uidAssignee = UUID.randomUUID().toString(); // i.e. different from Assigner/Case-Allocator
            before_registerAndVerifyAssigneeOrgRole(uidAssignee, testArguments);

            // CASE
            final Case ccdCase = before_stubCaseinDataStoreResponse(testArguments);

            // create case role assignment request
            AssignmentRequest assignmentRequestCaseRole = createCaseRoleAssignmentRequest(
                uidAssigner,
                uidAssignee,
                testArguments.getRoleCategory(),
                testArguments.getCaseRoleName(),
                testArguments.getJurisdiction(),
                ccdCase.getId()
            );

            // WHEN (Grant)
            MvcResult result = mockMvc.perform(post(URL_CREATE_ROLES)
                                                   .contentType(JSON_CONTENT_TYPE)
                                                   .headers(getHttpHeaders(AUTHORISED_SERVICE_XUI))
                                                   .content(mapper.writeValueAsBytes(assignmentRequestCaseRole))
            ).andExpect(status().is(201)).andReturn();
            testScenario.addRasFilesToStep(STEP_GRANT, result);

            // THEN (Grant)
            assertCreateRoleAssignmentResponseStatus(Status.APPROVED, result, 1);

            // load role assignments
            List<RoleAssignment> assigneeRolesAfterGrant = assertRoleAssignmentsInDb(uidAssignee, 2);
            testScenario.addFileToStep(STEP_AFTER_GRANT, "assigneeRoles_afterGrant", assigneeRolesAfterGrant);

            RoleAssignment caseRole = assertCaseRoleAssignmentValues(
                uidAssignee,
                assigneeRolesAfterGrant,
                testArguments,
                ccdCase
            );

            // WHEN / THEN (Delete)
            // NB: expected 1 role = 1 org role (i.e. case-role is deleted)
            List<RoleAssignment> assigneeRolesAfterDelete = assertSuccessfulCaseRoleDeletion(caseRole, 1);
            testScenario.addFileToStep(STEP_AFTER_DELETE, "assigneeRoles_afterDelete", assigneeRolesAfterDelete);

        }

    }


    @Nested
    class NegativeDeleteCaseRoleTests extends TestArgumentGenerator {

        static final String REJECT_BAD_CA_JURISDICTION = "Reject delete case-role - bad CA jurisdiction";
        static final String REJECT_BAD_CA_REGION = "Reject delete case-role - bad CA region";
        static final String REJECT_BAD_CA_CASE_TYPE = "Reject delete case-role - bad CA case-type";

        static final String SUMMARY =
            """
                <h2>Negative Paths - Reject Delete</h2>
                <ul>
                <li>%s</li>
                <li>%s</li>
                <li>%s (if CA case-type filter is mandatory)</li>
                </ul>
            """.formatted(REJECT_BAD_CA_JURISDICTION,
                          REJECT_BAD_CA_REGION,
                          REJECT_BAD_CA_CASE_TYPE);

        @MethodSource("getTestArguments")
        @ParameterizedTest(name = DISPLAY_NAME)
        void testRejectDeleteCaseRole_badCaJurisdiction(String ignoredDisplayName,
                                                        CaseAllocatorTestArguments testArguments) throws Exception {
            createTestScenario("testRejectDeleteCaseRole_badCaJurisdiction",
                               REJECT_BAD_CA_JURISDICTION,
                               testArguments);

            try {
                runRejectDeleteCaseRole_badCaseAllocator(testArguments, true, false, false);
            } catch (AssertionError ex) {
                registerError(ex);
                throw ex;
            }
        }

        @MethodSource("getTestArguments_withCaAlwaysUseCaseType")
        @ParameterizedTest(name = DISPLAY_NAME)
        void testRejectDeleteCaseRole_badCaCaseType(String ignoredDisplayName,
                                                    CaseAllocatorTestArguments testArguments) throws Exception {
            createTestScenario("testRejectDeleteCaseRole_badCaCaseType",
                               REJECT_BAD_CA_CASE_TYPE,
                               testArguments);

            try {
                runRejectDeleteCaseRole_badCaseAllocator(testArguments, false, true, false);
            } catch (AssertionError ex) {
                registerError(ex);
                throw ex;
            }
        }

        @MethodSource("getTestArguments")
        @ParameterizedTest(name = DISPLAY_NAME)
        void testRejectDeleteCaseRole_badCaRegion(String ignoredDisplayName,
                                                  CaseAllocatorTestArguments testArguments) throws Exception {
            createTestScenario("testRejectDeleteCaseRole_badCaRegion",
                               REJECT_BAD_CA_REGION,
                               testArguments);

            try {
                runRejectDeleteCaseRole_badCaseAllocator(testArguments, false, false, true);
            } catch (AssertionError ex) {
                registerError(ex);
                throw ex;
            }
        }

        private void runRejectDeleteCaseRole_badCaseAllocator(CaseAllocatorTestArguments testArguments,
                                                              boolean overrideJurisdiction,
                                                              boolean overrideCaseType,
                                                              boolean overrideRegion) throws Exception {

            // GIVEN

            // ASSIGNER
            var uidAssigner = TEST_AUTH_USER_ID; // NB: authenticated user is the assigner, i.e. case-allocator
            before_registerAndVerifyAssignerAsCaseAllocator(uidAssigner, testArguments, null);

            // ASSIGNEE
            var uidAssignee = UUID.randomUUID().toString(); // i.e. different from Assigner/Case-Allocator
            before_registerAndVerifyAssigneeOrgRole(uidAssignee, testArguments);

            // CASE
            final Case ccdCase = before_stubCaseinDataStoreResponse(testArguments);

            // create case role assignment request
            AssignmentRequest assignmentRequestCaseRole = createCaseRoleAssignmentRequest(
                uidAssigner,
                uidAssignee,
                testArguments.getRoleCategory(),
                testArguments.getCaseRoleName(),
                testArguments.getJurisdiction(),
                ccdCase.getId()
            );

            // WHEN (Grant)
            MvcResult result = mockMvc.perform(post(URL_CREATE_ROLES)
                                                   .contentType(JSON_CONTENT_TYPE)
                                                   .headers(getHttpHeaders(AUTHORISED_SERVICE_XUI))
                                                   .content(mapper.writeValueAsBytes(assignmentRequestCaseRole))
            ).andExpect(status().is(201)).andReturn();
            testScenario.addRasFilesToStep(STEP_GRANT, result);

            // THEN (Grant)
            assertCreateRoleAssignmentResponseStatus(Status.APPROVED, result, 1);

            // load role assignments
            List<RoleAssignment> assigneeRolesAfterGrant = assertRoleAssignmentsInDb(uidAssignee, 2);
            testScenario.addFileToStep(STEP_AFTER_GRANT, "assigneeRoles_afterGrant", assigneeRolesAfterGrant);

            RoleAssignment caseRole = assertCaseRoleAssignmentValues(
                uidAssignee,
                assigneeRolesAfterGrant,
                testArguments,
                ccdCase
            );

            // WHEN / THEN (Delete)

            // find then override the Assigner roles with changes for the test
            overrideRoleAssignmentValuesInDb(
                assertRoleAssignmentsInDb(uidAssigner, 1).get(0),
                false,
                overrideJurisdiction,
                overrideCaseType,
                overrideRegion
            );
            testScenario.addFileToStep(STEP_BEFORE_DELETE, "assignerRoles", assertRoleAssignmentsInDb(uidAssigner, 1));

            // NB: expected 2 roles = 1 org role + 1 case role
            List<RoleAssignment> assigneeRolesAfterReject = assertFailedCaseRoleDeletion(caseRole, 2);
            testScenario.addFileToStep(STEP_AFTER_REJECT, "assigneeRoles_afterReject", assigneeRolesAfterReject);

        }

    }


    @Nested
    class NegativeGrantCaseRoleTests extends TestArgumentGenerator {

        static final String REJECT_BAD_ROLE_NAME = "Reject case-role - bad existing role name";
        static final String REJECT_BAD_ROLE_JURISDICTION = "Reject case-role - bad existing role jurisdiction";
        static final String REJECT_BAD_ROLE_CASE_TYPE = "Reject delete case-role - bad existing role case-type";

        static final String REJECT_BAD_CA_JURISDICTION = "Reject case-role - bad CA jurisdiction";
        static final String REJECT_BAD_CA_REGION = "Reject case-role - bad CA region";
        static final String REJECT_BAD_CA_CASE_TYPE = "Reject case-role - bad CA case-type";

        static final String SUMMARY =
            """
                <h2>Negative Paths - Reject Grant</h2>
                <ul>
                <li>%s</li>
                <li>%s</li>
                <li>%s (if check is appropriate)</li>
                </ul>

                <ul>
                <li>%s</li>
                <li>%s</li>
                <li>%s (if CA case-type filter is mandatory)</li>
                </ul>
            """.formatted(REJECT_BAD_ROLE_NAME,
                          REJECT_BAD_ROLE_JURISDICTION,
                          REJECT_BAD_ROLE_CASE_TYPE,
                          REJECT_BAD_CA_JURISDICTION,
                          REJECT_BAD_CA_REGION,
                          REJECT_BAD_CA_CASE_TYPE);

        @MethodSource("getTestArguments")
        @ParameterizedTest(name = DISPLAY_NAME)
        void testRejectCaseRole_badExistingRoleName(String ignoredDisplayName,
                                                    CaseAllocatorTestArguments testArguments) throws Exception {

            createTestScenario("testRejectCaseRole_badExistingRoleName",
                               REJECT_BAD_ROLE_NAME,
                               testArguments);

            try {
                runRejectCaseRole_badExistingRole(testArguments, true, false, false);
            } catch (AssertionError ex) {
                registerError(ex);
                throw ex;
            }
        }

        @MethodSource("getTestArguments")
        @ParameterizedTest(name = DISPLAY_NAME)
        void testRejectCaseRole_badExistingRoleJurisdiction(String ignoredDisplayName,
                                                            CaseAllocatorTestArguments testArguments) throws Exception {

            createTestScenario("testRejectCaseRole_badExistingRoleJurisdiction",
                               REJECT_BAD_ROLE_JURISDICTION,
                               testArguments);
            try {
                runRejectCaseRole_badExistingRole(testArguments, false, true, false);
            } catch (AssertionError ex) {
                registerError(ex);
                throw ex;
            }
        }

        @MethodSource("getTestArguments_withExistingRoleCaseType")
        @ParameterizedTest(name = DISPLAY_NAME)
        void testRejectCaseRole_badExistingRoleCaseType(String ignoredDisplayName,
                                                        CaseAllocatorTestArguments testArguments) throws Exception {

            createTestScenario("testRejectCaseRole_badExistingRoleCaseType",
                               REJECT_BAD_ROLE_CASE_TYPE,
                               testArguments);

            try {
                runRejectCaseRole_badExistingRole(testArguments, false, false, true);
            } catch (AssertionError ex) {
                registerError(ex);
                throw ex;
            }
        }

        private void runRejectCaseRole_badExistingRole(CaseAllocatorTestArguments testArguments,
                                                       boolean overrideRoleName,
                                                       boolean overrideJurisdiction,
                                                       boolean overrideCaseType) throws Exception {

            // GIVEN

            // ASSIGNER
            var uidAssigner = TEST_AUTH_USER_ID; // NB: authenticated user is the assigner, i.e. case-allocator
            before_registerAndVerifyAssignerAsCaseAllocator(uidAssigner, testArguments, null);

            // ASSIGNEE
            var uidAssignee = UUID.randomUUID().toString(); // i.e. different from Assigner/Case-Allocator
            before_registerAndVerifyBadAssigneeOrgRole(
                uidAssignee,
                testArguments,
                overrideRoleName,
                overrideJurisdiction,
                overrideCaseType
            );

            // CASE
            final Case ccdCase = before_stubCaseinDataStoreResponse(testArguments);

            // create case role assignment request
            AssignmentRequest assignmentRequestCaseRole = createCaseRoleAssignmentRequest(
                uidAssigner,
                uidAssignee,
                testArguments.getRoleCategory(),
                testArguments.getCaseRoleName(),
                testArguments.getJurisdiction(),
                ccdCase.getId()
            );

            // WHEN (Grant)
            MvcResult result = mockMvc.perform(post(URL_CREATE_ROLES)
                                                   .contentType(JSON_CONTENT_TYPE)
                                                   .headers(getHttpHeaders(AUTHORISED_SERVICE_XUI))
                                                   .content(mapper.writeValueAsBytes(assignmentRequestCaseRole))
            ).andExpect(status().is(422)).andReturn();
            testScenario.addRasFilesToStep(STEP_REJECT_GRANT, result);

            // THEN (Grant)
            assertCreateRoleAssignmentResponseStatus(Status.REJECTED, result, 1);

            // load role assignments
            List<RoleAssignment> assigneeRolesAfterReject = assertRoleAssignmentsInDb(uidAssignee, 1);
            testScenario.addFileToStep(STEP_AFTER_REJECT, "assigneeRoles_afterReject", assigneeRolesAfterReject);
        }


        @MethodSource("getTestArguments")
        @ParameterizedTest(name = DISPLAY_NAME)
        void testRejectCaseRole_badCaJurisdiction(String ignoredDisplayName,
                                                  CaseAllocatorTestArguments testArguments) throws Exception {
            createTestScenario("testRejectCaseRole_badCaJurisdiction",
                               REJECT_BAD_CA_JURISDICTION,
                               testArguments);

            try {
                runRejectCaseRole_badCaseAllocator(testArguments, true, false, false);
            } catch (AssertionError ex) {
                registerError(ex);
                throw ex;
            }
        }

        @MethodSource("getTestArguments_withCaAlwaysUseCaseType")
        @ParameterizedTest(name = DISPLAY_NAME)
        void testRejectCaseRole_badCaCaseType(String ignoredDisplayName,
                                              CaseAllocatorTestArguments testArguments) throws Exception {
            createTestScenario("testRejectCaseRole_badCaCaseType",
                               REJECT_BAD_CA_CASE_TYPE,
                               testArguments);

            try {
                runRejectCaseRole_badCaseAllocator(testArguments, false, true, false);
            } catch (AssertionError ex) {
                registerError(ex);
                throw ex;
            }
        }

        @MethodSource("getTestArguments")
        @ParameterizedTest(name = DISPLAY_NAME)
        void testRejectCaseRole_badCaRegion(String ignoredDisplayName,
                                            CaseAllocatorTestArguments testArguments) throws Exception {
            createTestScenario("testRejectCaseRole_badCaRegion",
                               REJECT_BAD_CA_REGION,
                               testArguments);

            try {
                runRejectCaseRole_badCaseAllocator(testArguments, false, false, true);
            } catch (AssertionError ex) {
                registerError(ex);
                throw ex;
            }
        }

        private void runRejectCaseRole_badCaseAllocator(CaseAllocatorTestArguments testArguments,
                                                        boolean overrideJurisdiction,
                                                        boolean overrideCaseType,
                                                        boolean overrideRegion) throws Exception {

            // GIVEN

            // ASSIGNER
            var uidAssigner = TEST_AUTH_USER_ID; // NB: authenticated user is the assigner, i.e. case-allocator
            before_registerAndVerifyBadAssignerAsCaseAllocator(
                uidAssigner,
                testArguments,
                overrideJurisdiction,
                overrideCaseType,
                overrideRegion
            );

            // ASSIGNEE
            var uidAssignee = UUID.randomUUID().toString(); // i.e. different from Assigner/Case-Allocator
            before_registerAndVerifyAssigneeOrgRole(uidAssignee, testArguments);

            // CASE
            final Case ccdCase = before_stubCaseinDataStoreResponse(testArguments);

            // create case role assignment request
            AssignmentRequest assignmentRequestCaseRole = createCaseRoleAssignmentRequest(
                uidAssigner,
                uidAssignee,
                testArguments.getRoleCategory(),
                testArguments.getCaseRoleName(),
                testArguments.getJurisdiction(),
                ccdCase.getId()
            );

            // WHEN (Grant)
            MvcResult result = mockMvc.perform(post(URL_CREATE_ROLES)
                                                   .contentType(JSON_CONTENT_TYPE)
                                                   .headers(getHttpHeaders(AUTHORISED_SERVICE_XUI))
                                                   .content(mapper.writeValueAsBytes(assignmentRequestCaseRole))
            ).andExpect(status().is(422)).andReturn();
            testScenario.addRasFilesToStep(STEP_REJECT_GRANT, result);

            // THEN (Grant)
            assertCreateRoleAssignmentResponseStatus(Status.REJECTED, result, 1);

            // load role assignments
            List<RoleAssignment> assigneeRolesAfterReject = assertRoleAssignmentsInDb(uidAssignee, 1);
            testScenario.addFileToStep(STEP_AFTER_REJECT, "assigneeRoles_afterReject", assigneeRolesAfterReject);
        }

    }


    @SuppressWarnings({"SameParameterValue"})
    private List<RoleAssignment> assertSuccessfulCaseRoleDeletion(RoleAssignment roleAssignment,
                                                                  int expectedRoleCount) throws Exception {

        // GIVEN
        String actorId = roleAssignment.getActorId();
        String assignmentId = roleAssignment.getId().toString();

        // WHEN
        MvcResult result = mockMvc.perform(delete(URL_DELETE_ROLES + "/" + assignmentId)
                                               .contentType(JSON_CONTENT_TYPE)
                                               .headers(getHttpHeaders(AUTHORISED_SERVICE_XUI))
        ).andExpect(status().is(204)).andReturn();
        testScenario.addRasFilesToStep(STEP_DELETE, result);

        // THEN
        // verify role assignment removed
        List<RoleAssignment> roleAssignmentsAfter = assertRoleAssignmentsInDb(actorId, expectedRoleCount);
        assertTrue(
            roleAssignmentsAfter.stream()
                .noneMatch(ra -> ra.getId().toString().equals(assignmentId)),
            "Role assignment should be deleted"
        );
        return roleAssignmentsAfter;
    }


    @SuppressWarnings({"SameParameterValue"})
    private List<RoleAssignment> assertFailedCaseRoleDeletion(RoleAssignment roleAssignment,
                                                              int expectedRoleCount) throws Exception {

        // GIVEN
        String actorId = roleAssignment.getActorId();
        String assignmentId = roleAssignment.getId().toString();

        // WHEN
        MvcResult result = mockMvc.perform(delete(URL_DELETE_ROLES + "/" + assignmentId)
                                               .contentType(JSON_CONTENT_TYPE)
                                               .headers(getHttpHeaders(AUTHORISED_SERVICE_XUI))
        ).andExpect(status().is(422)).andReturn(); // 422 - rejected
        testScenario.addRasFilesToStep(STEP_REJECT_DELETE, result);

        // THEN
        // verify role assignment still present
        List<RoleAssignment> roleAssignmentsAfter = assertRoleAssignmentsInDb(actorId, expectedRoleCount);
        assertTrue(
            roleAssignmentsAfter.stream()
                .anyMatch(ra -> ra.getId().toString().equals(assignmentId)),
            "Role assignment should still be present"
        );
        return roleAssignmentsAfter;
    }


    private RoleAssignment assertCaseRoleAssignmentValues(String actorId,
                                                          List<RoleAssignment> roleAssignments,
                                                          CaseAllocatorTestArguments testArguments,
                                                          Case ccdCase) {
        RoleAssignment caseRole = findCaseRole(roleAssignments, testArguments.getCaseRoleName());
        assertCaseRoleAssignmentDefaultValues(actorId,
                                              caseRole,
                                              testArguments.getCaseRoleName(),
                                              testArguments.getRoleCategory(),
                                              ccdCase);
        return caseRole;
    }

    private RoleAssignment findCaseRole(List<RoleAssignment> roleAssignments,
                                        String caseRoleName) {
        return roleAssignments.stream()
            .filter(role -> role.getRoleType() == RoleType.CASE && role.getRoleName().equals(caseRoleName))
            .findFirst()
            .orElse(null);
    }

    private Case before_stubCaseinDataStoreResponse(CaseAllocatorTestArguments testArguments) {

        Case ccdCase = mockRetrieveDataServiceGetCaseById(
            CASE_ID,
            testArguments.getJurisdiction(),
            testArguments.getCaseType(),
            CASE_REGION_ID
        );
        testScenario.addFileToStep(STEP_BEFORE_GRANT, "ccdCase", ccdCase);

        return ccdCase;
    }

    private void before_registerAndVerifyAssignerAsCaseAllocator(String actorId,
                                                                 CaseAllocatorTestArguments testArguments,
                                                                 String useCaRegion) throws Exception {

        List<RoleAssignment> assignerRoles = registerAndVerifyOrgRoleAssignment(
            actorId,
            testArguments.getCaRoleCategory(),
            ROLE_CASE_ALLOCATOR,
            testArguments.getJurisdiction(),
            testArguments.isCaAlwaysUseCaseType() ? testArguments.getCaseType() : null,
            useCaRegion
        );
        testScenario.addFileToStep(STEP_BEFORE_GRANT, "assignerRoles", assignerRoles);
    }


    private void before_registerAndVerifyBadAssignerAsCaseAllocator(String actorId,
                                                                    CaseAllocatorTestArguments testArguments,
                                                                    boolean overrideJurisdiction,
                                                                    boolean overrideCaseType,
                                                                    boolean overrideRegion) throws Exception {
        // generate the org role assignment as normal to use as a template
        List<RoleAssignment> assignerRoles = registerAndVerifyOrgRoleAssignment(
            actorId,
            testArguments.getCaRoleCategory(),
            ROLE_CASE_ALLOCATOR,
            testArguments.getJurisdiction(),
            testArguments.isCaAlwaysUseCaseType() ? testArguments.getCaseType() : null,
            null
        );

        // then override the bits we want to change for the test
        overrideRoleAssignmentValuesInDb(assignerRoles.get(0),
                                         false,
                                         overrideJurisdiction,
                                         overrideCaseType,
                                         overrideRegion);

        // reset output to match DB
        assignerRoles = assertRoleAssignmentsInDb(actorId, 1);
        testScenario.addFileToStep(STEP_BEFORE_GRANT, "assignerRoles", assignerRoles);
    }

    private void before_registerAndVerifyAssigneeOrgRole(String actorId,
                                                         CaseAllocatorTestArguments testArguments) throws Exception {
        List<RoleAssignment> assigneeRolesBefore = registerAndVerifyOrgRoleAssignment(
            actorId,
            testArguments.getRoleCategory(),
            testArguments.getExistingRoleName(),
            testArguments.getJurisdiction(),
            testArguments.getExistingRoleCaseType(),
            null
        );
        testScenario.addFileToStep(STEP_BEFORE_GRANT, "assigneeRoles_before", assigneeRolesBefore);
    }

    private void before_registerAndVerifyBadAssigneeOrgRole(String actorId,
                                                            CaseAllocatorTestArguments testArguments,
                                                            boolean overrideRoleName,
                                                            boolean overrideJurisdiction,
                                                            boolean overrideCaseType) throws Exception {
        // generate the org role assignment as normal to use as a template
        List<RoleAssignment> assigneeRolesBefore = registerAndVerifyOrgRoleAssignment(
            actorId,
            testArguments.getRoleCategory(),
            testArguments.getExistingRoleName(),
            testArguments.getJurisdiction(),
            testArguments.getExistingRoleCaseType(),
            null
        );

        // then override the bits we want to change for the test
        overrideRoleAssignmentValuesInDb(assigneeRolesBefore.get(0),
                                         overrideRoleName,
                                         overrideJurisdiction,
                                         overrideCaseType,
                                         false);

        // reset output to match DB
        assigneeRolesBefore = assertRoleAssignmentsInDb(actorId, 1);
        testScenario.addFileToStep(STEP_BEFORE_GRANT, "assigneeRoles_before", assigneeRolesBefore);
    }

    private void overrideRoleAssignmentValuesInDb(RoleAssignment roleAssignment,
                                                  boolean overrideRoleName,
                                                  boolean overrideJurisdiction,
                                                  boolean overrideCaseType,
                                                  boolean overrideRegion) {

        // delete current role assignment
        persistenceService.deleteRoleAssignment(roleAssignment);

        // reset roleAssignment ID ready for save as new
        roleAssignment.setId(UUID.randomUUID());

        if (overrideRoleName) {
            roleAssignment.setRoleName("bad-role-name");
        }
        if (overrideJurisdiction) {
            roleAssignment.setAttribute("jurisdiction", "bad-jurisdiction");
        }
        if (overrideCaseType) {
            roleAssignment.setAttribute("caseType", "bad-case-type");
        }
        if (overrideRegion) {
            roleAssignment.setAttribute("region", "bad-region");
        }

        persistenceService.persistRoleAssignments(List.of(roleAssignment));
    }

    private List<RoleAssignment> registerAndVerifyOrgRoleAssignment(String actorId,
                                                                    RoleCategory roleCategory,
                                                                    String roleName,
                                                                    String jurisdiction,
                                                                    String caseType,
                                                                    String region) throws Exception {

        // GIVEN
        AssignmentRequest assignmentRequest = createOrgRoleAssignmentRequest(
            actorId,
            roleCategory,
            roleName,
            jurisdiction,
            caseType,
            region
        );

        // WHEN
        log.info("Create RoleAssignment Request: {}", writeValueAsPrettyJson(assignmentRequest));
        MvcResult result = mockMvc.perform(post(URL_CREATE_ROLES)
                                               .contentType(JSON_CONTENT_TYPE)
                                               .headers(getHttpHeaders(AUTHORISED_SERVICE_ORM))
                                               .content(mapper.writeValueAsBytes(assignmentRequest))
        ).andExpect(status().is(201)).andReturn();

        // THEN
        assertCreateRoleAssignmentResponseStatus(Status.APPROVED, result, 1);

        // check role assignments
        return assertRoleAssignmentsInDb(actorId, 1);
    }

}
