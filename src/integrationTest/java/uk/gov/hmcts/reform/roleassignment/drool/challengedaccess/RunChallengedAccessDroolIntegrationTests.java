package uk.gov.hmcts.reform.roleassignment.drool.challengedaccess;

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
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.Case;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.ActorIdType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.drool.BaseDroolIntegrationTest;
import uk.gov.hmcts.reform.roleassignment.drool.helper.ReportWriter;
import uk.gov.hmcts.reform.roleassignment.drool.model.ChallengedAccessTestArguments;
import uk.gov.hmcts.reform.roleassignment.drool.model.TestScenario;

import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.roleassignment.util.JacksonUtils.convertValueJsonNode;

public class RunChallengedAccessDroolIntegrationTests extends BaseDroolIntegrationTest {

    public static final String DROOL_CA_TEST_OUTPUT_PATH = DROOL_TEST_OUTPUT_PATH + "ChallengedAccess/";

    static final String STEP_BEFORE_GRANT = "GIVEN: Before Grant";
    static final String STEP_GRANT = "WHEN: Grant Case-Role";
    static final String STEP_DELETE = "WHEN: Delete Case-Role";
    static final String STEP_REJECT_GRANT = "WHEN: Reject Grant Case-Role";
    static final String STEP_AFTER_GRANT = "THEN: After Grant";
    static final String STEP_AFTER_DELETE = "THEN: After Delete";
    static final String STEP_AFTER_REJECT = "THEN: After Reject";

    static final String TEST_AUTH_1 = "TestAuth1";
    static final String TEST_AUTH_2 = "TestAuth2";

    private static final String DISPLAY_NAME = "#{index} - {0}";

    private TestScenario testScenario;

    private static final List<TestScenario> testRun = new ArrayList<>();


    static class TestArgumentGenerator {

        private static List<ChallengedAccessTestArguments> getAllChallengedAccessTestArguments() {
            List<ChallengedAccessTestArguments> arguments = new ArrayList<>();

            arguments.addAll(FrChallengedAccessIT.getAllTestArguments());

            return arguments;
        }

        static Stream<Arguments> getDefaultSuccessTestArguments() {
            return getAllChallengedAccessTestArguments().stream()
                .filter(ChallengedAccessTestArguments::isExpectSuccess)
                .map(ChallengedAccessTestArguments::toArguments);
        }

        static Stream<Arguments> getDefaultSuccessTestArguments_withExistingRoleCaseType() {
            return getAllChallengedAccessTestArguments().stream()
                .filter(ChallengedAccessTestArguments::isExpectSuccess)
                .filter(arg -> arg.getExistingRoleCaseType() != null)
                .map(ChallengedAccessTestArguments::toArguments);
        }

        static Stream<Arguments> getDefaultFailureTestArguments() {
            return getAllChallengedAccessTestArguments().stream()
                .filter(arg -> !arg.isExpectSuccess())
                .map(ChallengedAccessTestArguments::toArguments);
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
        ReportWriter.writeTestReport("Challenged Access Drool Integration Tests",
                                     HappyPathTests.SUMMARY
                                        + NegativePathTests.SUMMARY,
                                     DROOL_CA_TEST_OUTPUT_PATH,
                                     RunChallengedAccessDroolIntegrationTests.testRun);
    }

    @BeforeEach
    void beforeEachTest() {
        // NB: reset the authenticated user's role-assignments before test
        persistenceService.deleteRoleAssignmentByActorId(TEST_AUTH_USER_ID);
    }

    @AfterEach
    void afterEachTest() {
        if (this.testScenario != null) {
            this.testScenario.writeToFile();
            RunChallengedAccessDroolIntegrationTests.testRun.add(testScenario);
        }
    }

    private void createTestScenario(String testName,
                                    String testDescription,
                                    ChallengedAccessTestArguments testArguments) {
        this.testScenario = new TestScenario(
            testName,
            testDescription,
            DROOL_CA_TEST_OUTPUT_PATH + "%s/" +  testName + "/",
            testArguments);
    }

    private void registerError(Error error) {
        if (this.testScenario != null) {
            this.testScenario.setError(error);
        }
    }

    @Nested
    class HappyPathTests extends TestArgumentGenerator {

        static final String GRANT_AND_DELETE = "Grant then Delete challenged-access case-role";
        static final String GRANT_AND_DELETE_WITH_REGION = GRANT_AND_DELETE + " - with different region";

        static final String SUMMARY =
            """
                <h2>Happy Paths</h2>
                <ul>
                <li>%s (with case-type filter if require)</li>
                <li>%s</li>
                </ul>
            """.formatted(GRANT_AND_DELETE, GRANT_AND_DELETE_WITH_REGION);

