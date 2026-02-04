package uk.gov.hmcts.reform.roleassignment.domain.service.drools;

import org.junit.jupiter.api.extension.ExtendWith;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.roleassignment.domain.model.FeatureFlag;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.FeatureFlagEnum;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Classification.RESTRICTED;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType.STANDARD;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.CREATE_REQUESTED;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.DELETE_APPROVED;
import static uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder.getRequestedOrgRole;
import static uk.gov.hmcts.reform.roleassignment.util.JacksonUtils.convertValueJsonNode;

@ExtendWith(MockitoExtension.class)
public class PrmOrgRoleTest extends DroolBase {

    @ParameterizedTest
    @CsvSource({
        // NB: GA only role combinations from: `./src/main/resources/roleconfig/role_prm.json`
        //     i.e. those with mandatory caseAccessGroupId
        "PRM_Test_GA_Role,TEST_JURISDICTION,TestCaseType,TEST_JURISDICTION:all-cases:123:12345",
        "CaseProfessionalGroupAccess_GA_Role,BEFTA_MASTER,TestCaseType,BEFTA_MASTER:all-cases:999:99999"
    })
    void shouldApproveOrRejectProfessionalOrgGroupAccessRoleRequest(String roleName,
                                                                    String jurisdiction,
                                                                    String caseType,
                                                                    String caseAccessGroupId) {

        RoleCategory roleCategory = RoleCategory.PROFESSIONAL;

        // wrong category
        verifyProfessionalOrgGroupAccessRequestedRole(RoleCategory.CITIZEN, // WRONG
                                                      roleName,
                                                      jurisdiction,
                                                      caseType,
                                                      caseAccessGroupId,
                                                      Status.REJECTED);
        // wrong jurisdiction
        verifyProfessionalOrgGroupAccessRequestedRole(roleCategory,
                                                      roleName,
                                                      "wrong-jurisdiction", // WRONG
                                                      caseType,
                                                      caseAccessGroupId,
                                                      Status.REJECTED);
        // missing case-type
        verifyProfessionalOrgGroupAccessRequestedRole(roleCategory,
                                                      roleName,
                                                      jurisdiction,
                                                      null, // MISSING
                                                      caseAccessGroupId,
                                                      Status.REJECTED);
        // missing caseAccessGroupId
        verifyProfessionalOrgGroupAccessRequestedRole(roleCategory,
                                                      roleName,
                                                      jurisdiction,
                                                      caseType,
                                                      null, // MISSING (NB: mandatory for GA role)
                                                      Status.REJECTED);

        // correct values should be approved
        verifyProfessionalOrgGroupAccessRequestedRole(roleCategory,
                                                      roleName,
                                                      jurisdiction,
                                                      caseType,
                                                      caseAccessGroupId,
                                                      Status.APPROVED);
    }

    @ParameterizedTest
    @CsvSource({
        // NB: ORG role combinations from: `./src/main/resources/roleconfig/role_prm.json`
        //     i.e. those without mandatory caseAccessGroupId
        "PRM_Test_Org_Role,TEST_JURISDICTION,TestCaseType",
        "CaseProfessionalGroupAccess_Org_Role,BEFTA_MASTER,TestCaseType",
        "Role1,BEFTA_JURISDICTION_1,TestCaseType",
        "Role1,BEFTA_MASTER,TestCaseType"
    })
    void shouldApproveOrRejectProfessionalOrgRoleRequest(String roleName,
                                                         String jurisdiction,
                                                         String caseType) {
        RoleCategory roleCategory = RoleCategory.PROFESSIONAL;

        // NB: ORG roles do not use caseAccessGroupId, so it should be null in these tests

        // wrong category
        verifyProfessionalOrgGroupAccessRequestedRole(RoleCategory.CITIZEN, // WRONG
                                                      roleName,
                                                      jurisdiction,
                                                      caseType,
                                                      null,
                                                      Status.REJECTED);
        // wrong jurisdiction
        verifyProfessionalOrgGroupAccessRequestedRole(roleCategory,
                                                      roleName,
                                                      "wrong-jurisdiction", // WRONG
                                                      caseType,
                                                      null,
                                                      Status.REJECTED);
        // missing case-type
        verifyProfessionalOrgGroupAccessRequestedRole(roleCategory,
                                                      roleName,
                                                      jurisdiction,
                                                      null, // MISSING
                                                      null,
                                                      Status.REJECTED);

        // correct values should be approved
        verifyProfessionalOrgGroupAccessRequestedRole(roleCategory,
                                                      roleName,
                                                      jurisdiction,
                                                      caseType,
                                                      null,
                                                      Status.APPROVED);
    }

