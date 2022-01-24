package uk.gov.hmcts.reform.roleassignment.domain.service.drools;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.roleassignment.domain.model.FeatureFlag;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Classification;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.FeatureFlagEnum;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;

import static uk.gov.hmcts.reform.roleassignment.util.JacksonUtils.convertValueJsonNode;

import java.util.HashMap;
import java.util.List;

class ChallengedAccessDroolsTest extends DroolBase {

    /* -----------------------------------------CHALLENGED ACCESS JUDICIAL----------------------------------------- */

    @Test //rule challenged_access_create_case_role_judiciary
    void shouldGrantAccessFor_ChallengedAccessJudiciary() {

        HashMap<String, JsonNode> roleAssignmentAttributes = new HashMap<>();
        roleAssignmentAttributes.put("caseId", convertValueJsonNode("1234567890123456"));
        roleAssignmentAttributes.put("requestedRole", convertValueJsonNode("challenged-access-judiciary"));
        roleAssignmentAttributes.put("caseType", convertValueJsonNode("notAsylum"));
        roleAssignmentAttributes.put("jurisdiction", convertValueJsonNode("IA"));

        assignmentRequest = TestDataBuilder.buildAssignmentRequestSpecialAccess("challenged-access",
                                                                                "challenged-access-judiciary",
                                                                                RoleCategory.JUDICIAL,
                                                                                roleAssignmentAttributes,
                                                                                Classification.PUBLIC,
                                                                                GrantType.CHALLENGED,
                                                                                Status.CREATE_REQUESTED,
                                                                                "anyClient",
                                                                                false).build();

        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_CHALLENGED_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        HashMap<String,JsonNode> existingAttributes = new HashMap<>();
        existingAttributes.put("jurisdiction", convertValueJsonNode("IA"));
        existingAttributes.put("caseType", convertValueJsonNode("Asylum"));
        existingAttributes.put("substantive", convertValueJsonNode("Y"));
        executeDroolRules(List.of(TestDataBuilder
                                      .buildExistingRoleForDrools("4772dc44-268f-4d0c-8f83-f0fb662aac84",
                                                                  "anyRoleName",
                                                                  RoleCategory.JUDICIAL,
                                                                  existingAttributes,
                                                                  Classification.PRIVATE,
                                                                  GrantType.STANDARD,
                                                                  RoleType.ORGANISATION)));

        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            Assertions.assertEquals(Status.APPROVED, roleAssignment.getStatus());
            Assertions.assertEquals("Asylum", roleAssignment.getAttributes().get("caseType").asText());
            Assertions.assertEquals(Classification.PUBLIC, roleAssignment.getClassification());
            Assertions.assertEquals(List.of("CCD","ExUI","SSIC", "RefData"),
                                    roleAssignment.getAuthorisations());
        });
    }

    @Test //rule challenged_access_create_case_role_judiciary
    void shouldRejectAccessFor_ChallengedAccessJudiciary_IncorrectRoleName() {

        HashMap<String, JsonNode> roleAssignmentAttributes = new HashMap<>();
        roleAssignmentAttributes.put("caseId", convertValueJsonNode("1234567890123456"));
        roleAssignmentAttributes.put("requestedRole", convertValueJsonNode("challenged-access-judiciary"));
        roleAssignmentAttributes.put("caseType", convertValueJsonNode("notAsylum"));
        roleAssignmentAttributes.put("jurisdiction", convertValueJsonNode("IA"));

        assignmentRequest = TestDataBuilder.buildAssignmentRequestSpecialAccess("challenged-access",
                                                                                "challenged-access-admin",
                                                                                RoleCategory.JUDICIAL,
                                                                                roleAssignmentAttributes,
                                                                                Classification.PUBLIC,
                                                                                GrantType.CHALLENGED,
                                                                                Status.CREATE_REQUESTED,
                                                                                "anyClient",
                                                                                false).build();

        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_CHALLENGED_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        HashMap<String,JsonNode> existingAttributes = new HashMap<>();
        existingAttributes.put("jurisdiction", convertValueJsonNode("IA"));
        existingAttributes.put("caseType", convertValueJsonNode("Asylum"));
        existingAttributes.put("substantive", convertValueJsonNode("Y"));
        executeDroolRules(List.of(TestDataBuilder
                                      .buildExistingRoleForDrools("4772dc44-268f-4d0c-8f83-f0fb662aac84",
                                                                  "anyRoleName",
                                                                  RoleCategory.JUDICIAL,
                                                                  existingAttributes,
                                                                  Classification.PRIVATE,
                                                                  GrantType.STANDARD,
                                                                  RoleType.ORGANISATION)));

        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            Assertions.assertEquals(Status.REJECTED, roleAssignment.getStatus());
        });
    }

    @Test //rule challenged_access_create_case_role_judiciary
    void shouldRejectAccessFor_ChallengedAccessJudiciary_IncorrectFlagEnabled() {

        HashMap<String, JsonNode> roleAssignmentAttributes = new HashMap<>();
        roleAssignmentAttributes.put("caseId", convertValueJsonNode("1234567890123456"));
        roleAssignmentAttributes.put("requestedRole", convertValueJsonNode("challenged-access-judiciary"));
        roleAssignmentAttributes.put("caseType", convertValueJsonNode("notAsylum"));
        roleAssignmentAttributes.put("jurisdiction", convertValueJsonNode("IA"));

        assignmentRequest = TestDataBuilder.buildAssignmentRequestSpecialAccess("challenged-access",
                                                                                "challenged-access-judiciary",
                                                                                RoleCategory.JUDICIAL,
                                                                                roleAssignmentAttributes,
                                                                                Classification.PUBLIC,
                                                                                GrantType.CHALLENGED,
                                                                                Status.CREATE_REQUESTED,
                                                                                "anyClient",
                                                                                false).build();

        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_SPECIFIC_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        HashMap<String,JsonNode> existingAttributes = new HashMap<>();
        existingAttributes.put("jurisdiction", convertValueJsonNode("IA"));
        existingAttributes.put("caseType", convertValueJsonNode("Asylum"));
        existingAttributes.put("substantive", convertValueJsonNode("Y"));
        executeDroolRules(List.of(TestDataBuilder
                                      .buildExistingRoleForDrools("4772dc44-268f-4d0c-8f83-f0fb662aac84",
                                                                  "anyRoleName",
                                                                  RoleCategory.JUDICIAL,
                                                                  existingAttributes,
                                                                  Classification.PRIVATE,
                                                                  GrantType.STANDARD,
                                                                  RoleType.ORGANISATION)));

        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            Assertions.assertEquals(Status.REJECTED, roleAssignment.getStatus());
        });
    }

    /* -----------------------------------------CHALLENGED ACCESS ADMIN----------------------------------------- */

    @Test //rule challenged_access_create_case_role_admin
    void shouldGrantAccessFor_ChallengedAccessAdmin() {

        HashMap<String, JsonNode> roleAssignmentAttributes = new HashMap<>();
        roleAssignmentAttributes.put("caseId", convertValueJsonNode("1234567890123456"));
        roleAssignmentAttributes.put("requestedRole", convertValueJsonNode("challenged-access-admin"));
        roleAssignmentAttributes.put("caseType", convertValueJsonNode("notAsylum"));
        roleAssignmentAttributes.put("jurisdiction", convertValueJsonNode("IA"));

        assignmentRequest = TestDataBuilder.buildAssignmentRequestSpecialAccess("challenged-access",
                                                                                "challenged-access-admin",
                                                                                RoleCategory.ADMIN,
                                                                                roleAssignmentAttributes,
                                                                                Classification.PUBLIC,
                                                                                GrantType.CHALLENGED,
                                                                                Status.CREATE_REQUESTED,
                                                                                "anyClient",
                                                                                false).build();

        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_CHALLENGED_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        HashMap<String,JsonNode> existingAttributes = new HashMap<>();
        existingAttributes.put("jurisdiction", convertValueJsonNode("IA"));
        existingAttributes.put("caseType", convertValueJsonNode("Asylum"));
        existingAttributes.put("substantive", convertValueJsonNode("Y"));
        executeDroolRules(List.of(TestDataBuilder
                                      .buildExistingRoleForDrools("4772dc44-268f-4d0c-8f83-f0fb662aac84",
                                                                  "anyRoleName",
                                                                  RoleCategory.ADMIN,
                                                                  existingAttributes,
                                                                  Classification.PRIVATE,
                                                                  GrantType.STANDARD,
                                                                  RoleType.ORGANISATION)));

        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            Assertions.assertEquals(Status.APPROVED, roleAssignment.getStatus());
            Assertions.assertEquals("Asylum", roleAssignment.getAttributes().get("caseType").asText());
            Assertions.assertEquals(Classification.PUBLIC, roleAssignment.getClassification());
            Assertions.assertEquals(List.of("CCD","ExUI","SSIC", "RefData"),
                                    roleAssignment.getAuthorisations());
        });
    }

    @Test //rule challenged_access_create_case_role_admin
    void shouldRejectAccessFor_ChallengedAccessAdmin_IncorrectRoleName() {

        HashMap<String, JsonNode> roleAssignmentAttributes = new HashMap<>();
        roleAssignmentAttributes.put("caseId", convertValueJsonNode("1234567890123456"));
        roleAssignmentAttributes.put("requestedRole", convertValueJsonNode("challenged-access-admin"));
        roleAssignmentAttributes.put("caseType", convertValueJsonNode("notAsylum"));
        roleAssignmentAttributes.put("jurisdiction", convertValueJsonNode("IA"));

        assignmentRequest = TestDataBuilder.buildAssignmentRequestSpecialAccess("challenged-access",
                                                                                "challenged-access-judiciary",
                                                                                RoleCategory.ADMIN,
                                                                                roleAssignmentAttributes,
                                                                                Classification.PUBLIC,
                                                                                GrantType.CHALLENGED,
                                                                                Status.CREATE_REQUESTED,
                                                                                "anyClient",
                                                                                false).build();

        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_CHALLENGED_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        HashMap<String,JsonNode> existingAttributes = new HashMap<>();
        existingAttributes.put("jurisdiction", convertValueJsonNode("IA"));
        existingAttributes.put("caseType", convertValueJsonNode("Asylum"));
        existingAttributes.put("substantive", convertValueJsonNode("Y"));
        executeDroolRules(List.of(TestDataBuilder
                                      .buildExistingRoleForDrools("4772dc44-268f-4d0c-8f83-f0fb662aac84",
                                                                  "anyRoleName",
                                                                  RoleCategory.ADMIN,
                                                                  existingAttributes,
                                                                  Classification.PRIVATE,
                                                                  GrantType.STANDARD,
                                                                  RoleType.ORGANISATION)));

        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            Assertions.assertEquals(Status.REJECTED, roleAssignment.getStatus());
        });
    }

    @Test //rule challenged_access_create_case_role_admin
    void shouldRejectAccessFor_ChallengedAccessAdmin_IncorrectFlagEnabled() {

        HashMap<String, JsonNode> roleAssignmentAttributes = new HashMap<>();
        roleAssignmentAttributes.put("caseId", convertValueJsonNode("1234567890123456"));
        roleAssignmentAttributes.put("requestedRole", convertValueJsonNode("challenged-access-judiciary"));
        roleAssignmentAttributes.put("caseType", convertValueJsonNode("notAsylum"));
        roleAssignmentAttributes.put("jurisdiction", convertValueJsonNode("IA"));

        assignmentRequest = TestDataBuilder.buildAssignmentRequestSpecialAccess("challenged-access",
                                                                                "challenged-access-admin",
                                                                                RoleCategory.ADMIN,
                                                                                roleAssignmentAttributes,
                                                                                Classification.PUBLIC,
                                                                                GrantType.CHALLENGED,
                                                                                Status.CREATE_REQUESTED,
                                                                                "anyClient",
                                                                                false).build();

        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_SPECIFIC_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        HashMap<String,JsonNode> existingAttributes = new HashMap<>();
        existingAttributes.put("jurisdiction", convertValueJsonNode("IA"));
        existingAttributes.put("caseType", convertValueJsonNode("Asylum"));
        existingAttributes.put("substantive", convertValueJsonNode("Y"));
        executeDroolRules(List.of(TestDataBuilder
                                      .buildExistingRoleForDrools("4772dc44-268f-4d0c-8f83-f0fb662aac84",
                                                                  "anyRoleName",
                                                                  RoleCategory.ADMIN,
                                                                  existingAttributes,
                                                                  Classification.PRIVATE,
                                                                  GrantType.STANDARD,
                                                                  RoleType.ORGANISATION)));

        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            Assertions.assertEquals(Status.REJECTED, roleAssignment.getStatus());
        });
    }

    /* -----------------------------------------CHALLENGED ACCESS LEGAL-OPS----------------------------------------- */

    @Test //rule challenged_access_create_case_role_legal_ops
    void shouldGrantAccessFor_ChallengedAccessLegalOps() {

        HashMap<String, JsonNode> roleAssignmentAttributes = new HashMap<>();
        roleAssignmentAttributes.put("caseId", convertValueJsonNode("1234567890123456"));
        roleAssignmentAttributes.put("requestedRole", convertValueJsonNode("challenged-access-legal-ops"));
        roleAssignmentAttributes.put("caseType", convertValueJsonNode("notAsylum"));
        roleAssignmentAttributes.put("jurisdiction", convertValueJsonNode("IA"));

        assignmentRequest = TestDataBuilder.buildAssignmentRequestSpecialAccess("challenged-access",
                                                                                "challenged-access-legal-ops",
                                                                                RoleCategory.LEGAL_OPERATIONS,
                                                                                roleAssignmentAttributes,
                                                                                Classification.PUBLIC,
                                                                                GrantType.CHALLENGED,
                                                                                Status.CREATE_REQUESTED,
                                                                                "anyClient",
                                                                                false).build();

        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_CHALLENGED_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        HashMap<String,JsonNode> existingAttributes = new HashMap<>();
        existingAttributes.put("jurisdiction", convertValueJsonNode("IA"));
        existingAttributes.put("caseType", convertValueJsonNode("Asylum"));
        existingAttributes.put("substantive", convertValueJsonNode("Y"));
        executeDroolRules(List.of(TestDataBuilder
                                      .buildExistingRoleForDrools("4772dc44-268f-4d0c-8f83-f0fb662aac84",
                                                                  "anyRoleName",
                                                                  RoleCategory.LEGAL_OPERATIONS,
                                                                  existingAttributes,
                                                                  Classification.PRIVATE,
                                                                  GrantType.STANDARD,
                                                                  RoleType.ORGANISATION)));

        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            Assertions.assertEquals(Status.APPROVED, roleAssignment.getStatus());
            Assertions.assertEquals("Asylum", roleAssignment.getAttributes().get("caseType").asText());
            Assertions.assertEquals(Classification.PUBLIC, roleAssignment.getClassification());
            Assertions.assertEquals(List.of("CCD","ExUI","SSIC", "RefData"),
                                    roleAssignment.getAuthorisations());
        });
    }

    @Test //rule challenged_access_create_case_role_legal_ops
    void shouldRejectAccessFor_ChallengedAccessLegalOps_IncorrectRoleName() {

        HashMap<String, JsonNode> roleAssignmentAttributes = new HashMap<>();
        roleAssignmentAttributes.put("caseId", convertValueJsonNode("1234567890123456"));
        roleAssignmentAttributes.put("requestedRole", convertValueJsonNode("challenged-access-legal_ops"));
        roleAssignmentAttributes.put("caseType", convertValueJsonNode("notAsylum"));
        roleAssignmentAttributes.put("jurisdiction", convertValueJsonNode("IA"));

        assignmentRequest = TestDataBuilder.buildAssignmentRequestSpecialAccess("challenged-access",
                                                                                "challenged-access-admin",
                                                                                RoleCategory.LEGAL_OPERATIONS,
                                                                                roleAssignmentAttributes,
                                                                                Classification.PUBLIC,
                                                                                GrantType.CHALLENGED,
                                                                                Status.CREATE_REQUESTED,
                                                                                "anyClient",
                                                                                false).build();

        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_CHALLENGED_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        HashMap<String,JsonNode> existingAttributes = new HashMap<>();
        existingAttributes.put("jurisdiction", convertValueJsonNode("IA"));
        existingAttributes.put("caseType", convertValueJsonNode("Asylum"));
        existingAttributes.put("substantive", convertValueJsonNode("Y"));
        executeDroolRules(List.of(TestDataBuilder
                                      .buildExistingRoleForDrools("4772dc44-268f-4d0c-8f83-f0fb662aac84",
                                                                  "anyRoleName",
                                                                  RoleCategory.LEGAL_OPERATIONS,
                                                                  existingAttributes,
                                                                  Classification.PRIVATE,
                                                                  GrantType.STANDARD,
                                                                  RoleType.ORGANISATION)));

        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            Assertions.assertEquals(Status.REJECTED, roleAssignment.getStatus());
        });
    }

    @Test //rule challenged_access_create_case_role_legal_ops
    void shouldRejectAccessFor_ChallengedAccessLegalOps_IncorrectFlagEnabled() {

        HashMap<String, JsonNode> roleAssignmentAttributes = new HashMap<>();
        roleAssignmentAttributes.put("caseId", convertValueJsonNode("1234567890123456"));
        roleAssignmentAttributes.put("requestedRole", convertValueJsonNode("challenged-access-legal-ops"));
        roleAssignmentAttributes.put("caseType", convertValueJsonNode("notAsylum"));
        roleAssignmentAttributes.put("jurisdiction", convertValueJsonNode("IA"));

        assignmentRequest = TestDataBuilder.buildAssignmentRequestSpecialAccess("challenged-access",
                                                                                "challenged-access-legal-ops",
                                                                                RoleCategory.ADMIN,
                                                                                roleAssignmentAttributes,
                                                                                Classification.PUBLIC,
                                                                                GrantType.CHALLENGED,
                                                                                Status.CREATE_REQUESTED,
                                                                                "anyClient",
                                                                                false).build();

        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_SPECIFIC_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        HashMap<String,JsonNode> existingAttributes = new HashMap<>();
        existingAttributes.put("jurisdiction", convertValueJsonNode("IA"));
        existingAttributes.put("caseType", convertValueJsonNode("Asylum"));
        existingAttributes.put("substantive", convertValueJsonNode("Y"));
        executeDroolRules(List.of(TestDataBuilder
                                      .buildExistingRoleForDrools("4772dc44-268f-4d0c-8f83-f0fb662aac84",
                                                                  "anyRoleName",
                                                                  RoleCategory.LEGAL_OPERATIONS,
                                                                  existingAttributes,
                                                                  Classification.PRIVATE,
                                                                  GrantType.STANDARD,
                                                                  RoleType.ORGANISATION)));

        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            Assertions.assertEquals(Status.REJECTED, roleAssignment.getStatus());
        });
    }


}