        @MethodSource("getDefaultSuccessTestArguments")
        @ParameterizedTest(name = DISPLAY_NAME)
        void testGrantAndDeleteChallengedAccessRole(String ignoredDisplayName,
                                                    ChallengedAccessTestArguments testArguments) throws Exception {

            createTestScenario("testGrantAndDeleteChallengedAccessRole",
                               GRANT_AND_DELETE,
                               testArguments);

            try {
                runGrantAndDeleteChallengedAccessRoleHappyPath(testArguments, false);
            } catch (AssertionError ex) {
                registerError(ex);
                throw ex;
            }
        }


        @MethodSource("getDefaultSuccessTestArguments")
        @ParameterizedTest(name = DISPLAY_NAME)
        void testGrantAndDeleteChallengedAccessRole_withRegionFilter(String ignoredDisplayName,
                                                                     ChallengedAccessTestArguments testArguments
        ) throws Exception {

            createTestScenario("testGrantAndDeleteChallengedAccessRole_withRegionFilter",
                               GRANT_AND_DELETE_WITH_REGION,
                               testArguments);

            try {
                runGrantAndDeleteChallengedAccessRoleHappyPath(testArguments, true);
            } catch (AssertionError ex) {
                registerError(ex);
                throw ex;
            }
        }


        private void runGrantAndDeleteChallengedAccessRoleHappyPath(ChallengedAccessTestArguments testArguments,
                                                                    boolean useRegionFilter) throws Exception {

            // GIVEN
            var actorId = TEST_AUTH_USER_ID;
            before_registerAndVerifyOrgRole(
                actorId,
                testArguments,
                // NB: Challenged-Access only permitted if user has role with different region to case (or no region)
                useRegionFilter ? "any-other-region" : null
            );

            final Case ccdCase = before_stubCaseinDataStoreResponse(testArguments);

            // create ChallengedAccess role assignment request
            AssignmentRequest assignmentRequestCaseRole = createChallengedAccessRoleAssignmentRequest(
                actorId,
                testArguments.getRoleCategory(),
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
            List<RoleAssignment> rolesAfterGrant = assertRoleAssignmentsInDb(actorId, 2);
            testScenario.addFileToStep(STEP_AFTER_GRANT, "rolesAfterGrant", rolesAfterGrant);

            RoleAssignment challengedAccessRole = assertChallengedAccessRoleAssignmentValues(
                actorId,
                rolesAfterGrant,
                testArguments,
                ccdCase
            );

            // WHEN / THEN (Delete)
            // NB: expected 1 role = 1 org role (i.e. ChallengedAccess role is deleted)
            List<RoleAssignment> rolesAfterDelete
                = assertSuccessfulChallengedAccessRoleDeletion(challengedAccessRole, 1);
            testScenario.addFileToStep(STEP_AFTER_DELETE, "rolesAfterDelete", rolesAfterDelete);

        }

    }

    @Nested
    class NegativePathTests extends TestArgumentGenerator {

        static final String REJECT_BAD_JURISDICTION = "Reject challenged-access case-role - bad jurisdiction";
        static final String REJECT_BAD_CASE_TYPE = "Reject challenged-access case-role - bad case-type";
        static final String REJECT_BAD_REGION = "Reject challenged-access case-role - with matching region";
        static final String REJECT_NOT_PERMITTED = "Reject challenged-access case-role - not permitted";

        static final String SUMMARY =
            """
                <h2>Negative Paths</h2>
                <ul>
                <li>%s</li>
                <li>%s</li>
                <li>%s</li>
                <li>%s (e.g. not a substantive role)</li>
                </ul>
            """.formatted(REJECT_BAD_JURISDICTION,
                          REJECT_BAD_CASE_TYPE,
                          REJECT_BAD_REGION,
                          REJECT_NOT_PERMITTED);

        @MethodSource("getDefaultSuccessTestArguments")
        @ParameterizedTest(name = DISPLAY_NAME)
        void testRejectChallengedAccessRole_badExistingRoleJurisdiction(String ignoredDisplayName,
                                                                        ChallengedAccessTestArguments testArguments
        ) throws Exception {

            createTestScenario("testRejectChallengedAccessRole_badExistingRoleJurisdiction",
                               REJECT_BAD_JURISDICTION,
                               testArguments);

            try {
                runRejectChallengedAccessRole(testArguments, true, false, false);
            } catch (AssertionError ex) {
                registerError(ex);
                throw ex;
            }
        }

