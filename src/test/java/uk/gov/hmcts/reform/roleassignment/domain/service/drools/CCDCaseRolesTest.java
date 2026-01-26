package uk.gov.hmcts.reform.roleassignment.domain.service.drools;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.roleassignment.domain.model.FeatureFlag;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Classification;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.FeatureFlagEnum;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.RetrieveDataService;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType.SPECIFIC;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.CREATE_REQUESTED;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.DELETE_REQUESTED;
import static uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder.getRequestedCaseRole_ra;
import static uk.gov.hmcts.reform.roleassignment.util.JacksonUtils.convertValueJsonNode;

@ExtendWith(MockitoExtension.class)
class CCDCaseRolesTest extends DroolBase {

    @Test
    void shouldRejectCaseRequestedRolesForUnauthoriseRequest() {
        RoleAssignment requestedRole1 = getRequestedCaseRole_ra(RoleCategory.PROFESSIONAL,
                                   "[PETSOLICITOR]", SPECIFIC, "caseId",
                                 "1234567890123456", CREATE_REQUESTED);
        assignmentRequest.setRequestedRoles(List.of(requestedRole1));
        FeatureFlag featureFlag  =  FeatureFlag.builder()
            .status(true).build();
        featureFlags.add(featureFlag);

        buildExecuteKieSession();
        //assertion
        assignmentRequest.getRequestedRoles().forEach(ra -> assertEquals(Status.REJECTED, ra.getStatus()));

        //verify retrieveDataService is used as ClientId = null
        RetrieveDataService retrieveDataService = getRetrieveDataService();
        verify(retrieveDataService).getCaseById("1234567890123456");
    }

    @Test
    void shouldRejectCaseRequestedRolesForUnauthoriseRequestNoLoadCaseData() {
        RoleAssignment requestedRole1 = getRequestedCaseRole_ra(RoleCategory.PROFESSIONAL,
                                                                "[PETSOLICITOR]", SPECIFIC, "caseId",
                                                                "1234567890123456", CREATE_REQUESTED);
        assignmentRequest.setRequestedRoles(List.of(requestedRole1));
        assignmentRequest.getRequest().setClientId("ccd_data");
        FeatureFlag featureFlag  =  FeatureFlag.builder()
            .status(true).build();
        featureFlags.add(featureFlag);

        buildExecuteKieSession();
        //assertion
        assignmentRequest.getRequestedRoles().forEach(ra -> assertEquals(Status.REJECTED, ra.getStatus()));

        //verify retrieveDataService is not used as ClientId = ccd_data
        RetrieveDataService retrieveDataService = getRetrieveDataService();
        verifyNoInteractions(retrieveDataService);
    }

    @Test
    void shouldApproveCreaterCaseRole() {
        verifyCreateCaseRequestedRole_CCD_1_0("[CREATOR]", "ccd_data", RoleCategory.JUDICIAL);
        verifyCreateCaseRequestedRole_CCD_1_0("[CREATOR]", "aac_manage_case_assignment",
                                              RoleCategory.LEGAL_OPERATIONS);
        verifyCreateCaseRequestedRole_CCD_1_0("[CREATOR]", "aac_manage_case_assignment",
                                              RoleCategory.CITIZEN);

        //verify retrieveDataService is not used as ClientId = ccd_data or aac_manage_case_assignment
        RetrieveDataService retrieveDataService = getRetrieveDataService();
        verifyNoInteractions(retrieveDataService);
    }
    
