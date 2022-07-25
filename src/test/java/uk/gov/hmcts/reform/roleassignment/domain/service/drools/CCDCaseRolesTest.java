package uk.gov.hmcts.reform.roleassignment.domain.service.drools;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.roleassignment.domain.model.FeatureFlag;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Classification;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.FeatureFlagEnum;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType.SPECIFIC;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.CREATE_REQUESTED;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.DELETE_REQUESTED;
import static uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder.getRequestedCaseRole_ra;
import static uk.gov.hmcts.reform.roleassignment.util.JacksonUtils.convertValueJsonNode;

@RunWith(MockitoJUnitRunner.class)
class CCDCaseRolesTest extends DroolBase {
    @Test
    void shouldRejectCaseRequestedRolesForUnauthoriseRequest() {
        RoleAssignment requestedRole1 = getRequestedCaseRole_ra(RoleCategory.PROFESSIONAL,
                                   "[PETSOLICITOR]", SPECIFIC, "caseId",
                                 "1234567890123456", CREATE_REQUESTED);
        assignmentRequest.setRequestedRoles(List.of(requestedRole1));
        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.CCD_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        buildExecuteKieSession();
        //assertion
        assignmentRequest.getRequestedRoles().forEach(ra -> assertEquals(Status.REJECTED, ra.getStatus()));
    }

    @Test
    void shouldApproveCreaterCaseRole() {
        verifyCreateCaseRequestedRole_CCD_1_0("[CREATOR]", "ccd_data", RoleCategory.JUDICIAL);
        verifyCreateCaseRequestedRole_CCD_1_0("[CREATOR]", "aac_manage_case_assignment",
                                              RoleCategory.LEGAL_OPERATIONS);
        verifyCreateCaseRequestedRole_CCD_1_0("[CREATOR]", "aac_manage_case_assignment",
                                              RoleCategory.CITIZEN);
    }

    @Test
    void shouldApprovePetSolicitiorCaseRole() {
        verifyCreateCaseRequestedRole_CCD_1_0("[PETSOLICITOR]", "ccd_data", RoleCategory.PROFESSIONAL);
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

        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.CCD_1_0.getValue())
            .status(true).build();
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

        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.CCD_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        buildExecuteKieSession();
        //assertion
        assignmentRequest.getRequestedRoles().forEach(ra -> assertEquals(Status.REJECTED, ra.getStatus()));
    }

    @Test
    void shouldApproveDeletePetsolicitorCaserole() {
        verifyDeleteCaseRequestRole_CCD_1_0("[PETSOLICITOR]", "ccd_data", RoleCategory.PROFESSIONAL);
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

        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.CCD_1_0.getValue())
            .status(true).build();
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
        assignmentRequest.getRequestedRoles().forEach(ra -> assertEquals(Status.REJECTED, ra.getStatus()));
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

        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.CCD_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        buildExecuteKieSession();
        //assertion
        assignmentRequest.getRequestedRoles().forEach(ra -> assertEquals(Status.APPROVED, ra.getStatus()));
    }
}