        @MethodSource("getDefaultSuccessTestArguments_withExistingRoleCaseType")
        @ParameterizedTest(name = DISPLAY_NAME)
        void testRejectChallengedAccessRole_badExistingRoleCaseType(String ignoredDisplayName,
                                                                    ChallengedAccessTestArguments testArguments
        ) throws Exception {

            createTestScenario("testRejectChallengedAccessRole_badExistingRoleCaseType",
                               REJECT_BAD_CASE_TYPE,
                               testArguments);
            try {
                runRejectChallengedAccessRole(testArguments, false, true, false);
            } catch (AssertionError ex) {
                registerError(ex);
                throw ex;
            }
        }

        @MethodSource("getDefaultSuccessTestArguments")
        @ParameterizedTest(name = DISPLAY_NAME)
        void testRejectChallengedAccessRole_matchingExistingRoleRegion(String ignoredDisplayName,
                                                                       ChallengedAccessTestArguments testArguments
        ) throws Exception {

            createTestScenario("testRejectChallengedAccessRole_matchingExistingRoleRegion",
                               REJECT_BAD_REGION,
                               testArguments);

            try {
                runRejectChallengedAccessRole(testArguments, false, false, true);
            } catch (AssertionError ex) {
                registerError(ex);
                throw ex;
            }
        }

        @MethodSource("getDefaultFailureTestArguments")
        @ParameterizedTest(name = DISPLAY_NAME)
        void testRejectChallengedAccessRole_existingRoleNotPermitted(String ignoredDisplayName,
                                                                     ChallengedAccessTestArguments testArguments
        ) throws Exception {

            createTestScenario("testRejectChallengedAccessRole_badExistingRoleRegion",
                               REJECT_NOT_PERMITTED,
                               testArguments);

            try {
                runRejectChallengedAccessRole(testArguments, false, false, false);
            } catch (AssertionError ex) {
                registerError(ex);
                throw ex;
            }
        }


        private void runRejectChallengedAccessRole(ChallengedAccessTestArguments testArguments,
                                                   boolean useBadJurisdiction,
                                                   boolean useBadCaseType,
                                                   boolean useBadRegion) throws Exception {

            // GIVEN
            var actorId = TEST_AUTH_USER_ID;
            if (useBadJurisdiction || useBadCaseType) {
                before_registerAndVerifyBadOrgRole(
                    actorId,
                    testArguments,
                    useBadJurisdiction,
                    useBadCaseType
                );
            } else {
                before_registerAndVerifyOrgRole(
                    actorId,
                    testArguments,
                    // NB: Challenged-Access not permitted if user has role with matching region to case
                    useBadRegion ? CASE_REGION_ID : null
                );
            }

            final Case ccdCase = before_stubCaseinDataStoreResponse(testArguments);

            // create ChallengedAccess role assignment request
            AssignmentRequest assignmentRequestCaseRole = createChallengedAccessRoleAssignmentRequest(
                actorId,
                testArguments.getRoleCategory(),
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
            List<RoleAssignment> rolesAfterReject = assertRoleAssignmentsInDb(actorId, 1);
            testScenario.addFileToStep(STEP_AFTER_REJECT, "rolesAfterReject", rolesAfterReject);

        }

    }


    private RoleAssignment assertChallengedAccessRoleAssignmentValues(String actorId,
                                                                      List<RoleAssignment> roleAssignments,
                                                                      ChallengedAccessTestArguments testArguments,
                                                                      Case ccdCase) {
        RoleAssignment roleAssignment = findChallengedAccessRole(roleAssignments);

        assertNotNull(roleAssignment, "ChallengedAccess role not found");
        assertEquals(actorId, roleAssignment.getActorId());
        assertEquals(ActorIdType.IDAM, roleAssignment.getActorIdType());
        assertEquals(RoleType.CASE, roleAssignment.getRoleType());
        assertEquals(getRoleNameForChallengedAccess(testArguments.getRoleCategory()), roleAssignment.getRoleName());
        // NB: case-role classification should match case
        assertEquals(ccdCase.getSecurityClassification(), roleAssignment.getClassification());
        assertEquals(GrantType.CHALLENGED, roleAssignment.getGrantType());
        assertEquals(testArguments.getRoleCategory(), roleAssignment.getRoleCategory());
        assertFalse(CollectionUtils.isEmpty(roleAssignment.getAttributes()));
        assertEquals(ccdCase.getJurisdiction(), roleAssignment.getAttributes().get("jurisdiction").asText());
        assertEquals(ccdCase.getCaseTypeId(), roleAssignment.getAttributes().get("caseType").asText());
        assertEquals(ccdCase.getId(), roleAssignment.getAttributes().get("caseId").asText());

        // end time is mandatory
        assertNotNull(roleAssignment.getEndTime());

        // verify authorisations have been copied across from org role
        assertNotNull(roleAssignment.getAuthorisations());
        assertEquals(2, roleAssignment.getAuthorisations().size());
        assertTrue(roleAssignment.getAuthorisations().containsAll(List.of(TEST_AUTH_1, TEST_AUTH_2)));

        return roleAssignment;
    }