    private void verifyProfessionalOrgGroupAccessRequestedRole(RoleCategory roleCategory,
                                                               String roleName,
                                                               String jurisdiction,
                                                               String caseType,
                                                               String caseAccessGroupId,
                                                               Status expectedStatus) {
        // GIVEN
        assignmentRequest.getRequest().setClientId("am_org_role_mapping_service");
        assignmentRequest.getRequest().setProcess("professional-organisational-role-mapping");
        assignmentRequest.getRequest().setReplaceExisting(true);
        assignmentRequest.setRequestedRoles(getRequestedOrgRole());
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(roleCategory);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName(roleName);
            roleAssignment.setGrantType(STANDARD);
            roleAssignment.setClassification(RESTRICTED);
            roleAssignment.setStatus(CREATE_REQUESTED);
            roleAssignment.getAttributes().put("jurisdiction", convertValueJsonNode(jurisdiction));
            if (StringUtils.isNotEmpty(caseType)) {
                roleAssignment.getAttributes().put("caseType", convertValueJsonNode(caseType));
            }
            if (StringUtils.isNotEmpty(caseAccessGroupId)) {
                roleAssignment.getAttributes().put("caseAccessGroupId", convertValueJsonNode(caseAccessGroupId));
            }
        });

        FeatureFlag featureFlag = FeatureFlag.builder()
            .flagName(FeatureFlagEnum.GA_PRM_1_0.getValue())
            .status(true)
            .build();
        featureFlags.add(featureFlag);

        // WHEN
        buildExecuteKieSession();

        // THEN
        assertFalse(assignmentRequest.getRequest().isByPassOrgDroolRule());
        assertTrue(assignmentRequest.getRequestedRoles().size() > 0, "No requested roles found");
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(expectedStatus, roleAssignment.getStatus());
            assertEquals(roleCategory, roleAssignment.getRoleCategory());
            assertEquals(RoleType.ORGANISATION, roleAssignment.getRoleType());
            assertEquals(roleName, roleAssignment.getRoleName());
            assertEquals(jurisdiction, roleAssignment.getAttributes().get("jurisdiction").asText());
            if (StringUtils.isNotEmpty(caseType)) {
                assertEquals(caseType, roleAssignment.getAttributes().get("caseType").asText());
            } else {
                assertFalse(roleAssignment.getAttributes().containsKey("caseType"));
            }
            if (StringUtils.isNotEmpty(caseAccessGroupId)) {
                assertEquals(caseAccessGroupId, roleAssignment.getAttributes().get("caseAccessGroupId").asText());
            } else {
                assertFalse(roleAssignment.getAttributes().containsKey("caseAccessGroupId"));
            }

            // If PROFESSIONAL role then these tests should always pass stage 1 processing
            assertEquals(
                roleCategory == RoleCategory.PROFESSIONAL,
                roleAssignment.getLog().contains("Create approved : prm_create_org_role"),
                "Role has not passed stage 1 of PRM role validation"
            );

            // however they should only pass validation of role_config pattern if expected status is APPROVED
            assertEquals(
                expectedStatus == Status.APPROVED,
                roleAssignment.getLog().contains("Approved : validate_role_assignment_against_patterns"),
                "Wrong outcome for role validation against role_config patterns"
            );
        });
    }

    @ParameterizedTest
    @CsvSource({
        // NB: All role combinations from: `./src/main/resources/roleconfig/role_prm.json`
        "PRM_Test_Org_Role,TEST_JURISDICTION",
        "PRM_Test_GA_Role,TEST_JURISDICTION",
        "CaseProfessionalGroupAccess_GA_Role,BEFTA_MASTER",
        "CaseProfessionalGroupAccess_Org_Role,BEFTA_MASTER",
        "Role1,BEFTA_JURISDICTION_1,TestCaseType",
        "Role1,BEFTA_MASTER,TestCaseType"
    })
    void shouldDeleteProfessionalOrgRole(String roleName, String jurisdiction) {

        // GIVEN
        assignmentRequest.getRequest().setClientId("am_org_role_mapping_service");
        assignmentRequest.setRequestedRoles(getRequestedOrgRole());
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.PROFESSIONAL);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName(roleName);
            roleAssignment.setGrantType(STANDARD);
            roleAssignment.setClassification(RESTRICTED);
            roleAssignment.setStatus(Status.DELETE_REQUESTED);
            roleAssignment.getAttributes().put("jurisdiction", convertValueJsonNode(jurisdiction));
        });

        // WHEN
        buildExecuteKieSession();

        // THEN
        assertFalse(assignmentRequest.getRequest().isByPassOrgDroolRule());
        assertTrue(assignmentRequest.getRequestedRoles().size() > 0, "No requested roles found");
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(DELETE_APPROVED, roleAssignment.getStatus());
            assertEquals(RoleCategory.PROFESSIONAL, roleAssignment.getRoleCategory());
            assertEquals(RoleType.ORGANISATION, roleAssignment.getRoleType());
            assertEquals(roleName, roleAssignment.getRoleName());
            assertEquals(jurisdiction, roleAssignment.getAttributes().get("jurisdiction").asText());

            assertTrue(
                roleAssignment.getLog().contains("Delete approved : prm_delete_org_role"),
                "Role has not passed stage 1 of PRM role deletion validation"
            );
        });
    }

}
