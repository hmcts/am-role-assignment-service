package uk.gov.hmcts.reform.roleassignment.domain.service.drools;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.roleassignment.domain.model.FeatureFlag;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.FeatureFlagEnum;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Classification.RESTRICTED;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType.STANDARD;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.APPROVED;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.DELETE_APPROVED;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.CREATE_REQUESTED;
import static uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder.getRequestedOrgRole;
import static uk.gov.hmcts.reform.roleassignment.util.JacksonUtils.convertValueJsonNode;

@RunWith(MockitoJUnitRunner.class)
class PrmOrgRoleTest extends DroolBase {

    @ParameterizedTest
    @CsvSource({
        "PRM_Test_Org_Role,TEST_JURISDICTION,TestCaseType"
    })
    void shouldApproveProfessionalOrgRoleRequest(String roleName,
                                                 String jurisdiction,
                                                 String caseType) {

        // arrange
        assignmentRequest.getRequest().setClientId("am_org_role_mapping_service");
        assignmentRequest.getRequest().setProcess("professional-organisational-role-mapping");
        assignmentRequest.getRequest().setReplaceExisting(true);
        assignmentRequest.setRequestedRoles(getRequestedOrgRole());
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.PROFESSIONAL);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName(roleName);
            roleAssignment.setGrantType(STANDARD);
            roleAssignment.setClassification(RESTRICTED);
            roleAssignment.setStatus(CREATE_REQUESTED);
            roleAssignment.getAttributes().put("jurisdiction", convertValueJsonNode(jurisdiction));
            roleAssignment.getAttributes().put("caseType", convertValueJsonNode(caseType));
        });

        FeatureFlag featureFlag = FeatureFlag.builder()
            .flagName(FeatureFlagEnum.GA_PRM_1_0.getValue())
            .status(true)
            .build();
        featureFlags.add(featureFlag);

        // act
        buildExecuteKieSession();

        // assertion
        assertFalse(assignmentRequest.getRequest().isByPassOrgDroolRule());
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(APPROVED, roleAssignment.getStatus());
            assertEquals(RoleCategory.PROFESSIONAL, roleAssignment.getRoleCategory());
            assertEquals(RoleType.ORGANISATION, roleAssignment.getRoleType());
            assertEquals(roleName, roleAssignment.getRoleName());
            assertEquals(jurisdiction, roleAssignment.getAttributes().get("jurisdiction").asText());
            assertEquals(caseType, roleAssignment.getAttributes().get("caseType").asText());
        });
    }

    @ParameterizedTest
    @CsvSource({
        "PRM_Test_GA_Role,TEST_JURISDICTION,TestCaseType,TEST_JURISDICTION:all-cases:123:12345"
    })
    void shouldApproveProfessionalOrgGroupAccessRoleRequest(String roleName,
                                                            String jurisdiction,
                                                            String caseType,
                                                            String caseAccessGroupId) {

        // arrange
        assignmentRequest.getRequest().setClientId("am_org_role_mapping_service");
        assignmentRequest.getRequest().setProcess("professional-organisational-role-mapping");
        assignmentRequest.getRequest().setReplaceExisting(true);
        assignmentRequest.setRequestedRoles(getRequestedOrgRole());
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.PROFESSIONAL);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName(roleName);
            roleAssignment.setGrantType(STANDARD);
            roleAssignment.setClassification(RESTRICTED);
            roleAssignment.setStatus(CREATE_REQUESTED);
            roleAssignment.getAttributes().put("jurisdiction", convertValueJsonNode(jurisdiction));
            roleAssignment.getAttributes().put("caseType", convertValueJsonNode(caseType));
            roleAssignment.getAttributes().put("caseAccessGroupId", convertValueJsonNode(caseAccessGroupId));
        });

        FeatureFlag featureFlag = FeatureFlag.builder()
            .flagName(FeatureFlagEnum.GA_PRM_1_0.getValue())
            .status(true)
            .build();
        featureFlags.add(featureFlag);

        // act
        buildExecuteKieSession();

        // assertion
        assertFalse(assignmentRequest.getRequest().isByPassOrgDroolRule());
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(APPROVED, roleAssignment.getStatus());
            assertEquals(RoleCategory.PROFESSIONAL, roleAssignment.getRoleCategory());
            assertEquals(RoleType.ORGANISATION, roleAssignment.getRoleType());
            assertEquals(roleName, roleAssignment.getRoleName());
            assertEquals(jurisdiction, roleAssignment.getAttributes().get("jurisdiction").asText());
            assertEquals(caseType, roleAssignment.getAttributes().get("caseType").asText());
            assertEquals(caseAccessGroupId, roleAssignment.getAttributes().get("caseAccessGroupId").asText());
        });
    }

    @ParameterizedTest
    @CsvSource({
        "PRM_Test_Org_Role,TEST_JURISDICTION",
        "PRM_Test_GA_Role,TEST_JURISDICTION"
    })
    void shouldDeleteProfessionalOrgRole(String roleName, String jurisdiction) {

        // arrange
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

        // act
        buildExecuteKieSession();

        // assertion
        assertFalse(assignmentRequest.getRequest().isByPassOrgDroolRule());
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(DELETE_APPROVED, roleAssignment.getStatus());
            assertEquals(RoleCategory.PROFESSIONAL, roleAssignment.getRoleCategory());
            assertEquals(RoleType.ORGANISATION, roleAssignment.getRoleType());
            assertEquals(roleName, roleAssignment.getRoleName());
            assertEquals(jurisdiction, roleAssignment.getAttributes().get("jurisdiction").asText());
        });
    }

    @ParameterizedTest
    @CsvSource({
        "PRM_Test_GA_Role,TEST_JURISDICTION,TestCaseType"
    })
    void shouldRejectProfessionalOrgGroupAccessRoleRequestWithMissingCaseAccessGroupId(String roleName,
                                                                                       String jurisdiction,
                                                                                       String caseType) {

        // arrange
        assignmentRequest.getRequest().setClientId("am_org_role_mapping_service");
        assignmentRequest.getRequest().setProcess("professional-organisational-role-mapping");
        assignmentRequest.getRequest().setReplaceExisting(true);
        assignmentRequest.setRequestedRoles(getRequestedOrgRole());
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.PROFESSIONAL);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName(roleName);
            roleAssignment.setGrantType(STANDARD);
            roleAssignment.setClassification(RESTRICTED);
            roleAssignment.setStatus(CREATE_REQUESTED);
            roleAssignment.getAttributes().put("jurisdiction", convertValueJsonNode(jurisdiction));
            roleAssignment.getAttributes().put("caseType", convertValueJsonNode(caseType));
        });

        FeatureFlag featureFlag = FeatureFlag.builder()
            .flagName(FeatureFlagEnum.GA_PRM_1_0.getValue())
            .status(true)
            .build();
        featureFlags.add(featureFlag);

        // act
        buildExecuteKieSession();

        // assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment ->
                                                          assertEquals(Status.REJECTED, roleAssignment.getStatus()));
    }

}