    @SuppressWarnings({"SameParameterValue"})
    private List<RoleAssignment> assertSuccessfulChallengedAccessRoleDeletion(RoleAssignment roleAssignment,
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

    private RoleAssignment findChallengedAccessRole(List<RoleAssignment> roleAssignments) {
        return roleAssignments.stream()
            .filter(role -> role.getGrantType().equals(GrantType.CHALLENGED))
            .findFirst()
            .orElse(null);
    }

    private Case before_stubCaseinDataStoreResponse(ChallengedAccessTestArguments testArguments) {

        Case ccdCase = mockRetrieveDataServiceGetCaseById(
            CASE_ID,
            testArguments.getJurisdiction(),
            testArguments.getCaseType(),
            CASE_REGION_ID
        );
        testScenario.addFileToStep(STEP_BEFORE_GRANT, "ccdCase", ccdCase);

        return ccdCase;
    }

    private void before_registerAndVerifyOrgRole(String actorId,
                                                 ChallengedAccessTestArguments testArguments,
                                                 String region) throws Exception {
        List<RoleAssignment> rolesBefore = registerAndVerifyOrgRoleAssignment(
            actorId,
            testArguments.getRoleCategory(),
            testArguments.getExistingRoleName(),
            testArguments.getJurisdiction(),
            testArguments.getExistingRoleCaseType(),
            region,
            List.of(TEST_AUTH_1, TEST_AUTH_2)
        );
        testScenario.addFileToStep(STEP_BEFORE_GRANT, "rolesBefore", rolesBefore);
    }

    private void before_registerAndVerifyBadOrgRole(String actorId,
                                                    ChallengedAccessTestArguments testArguments,
                                                    boolean overrideJurisdiction,
                                                    boolean overrideCaseType) throws Exception {
        // generate the org role assignment as normal to use as a template
        List<RoleAssignment> rolesBefore = registerAndVerifyOrgRoleAssignment(
            actorId,
            testArguments.getRoleCategory(),
            testArguments.getExistingRoleName(),
            testArguments.getJurisdiction(),
            testArguments.getExistingRoleCaseType(),
            null,
            List.of(TEST_AUTH_1, TEST_AUTH_2)
        );

        // then override the bits we want to change for the test
        overrideRoleAssignmentValuesInDb(rolesBefore.get(0),
                                         false,
                                         overrideJurisdiction,
                                         overrideCaseType,
                                         false);

        // reset output to match DB
        rolesBefore = assertRoleAssignmentsInDb(actorId, 1);
        testScenario.addFileToStep(STEP_BEFORE_GRANT, "rolesBefore", rolesBefore);
    }

    private AssignmentRequest createChallengedAccessRoleAssignmentRequest(String actorId,
                                                                          RoleCategory roleCategory,
                                                                          String jurisdiction,
                                                                          String caseId) {

        String roleName = getRoleNameForChallengedAccess(roleCategory);

        // create standard case-role request
        var assignmentRequest = createCaseRoleAssignmentRequest(
            actorId,
            actorId, // NB: challenged-access is self-service, assigner == assignee
            roleCategory,
            roleName,
            jurisdiction,
            caseId
        );

        // override elements we know will be different for challenged-access requests
        var request = assignmentRequest.getRequest();
        request.setProcess("challenged-access");
        request.setReference(caseId + "/" + roleName + "/" + actorId);

        // override elements we know will be different for challenged-access case-role assignments
        var roleAssignment = assignmentRequest.getRequestedRoles().stream().findFirst().orElseThrow();
        roleAssignment.setGrantType(GrantType.CHALLENGED);
        roleAssignment.setEndTime(ZonedDateTime.now(ZoneOffset.UTC).with(
            LocalTime.of(23, 59, 59)
        ));

        // add notes
        String[] notes = { "any", "array", "will-do" };
        roleAssignment.setNotes(convertValueJsonNode(notes));

        return assignmentRequest;
    }

    private String getRoleNameForChallengedAccess(RoleCategory roleCategory) {
        return switch (roleCategory) {
            case ADMIN -> "challenged-access-admin";
            case CTSC -> "challenged-access-ctsc";
            case JUDICIAL -> "challenged-access-judiciary";
            case LEGAL_OPERATIONS -> "challenged-access-legal-ops";
            default -> throw new IllegalArgumentException("Unsupported role category: " + roleCategory);
        };
    }

}
