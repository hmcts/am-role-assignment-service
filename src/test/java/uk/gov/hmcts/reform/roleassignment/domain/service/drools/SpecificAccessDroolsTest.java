package uk.gov.hmcts.reform.roleassignment.domain.service.drools;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
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

@RunWith(MockitoJUnitRunner.class)
class SpecificAccessDroolsTest extends DroolBase {

    /* -----------------------------------------SPECIFIC ACCESS JUDICIAL----------------------------------------- */

    @Test //rule create_specific_access_requested_judiciary_case_role
    void shouldGrantAccessFor_SpecificAccessJudiciary() {

        HashMap<String, JsonNode> roleAssignmentAttributes = new HashMap<>();
        roleAssignmentAttributes.put("caseId", convertValueJsonNode("1234567890123456"));
        roleAssignmentAttributes.put("requestedRole", convertValueJsonNode("specific-access-judiciary"));
        roleAssignmentAttributes.put("caseType", convertValueJsonNode("notAsylum"));
        roleAssignmentAttributes.put("jurisdiction", convertValueJsonNode("notIA"));

        assignmentRequest = TestDataBuilder.buildAssignmentRequestSpecificAccess("specific-access",
                                                                                 "specific-access-requested",
                                                                                 RoleCategory.JUDICIAL,
                                                                                 roleAssignmentAttributes,
                                                                                 Classification.PRIVATE,
                                                                                 GrantType.BASIC,
                                                                                 Status.CREATE_REQUESTED,
                                                                                 "anyClient",
                                                                                 false).build();

        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_SPECIFIC_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        HashMap<String,JsonNode> existingAttributes = new HashMap<>();
        existingAttributes.put("jurisdiction",convertValueJsonNode("IA"));
        existingAttributes.put("caseTypeId",convertValueJsonNode("Asylum"));

        executeDroolRules(List.of(TestDataBuilder
                                      .buildExistingRoleForDrools("4772dc44-268f-4d0c-8f83-f0fb662aac84",
                                                                  "case-allocator",
                                                                  RoleCategory.JUDICIAL,
                                                                  existingAttributes,
                                                                  Classification.PRIVATE,
                                                                  GrantType.STANDARD,
                                                                  RoleType.ORGANISATION)));

        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            Assertions.assertEquals(Status.APPROVED, roleAssignment.getStatus());
            Assertions.assertEquals("IA", roleAssignment.getAttributes().get("jurisdiction").asText());
            Assertions.assertEquals("Asylum", roleAssignment.getAttributes().get("caseType").asText());
        });
    }

    @Test //rule delete_specific_access_requested_case_role
    void shouldDeleteAccessFor_SpecificAccessJudiciary() {

        HashMap<String, JsonNode> roleAssignmentAttributes = new HashMap<>();

        assignmentRequest = TestDataBuilder.buildAssignmentRequestSpecificAccess("specific-access",
                                                                                 "specific-access-denied",
                                                                                 RoleCategory.JUDICIAL,
                                                                                 roleAssignmentAttributes,
                                                                                 Classification.PRIVATE,
                                                                                 GrantType.BASIC,
                                                                                 Status.DELETE_REQUESTED,
                                                                                 "anyClient",
                                                                                 false).build();

        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_SPECIFIC_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        buildExecuteKieSession();

        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            Assertions.assertEquals(Status.DELETE_APPROVED, roleAssignment.getStatus());
        });
    }

    @Test //rule create_specific_access_granted_or_denied_case_role
    void shouldGrantAccessFor_SpecificAccessJudiciary_Xui() {

        HashMap<String, JsonNode> roleAssignmentAttributes = new HashMap<>();
        roleAssignmentAttributes.put("caseId", convertValueJsonNode("1234567890123456"));
        roleAssignmentAttributes.put("jurisdiction",convertValueJsonNode("notIA"));
        roleAssignmentAttributes.put("caseTypeId",convertValueJsonNode("notAsylum"));
        roleAssignmentAttributes.put("requestedRole", convertValueJsonNode("specific-access-judiciary"));

        assignmentRequest = TestDataBuilder.buildAssignmentRequestSpecificAccess("specific-access",
                                                                                 "specific-access-granted",
                                                                                 RoleCategory.JUDICIAL,
                                                                                 roleAssignmentAttributes,
                                                                                 Classification.PRIVATE,
                                                                                 GrantType.BASIC,
                                                                                 Status.CREATE_REQUESTED,
                                                                                 "xui_webapp",
                                                                                 true).build();

        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_SPECIFIC_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        HashMap<String,JsonNode> existingAttributes = new HashMap<>();
        existingAttributes.put("jurisdiction",convertValueJsonNode("IA"));
        existingAttributes.put("caseTypeId",convertValueJsonNode("Asylum"));
        existingAttributes.put("requestedRole", convertValueJsonNode("specific-access-judiciary"));

        executeDroolRules(List.of(TestDataBuilder
                                      .buildExistingRoleForDrools("4772dc44-268f-4d0c-8f83-f0fb662aac84",
                                                                  "specific-access-requested",
                                                                  RoleCategory.JUDICIAL,
                                                                  existingAttributes,
                                                                  Classification.PRIVATE,
                                                                  GrantType.STANDARD,
                                                                  RoleType.CASE)));

        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            Assertions.assertEquals(Status.APPROVED, roleAssignment.getStatus());
            Assertions.assertEquals("IA", roleAssignment.getAttributes().get("jurisdiction").asText());
            Assertions.assertEquals("Asylum", roleAssignment.getAttributes().get("caseType").asText());
        });
    }

    @Test //rule delete_specific_access_granted_or_denied_case_role
    void shouldDeleteAccessFor_SpecificAccessJudiciary_Xui() {

        HashMap<String, JsonNode> roleAssignmentAttributes = new HashMap<>();

        assignmentRequest = TestDataBuilder.buildAssignmentRequestSpecificAccess("specific-access",
                                                                                 "specific-access-requested",
                                                                                 RoleCategory.JUDICIAL,
                                                                                 roleAssignmentAttributes,
                                                                                 Classification.PRIVATE,
                                                                                 GrantType.BASIC,
                                                                                 Status.DELETE_REQUESTED,
                                                                                 "xui_webapp",
                                                                                 false).build();

        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_SPECIFIC_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        buildExecuteKieSession();

        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            Assertions.assertEquals(Status.DELETE_APPROVED, roleAssignment.getStatus());
        });
    }

    @Test //rule case_allocator_create_specific_access_judicial_case_role
    void shouldGrantAccessFor_SpecificAccessJudiciary_ExistingRoleCaseAllocator() {

        HashMap<String, JsonNode> roleAssignmentAttributes = new HashMap<>();
        roleAssignmentAttributes.put("caseId", convertValueJsonNode("1234567890123456"));
        roleAssignmentAttributes.put("requestedRole", convertValueJsonNode("specific-access-judiciary"));
        roleAssignmentAttributes.put("caseType", convertValueJsonNode("notAsylum"));
        roleAssignmentAttributes.put("jurisdiction", convertValueJsonNode("IA"));

        assignmentRequest = TestDataBuilder.buildAssignmentRequestSpecificAccess("specific-access",
                                                                                 "specific-access-judiciary",
                                                                                 RoleCategory.JUDICIAL,
                                                                                 roleAssignmentAttributes,
                                                                                 Classification.PRIVATE,
                                                                                 GrantType.BASIC,
                                                                                 Status.CREATE_REQUESTED,
                                                                                 "anyClient",
                                                                                 false).build();

        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_SPECIFIC_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        HashMap<String,JsonNode> existingAttributes = new HashMap<>();
        existingAttributes.put("jurisdiction", convertValueJsonNode("IA"));
        existingAttributes.put("caseType", convertValueJsonNode("Asylum"));
        existingAttributes.put("managedRoleCategory", convertValueJsonNode("JUDICIAL"));
        existingAttributes.put("managedRole", convertValueJsonNode("specific-access-judiciary"));
        existingAttributes.put("caseId", convertValueJsonNode("1234567890123456"));

        executeDroolRules(List.of(TestDataBuilder
                                      .buildExistingRoleForDrools("4772dc44-268f-4d0c-8f83-f0fb662aac84",
                                                                  "case-allocator",
                                                                  RoleCategory.JUDICIAL,
                                                                  existingAttributes,
                                                                  Classification.PRIVATE,
                                                                  GrantType.STANDARD,
                                                                  RoleType.ORGANISATION)));

        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            Assertions.assertEquals(Status.APPROVED, roleAssignment.getStatus());
            Assertions.assertEquals("IA", roleAssignment.getAttributes().get("jurisdiction").asText());
            Assertions.assertEquals("Asylum", roleAssignment.getAttributes().get("caseType").asText());
            Assertions.assertEquals(List.of("CCD","ExUI","SSIC", "RefData"),
                                    roleAssignment.getAuthorisations());
        });
    }

    /* -----------------------------------------SPECIFIC ACCESS LEGAL OPS----------------------------------------- */

    @Test //rule create_specific_access_requested_legal_ops_case_role
    void shouldGrantAccessFor_SpecificAccessLegalOps() {

        HashMap<String, JsonNode> roleAssignmentAttributes = new HashMap<>();
        roleAssignmentAttributes.put("jurisdiction", convertValueJsonNode("anyJurisdiction"));
        roleAssignmentAttributes.put("caseType", convertValueJsonNode("anyCase"));
        roleAssignmentAttributes.put("caseId",convertValueJsonNode("9234567890123456"));
        roleAssignmentAttributes.put("requestedRole", convertValueJsonNode("specific-access-legal-ops"));

        assignmentRequest = TestDataBuilder.buildAssignmentRequestSpecificAccess("specific-access",
                                                                                 "specific-access-requested",
                                                                                 RoleCategory.LEGAL_OPERATIONS,
                                                                                 roleAssignmentAttributes,
                                                                                 Classification.PRIVATE,
                                                                                 GrantType.BASIC,
                                                                                 Status.CREATE_REQUESTED,
                                                                                 "anyClient",
                                                                                 false).build();

        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_SPECIFIC_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        HashMap<String,JsonNode> existingAttributes = new HashMap<>();
        existingAttributes.put("jurisdiction",convertValueJsonNode("IA"));
        existingAttributes.put("caseTypeId",convertValueJsonNode("Asylum"));

        executeDroolRules(List.of(TestDataBuilder
                                      .buildExistingRoleForDrools("4772dc44-268f-4d0c-8f83-f0fb662aac84",
                                                                  "case-allocator",
                                                                  RoleCategory.LEGAL_OPERATIONS,
                                                                  existingAttributes,
                                                                  Classification.PRIVATE,
                                                                  GrantType.STANDARD,
                                                                  RoleType.ORGANISATION)));

        assignmentRequest.getRequestedRoles().forEach(e -> {
            Assertions.assertEquals(Status.APPROVED, e.getStatus());
            Assertions.assertEquals("IA",e.getAttributes().get("jurisdiction").asText());
            Assertions.assertEquals("Asylum",e.getAttributes().get("caseType").asText());
        });

    }

    /* -------------------------------------------SPECIFIC ACCESS ADMIN------------------------------------------- */

    @Test //rule create_specific_access_requested_admin_case_role
    void shouldGrantAccessFor_SpecificAccessAdmin() {
        HashMap<String, JsonNode> roleAssignmentAttributes = new HashMap<>();
        roleAssignmentAttributes.put("jurisdiction", convertValueJsonNode("anyJurisdiction"));
        roleAssignmentAttributes.put("caseType",convertValueJsonNode("anyCase"));
        roleAssignmentAttributes.put("caseId",convertValueJsonNode("1234567890123456"));
        roleAssignmentAttributes.put("requestedRole", convertValueJsonNode("specific-access-admin"));

        assignmentRequest = TestDataBuilder.buildAssignmentRequestSpecificAccess("specific-access",
                                                                                 "specific-access-requested",
                                                                                 RoleCategory.ADMIN,
                                                                                 roleAssignmentAttributes,
                                                                                 Classification.PRIVATE,
                                                                                 GrantType.BASIC,
                                                                                 Status.CREATE_REQUESTED,
                                                                                 "anyClient",
                                                                                 false).build();

        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_SPECIFIC_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        HashMap<String,JsonNode> existingAttributes = new HashMap<>();
        existingAttributes.put("jurisdiction",convertValueJsonNode("IA"));
        existingAttributes.put("caseTypeId",convertValueJsonNode("Asylum"));

        executeDroolRules(List.of(TestDataBuilder
                                      .buildExistingRoleForDrools("4772dc44-268f-4d0c-8f83-f0fb662aac84",
                                                                  "case-allocator",
                                                                  RoleCategory.ADMIN,
                                                                  existingAttributes,
                                                                  Classification.PRIVATE,
                                                                  GrantType.STANDARD,
                                                                  RoleType.ORGANISATION)));

        assignmentRequest.getRequestedRoles().forEach(e -> {
            Assertions.assertEquals(Status.APPROVED,e.getStatus());
            Assertions.assertEquals("IA",e.getAttributes().get("jurisdiction").asText());
            Assertions.assertEquals("Asylum",e.getAttributes().get("caseType").asText());
        });

    }

}