    @ParameterizedTest
    @ValueSource(strings = {
        "[PETSOLICITOR]",
        "[LASOCIALWORKER]",
        "[LABARRISTER]",
        "[LAMANAGING]",
        "[LASOLICITOR]",
        "[LASHARED]"
    })
    void shouldApproveProfessionalCaseRole(String roleName) {
        verifyCreateCaseRequestedRole_CCD_1_0(roleName , "ccd_data", RoleCategory.PROFESSIONAL);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "[PETSOLICITOR]",
        "[LASOCIALWORKER]",
        "[LABARRISTER]",
        "[LAMANAGING]",
        "[LASOLICITOR]",
        "[LASHARED]"
    })
    void shouldRejectProfessionalCaseRole_WithWrongOrMissingValues(String roleName) {
        // without caseId
        verifyCcdCaseRequestedRole( RoleCategory.PROFESSIONAL,
                                   roleName,
                                   "IA",
                                   "Asylum",
                                   false, // WRONG
                                   Status.REJECTED);
        // wrong category
        verifyCcdCaseRequestedRole(RoleCategory.CITIZEN, // WRONG (NB: this is another valid CCD Case Role Category)
                                   roleName,
                                   "IA",
                                   "Asylum",
                                   true,
                                   Status.REJECTED);
    }
    
    private void verifyCreateCaseRequestedRole_CCD_1_0(String roleName, String clientId, RoleCategory category) {
        RoleAssignment requestedRole1 = getRequestedCaseRole_ra(category, roleName,
                                                             SPECIFIC, "caseId",
                                                             "1234567890123456", CREATE_REQUESTED);
        requestedRole1.setClassification(Classification.RESTRICTED);
        requestedRole1.getAttributes().putAll(Map.of("jurisdiction", convertValueJsonNode("IA"),
                                                     "caseType", convertValueJsonNode("Asylum"),
                                                     "caseId", convertValueJsonNode("1234567890123456")));
        assignmentRequest.setRequestedRoles(List.of(requestedRole1));
        assignmentRequest.getRequest().setClientId(clientId);

        FeatureFlag featureFlag  =  FeatureFlag.builder().build();
        featureFlags.add(featureFlag);

        buildExecuteKieSession();
        //assertion
        assignmentRequest.getRequestedRoles().forEach(ra -> assertEquals(Status.APPROVED, ra.getStatus()));
    }

    @Test
    void shouldApproveDummyCaseRoleCreation_CCD_1_0_enableByPassDroolRule() {
        RoleAssignment requestedRole1 = getRequestedCaseRole_ra(RoleCategory.SYSTEM, "[RESPSOLICITOR]",
                                                             SPECIFIC, "caseId",
                                                             "1234567890123456", CREATE_REQUESTED);
        requestedRole1.setClassification(Classification.RESTRICTED);
        requestedRole1.getAttributes().put("jurisdiction", convertValueJsonNode("BEFTA_MASTER"));
        assignmentRequest.setRequestedRoles(List.of(requestedRole1));
        assignmentRequest.getRequest().setClientId("ccd_data");

        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.CCD_BYPASS_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        buildExecuteKieSession();
        //assertion
        assignmentRequest.getRequestedRoles().forEach(ra -> assertEquals(Status.APPROVED, ra.getStatus()));

        //verify retrieveDataService is not used as ClientId = ccd_data
        RetrieveDataService retrieveDataService = getRetrieveDataService();
        verifyNoInteractions(retrieveDataService);
    }

    @Test
    void shouldApproveDummyCaseRoleCreationWithDummyRoleName_CCD_1_0_enableByPassDroolRule() {
        RoleAssignment requestedRole1 = getRequestedCaseRole_ra(RoleCategory.PROFESSIONAL, "[DUMMYSOLICITOR]",
                                                             SPECIFIC, "caseId",
                                                             "1234567890123456", CREATE_REQUESTED);
        requestedRole1.setClassification(Classification.PUBLIC);
        requestedRole1.getAttributes().putAll(Map.of("jurisdiction", convertValueJsonNode("AUTOTEST1"),
                                                     "caseType", convertValueJsonNode("Asylum"),
                                                     "caseId", convertValueJsonNode("1234567890123456")));
        assignmentRequest.setRequestedRoles(List.of(requestedRole1));
        assignmentRequest.getRequest().setClientId("ccd_data");
        assignmentRequest.getRequest().setByPassOrgDroolRule(true);

        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.CCD_BYPASS_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        buildExecuteKieSession();
        //assertion
        assignmentRequest.getRequestedRoles().forEach(ra -> assertEquals(Status.APPROVED, ra.getStatus()));

        //verify retrieveDataService is not used as ClientId = ccd_data
        RetrieveDataService retrieveDataService = getRetrieveDataService();
        verifyNoInteractions(retrieveDataService);
    }

    @Test
    void shouldRejectDummyCaseRoleCreation_CCD_1_0_disableByPassDroolRule() {
        RoleAssignment requestedRole1 = getRequestedCaseRole_ra(RoleCategory.SYSTEM, "[RESPSOLICITOR]",
                                                             SPECIFIC, "caseId",
                                                             "1234567890123456", CREATE_REQUESTED);
        requestedRole1.setClassification(Classification.RESTRICTED);
        requestedRole1.getAttributes().putAll(Map.of("jurisdiction", convertValueJsonNode("IA"),
                                                     "caseType", convertValueJsonNode("Asylum"),
                                                     "caseId", convertValueJsonNode("1234567890123456")));
        assignmentRequest.setRequestedRoles(List.of(requestedRole1));
        assignmentRequest.getRequest().setClientId("ccd_data");
        assignmentRequest.getRequest().setByPassOrgDroolRule(false);

        FeatureFlag featureFlag  =  FeatureFlag.builder().build();
        featureFlags.add(featureFlag);

        buildExecuteKieSession();
        //assertion
        assignmentRequest.getRequestedRoles().forEach(ra -> assertEquals(Status.REJECTED, ra.getStatus()));
    }

    @Test
    void shouldApproveDeletePetsolicitorCaserole() {
        verifyDeleteCaseRequestRole_CCD_1_0("[PETSOLICITOR]", "ccd_data", RoleCategory.PROFESSIONAL);
        verifyDeleteCaseRequestRole_CCD_1_0("[PETSOLICITOR]", "ccd_case_disposer", RoleCategory.PROFESSIONAL);
    }

    @Test
    void shouldApproveDeleteCreaterCaseRequestedRoles() {
        verifyDeleteCaseRequestRole_CCD_1_0("[CREATOR]", "ccd_data", RoleCategory.JUDICIAL);
        verifyDeleteCaseRequestRole_CCD_1_0("[CREATOR]", "aac_manage_case_assignment",
                                            RoleCategory.LEGAL_OPERATIONS);
        verifyDeleteCaseRequestRole_CCD_1_0("[CREATOR]", "aac_manage_case_assignment",
                                            RoleCategory.CITIZEN);
        verifyDeleteCaseRequestRole_CCD_1_0("[DUMMYCREATOR]", "ccd_data", RoleCategory.PROFESSIONAL);
    }

    private void verifyDeleteCaseRequestRole_CCD_1_0(String roleName, String clientId, RoleCategory category) {
        RoleAssignment requestedRole1 = getRequestedCaseRole_ra(category, roleName,
                                                             SPECIFIC, "caseId",
                                                             "1234567890123456", DELETE_REQUESTED);
        requestedRole1.setClassification(Classification.RESTRICTED);
        requestedRole1.getAttributes().putAll(Map.of("jurisdiction", convertValueJsonNode("IA"),
                                                     "caseType", convertValueJsonNode("Asylum"),
                                                     "caseId", convertValueJsonNode("1234567890123456")));
        assignmentRequest.setRequestedRoles(List.of(requestedRole1));
        assignmentRequest.getRequest().setClientId(clientId);

        FeatureFlag featureFlag  =  FeatureFlag.builder().build();
        featureFlags.add(featureFlag);

        buildExecuteKieSession();
        //assertion
        assignmentRequest.getRequestedRoles().forEach(ra -> assertEquals(Status.DELETE_APPROVED, ra.getStatus()));
    }

    @Test
    void shouldApproveDeleteDummyCaseRoles_enableCcdBypassFlag() {
        RoleAssignment requestedRole1 = getRequestedCaseRole_ra(RoleCategory.SYSTEM, "[RESPSOLICITOR]",
                                                             SPECIFIC, "caseId",
                                                             "1234567890123456", DELETE_REQUESTED);
        requestedRole1.setClassification(Classification.RESTRICTED);
        requestedRole1.getAttributes().putAll(Map.of("jurisdiction", convertValueJsonNode("AUTOTEST1"),
                                                     "caseType", convertValueJsonNode("Asylum"),
                                                     "caseId", convertValueJsonNode("1234567890123456")));
        assignmentRequest.setRequestedRoles(List.of(requestedRole1));
        assignmentRequest.getRequest().setClientId("ccd_data");

        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.CCD_BYPASS_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        buildExecuteKieSession();
        //assertion
        assignmentRequest.getRequestedRoles().forEach(ra -> assertEquals(Status.DELETE_APPROVED, ra.getStatus()));

        //verify retrieveDataService is not used as ClientId = ccd_data
        RetrieveDataService retrieveDataService = getRetrieveDataService();
        verifyNoInteractions(retrieveDataService);
    }

    @Test
    void shouldApproveDeleteDummyCaseRolesWithDummyRoleName_enableCcdBypassFlag() {
        RoleAssignment requestedRole1 = getRequestedCaseRole_ra(RoleCategory.PROFESSIONAL, "[DUMMYSOLICITOR]",
                                                             SPECIFIC, "caseId",
                                                             "1234567890123456", DELETE_REQUESTED);
        requestedRole1.setClassification(Classification.RESTRICTED);
        requestedRole1.getAttributes().putAll(Map.of("jurisdiction", convertValueJsonNode("AUTOTEST1"),
                                                     "caseType", convertValueJsonNode("Asylum"),
                                                     "caseId", convertValueJsonNode("1234567890123456")));
        assignmentRequest.setRequestedRoles(List.of(requestedRole1));
        assignmentRequest.getRequest().setClientId("ccd_data");

        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.CCD_BYPASS_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        buildExecuteKieSession();
        //assertion
        assignmentRequest.getRequestedRoles().forEach(ra -> assertEquals(Status.DELETE_APPROVED, ra.getStatus()));
    }

    @Test
    void shouldRejectDeleteDummyCaseRoles_invalidJurisdiction() {
        RoleAssignment requestedRole1 = getRequestedCaseRole_ra(RoleCategory.SYSTEM, "[RESPSOLICITOR]",
                                                             SPECIFIC, "caseId",
                                                             "1234567890123456", DELETE_REQUESTED);
        requestedRole1.setClassification(Classification.RESTRICTED);
        requestedRole1.getAttributes().putAll(Map.of("jurisdiction", convertValueJsonNode("IA"),
                                                     "caseType", convertValueJsonNode("Asylum"),
                                                     "caseId", convertValueJsonNode("1234567890123456")));
        assignmentRequest.setRequestedRoles(List.of(requestedRole1));
        assignmentRequest.getRequest().setClientId("ccd_data");

        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.CCD_BYPASS_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        buildExecuteKieSession();
        //assertion
        assignmentRequest.getRequestedRoles().forEach(ra -> assertEquals(Status.DELETE_REJECTED, ra.getStatus()));
    }

    @Test
    void shouldRejectCaseRoleCreation_disableCcdBypassFlag() {
        RoleAssignment requestedRole1 = getRequestedCaseRole_ra(RoleCategory.PROFESSIONAL,
                                                               "[PETSOLICITOR]", SPECIFIC, "caseId",
                                                             "1234567890123456", CREATE_REQUESTED);
        requestedRole1.setClassification(Classification.RESTRICTED);
        requestedRole1.getAttributes().putAll(Map.of("jurisdiction", convertValueJsonNode("AUTOTEST1"),
                                                     "caseType", convertValueJsonNode("Asylum"),
                                                     "caseId", convertValueJsonNode("1234567890123456")));
        assignmentRequest.setRequestedRoles(List.of(requestedRole1));
        assignmentRequest.getRequest().setClientId("ccd_data");

        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.CCD_BYPASS_1_0.getValue())
            .status(false).build();
        featureFlags.add(featureFlag);

        buildExecuteKieSession();
        //assertion
        assignmentRequest.getRequestedRoles().forEach(ra -> assertEquals(Status.APPROVED, ra.getStatus()));
    }

    @Test
    void shouldApproveCaseRoleCreation_enableByPassDroolRule_enableFlag() {
        RoleAssignment requestedRole1 = getRequestedCaseRole_ra(RoleCategory.PROFESSIONAL,
                                                               "[PETSOLICITOR]", SPECIFIC, "caseId",
                                                             "1234567890123456", CREATE_REQUESTED);
        requestedRole1.setClassification(Classification.RESTRICTED);
        requestedRole1.getAttributes().putAll(Map.of("jurisdiction", convertValueJsonNode("IA"),
                                                     "caseType", convertValueJsonNode("Asylum"),
                                                     "caseId", convertValueJsonNode("1234567890123456")));
        assignmentRequest.setRequestedRoles(List.of(requestedRole1));
        assignmentRequest.getRequest().setClientId("ccd_data");

        FeatureFlag featureFlag  =  FeatureFlag.builder().build();
        featureFlags.add(featureFlag);

        buildExecuteKieSession();
        //assertion
        assignmentRequest.getRequestedRoles().forEach(ra -> assertEquals(Status.APPROVED, ra.getStatus()));

        //verify retrieveDataService is not used as ClientId = ccd_data
        RetrieveDataService retrieveDataService = getRetrieveDataService();
        verifyNoInteractions(retrieveDataService);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "[C100APPLICANTSOLICITOR1]",
        "[C100APPLICANTSOLICITOR2]",
        "[C100APPLICANTSOLICITOR3]",
        "[C100APPLICANTSOLICITOR4]",
        "[C100APPLICANTSOLICITOR5]",
        "[FL401APPLICANTSOLICITOR]",
        "[C100CHILDSOLICITOR1]",
        "[C100CHILDSOLICITOR2]",
        "[C100CHILDSOLICITOR3]",
        "[C100CHILDSOLICITOR4]",
        "[C100CHILDSOLICITOR5]",
        "[C100RESPONDENTSOLICITOR1]",
        "[C100RESPONDENTSOLICITOR2]",
        "[C100RESPONDENTSOLICITOR3]",
        "[C100RESPONDENTSOLICITOR4]",
        "[C100RESPONDENTSOLICITOR5]",
        "[FL401RESPONDENTSOLICITOR]",
        "[C100APPLICANTBARRISTER1]",
        "[C100APPLICANTBARRISTER2]",
        "[C100APPLICANTBARRISTER3]",
        "[C100APPLICANTBARRISTER4]",
        "[C100APPLICANTBARRISTER5]",
        "[FL401APPLICANTBARRISTER]",
        "[C100RESPONDENTBARRISTER1]",
        "[C100RESPONDENTBARRISTER2]",
        "[C100RESPONDENTBARRISTER3]",
        "[C100RESPONDENTBARRISTER4]",
        "[C100RESPONDENTBARRISTER5]",
        "[FL401RESPONDENTBARRISTER]"
    })
    void shouldApproveOrRejectPrivateLawProfessionalCaseRoles(String roleName) {
        RoleCategory roleCategory = RoleCategory.PROFESSIONAL;
        String jurisdiction = "PRIVATELAW";
        String caseType = "PRLAPPS";

        // wrong category
        verifyCcdCaseRequestedRole(RoleCategory.CITIZEN, // WRONG (NB: this is another valid CCD Case Role Category)
                                   roleName,
                                   jurisdiction,
                                   caseType,
                                   true,
                                   Status.REJECTED);
        // wrong jurisdiction
        verifyCcdCaseRequestedRole(roleCategory,
                                   roleName,
                                   "wrong-jurisdiction", // WRONG
                                   caseType,
                                   true,
                                   Status.REJECTED);
        // wrong case-type
        verifyCcdCaseRequestedRole(roleCategory,
                                   roleName,
                                   jurisdiction,
                                   "wrong-caseType", // WRONG
                                   true,
                                   Status.REJECTED);
        // without caseId
        verifyCcdCaseRequestedRole(roleCategory,
                                   roleName,
                                   jurisdiction,
                                   caseType,
                                   false, // WRONG
                                   Status.REJECTED);

        // correct values should be approved
        verifyCcdCaseRequestedRole(roleCategory,
                                   roleName,
                                   jurisdiction,
                                   caseType,
                                   true,
                                   Status.APPROVED);
    }

    void verifyCcdCaseRequestedRole(RoleCategory roleCategory,
                                    String roleName,
                                    String jurisdiction,
                                    String caseType,
                                    boolean withCaseId,
                                    Status expectedStatus) {

        // GIVEN
        RoleAssignment requestedRole = getRequestedCaseRole_ra(
            roleCategory,
            roleName,
            SPECIFIC,
            "caseId",
            "1234567890123456",
            CREATE_REQUESTED
        );
        requestedRole.setClassification(Classification.RESTRICTED);
        requestedRole.getAttributes().putAll(Map.of("jurisdiction", convertValueJsonNode(jurisdiction),
                                                     "caseType", convertValueJsonNode(caseType)));
        if (!withCaseId) {
            requestedRole.getAttributes().remove("caseId");
        }
        assignmentRequest.setRequestedRoles(List.of(requestedRole));
        assignmentRequest.getRequest().setClientId("ccd_data"); // NB: these are CCD Case Role tests

        FeatureFlag featureFlag  =  FeatureFlag.builder().build();
        featureFlags.add(featureFlag);

        // WHEN
        buildExecuteKieSession();

        // THEN
        assertTrue(assignmentRequest.getRequestedRoles().size() > 0, "No requested roles found");
        assignmentRequest.getRequestedRoles().forEach(ra -> {
            assertEquals(expectedStatus, ra.getStatus());

            // If has Case-ID then these tests should always pass stage 1 processing
            assertEquals(
                withCaseId,
                ra.getLog().contains("Stage 1 approved : ccd_create_case_roles"),
                "Role has not passed stage 1 of CCD case role validation"
            );

            // however they should only pass validation of role_config pattern if expected status is APPROVED
            assertEquals(
                expectedStatus == Status.APPROVED,
                ra.getLog().contains("Approved : validate_role_assignment_against_patterns"),
                "Wrong outcome for role validation against role_config patterns"
            );
        });
    }

    @Nested
    @DisplayName("IDAM Disposer CCD Case Roles Tests")
    class IdamDisposerCaseRolesTest {

        static final String IDAM_DISPOSER_CLIENT_ID = "disposer-idam-user";

        @Test
        void shouldApproveCreateCaseRoleForCitizenFromIdamDisposer() {

            // GIVEN
            assignmentRequest.setRequestedRoles(List.of(createRequestedRole(RoleCategory.CITIZEN)));
            assignmentRequest.getRequest().setClientId(IDAM_DISPOSER_CLIENT_ID);
            setDisposerFeatureFlag(true);

            // WHEN
            buildExecuteKieSession();

            // THEN
            assignmentRequest.getRequestedRoles().forEach(ra -> assertEquals(Status.APPROVED, ra.getStatus()));

            //verify retrieveDataService is not used as ClientId = disposer-idam-user
            RetrieveDataService retrieveDataService = getRetrieveDataService();
            verifyNoInteractions(retrieveDataService);
        }

        @ParameterizedTest
        @EnumSource(
            value = RoleCategory.class,
            names = { "CITIZEN" },
            mode = EnumSource.Mode.EXCLUDE
        )
        void shouldRejectCreateCaseRoleForNonCitizenFromIdamDisposer(RoleCategory roleCategory) {

            // GIVEN
            assignmentRequest.setRequestedRoles(List.of(createRequestedRole(roleCategory)));
            assignmentRequest.getRequest().setClientId(IDAM_DISPOSER_CLIENT_ID);
            setDisposerFeatureFlag(true);

            // WHEN
            buildExecuteKieSession();

            // THEN
            assignmentRequest.getRequestedRoles().forEach(ra -> assertEquals(Status.REJECTED, ra.getStatus()));

            //verify retrieveDataService is not used as ClientId = disposer-idam-user
            RetrieveDataService retrieveDataService = getRetrieveDataService();
            verifyNoInteractions(retrieveDataService);
        }

        @Test
        void shouldRejectCreateCaseRoleForCitizenFromIdamDisposer_flagDisabled() {

            // GIVEN
            assignmentRequest.setRequestedRoles(List.of(createRequestedRole(RoleCategory.CITIZEN)));
            assignmentRequest.getRequest().setClientId(IDAM_DISPOSER_CLIENT_ID);
            setDisposerFeatureFlag(false); // i.e. disable flag

            // WHEN
            buildExecuteKieSession();

            // THEN
            assignmentRequest.getRequestedRoles().forEach(ra -> assertEquals(Status.REJECTED, ra.getStatus()));

            //verify retrieveDataService is not used as ClientId = disposer-idam-user
            RetrieveDataService retrieveDataService = getRetrieveDataService();
            verifyNoInteractions(retrieveDataService);
        }

        private RoleAssignment createRequestedRole(RoleCategory roleCategory) {
            RoleAssignment requestedRole1 = getRequestedCaseRole_ra(roleCategory,
                                                                    "[CREATOR]", SPECIFIC, "caseId",
                                                                    "1234567890123456", CREATE_REQUESTED);
            requestedRole1.setClassification(Classification.RESTRICTED);
            requestedRole1.getAttributes().putAll(Map.of("jurisdiction", convertValueJsonNode("IA"),
                                                         "caseType", convertValueJsonNode("Asylum"),
                                                         "caseId", convertValueJsonNode("1234567890123456")));

            return requestedRole1;
        }

        private void setDisposerFeatureFlag(boolean status) {
            FeatureFlag featureFlag  =  FeatureFlag.builder()
                .flagName(FeatureFlagEnum.DISPOSER_1_0.getValue())
                .status(status)
                .build();

            featureFlags.add(featureFlag);
        }
    }

}
