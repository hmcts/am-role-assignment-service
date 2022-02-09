package uk.gov.hmcts.reform.roleassignment.domain.service.drools;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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

import java.util.HashMap;
import java.util.List;

import static uk.gov.hmcts.reform.roleassignment.util.JacksonUtils.convertValueJsonNode;

@RunWith(MockitoJUnitRunner.class)
class SpecificAccessDroolsTest extends DroolBase {

    @ParameterizedTest
    @CsvSource({
        "specific-access-judiciary,JUDICIAL,STANDARD",
        "specific-access-admin,ADMIN,STANDARD",
        "specific-access-legal-ops,LEGAL_OPERATIONS,STANDARD",
        "specific-access-judiciary,JUDICIAL,BASIC",
        "specific-access-admin,ADMIN,BASIC",
        "specific-access-legal-ops,LEGAL_OPERATIONS,BASIC"
    })
    void shouldGrantAccessFor_SpecificAccess_SelfRequested(String roleName, String roleCategory, String orgGrantType) {

        HashMap<String, JsonNode> roleAssignmentAttributes = new HashMap<>();
        roleAssignmentAttributes.put("caseId", convertValueJsonNode("1234567890123456"));
        roleAssignmentAttributes.put("requestedRole", convertValueJsonNode(roleName));
        roleAssignmentAttributes.put("caseType", convertValueJsonNode("notAsylum"));
        roleAssignmentAttributes.put("jurisdiction", convertValueJsonNode("notIA"));

        assignmentRequest = TestDataBuilder.buildAssignmentRequestSpecialAccess(
            "specific-access",
            "specific-access-requested",
            RoleCategory.valueOf(roleCategory),
            roleAssignmentAttributes,
            Classification.RESTRICTED,
            GrantType.BASIC,
            Status.CREATE_REQUESTED,
            "anyClient",
            true,
            "Access required for reasons"
        )
            .build();

        FeatureFlag featureFlag = FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_SPECIFIC_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        HashMap<String, JsonNode> existingAttributes = new HashMap<>();
        existingAttributes.put("jurisdiction", convertValueJsonNode("IA"));
        existingAttributes.put("caseTypeId", convertValueJsonNode("Asylum"));

        executeDroolRules(List.of(TestDataBuilder
                                      .buildExistingRoleForDrools(
                                          TestDataBuilder.ACTORID,
                                          "judge",
                                          RoleCategory.valueOf(roleCategory),
                                          existingAttributes,
                                          Classification.PRIVATE,
                                          GrantType.valueOf(orgGrantType),
                                          RoleType.ORGANISATION
                                      )));

        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            Assertions.assertEquals(Status.APPROVED, roleAssignment.getStatus());
            Assertions.assertEquals("IA", roleAssignment.getAttributes().get("jurisdiction").asText());
            Assertions.assertEquals("Asylum", roleAssignment.getAttributes().get("caseType").asText());
        });
    }

    @ParameterizedTest
    @CsvSource({
        "specific-access-judiciary,JUDICIAL",
        "specific-access-admin,ADMIN",
        "specific-access-legal-ops,LEGAL_OPERATIONS"
    })
    void shouldGrantAccessFor_SpecificAccess_CaseAllocator(String roleName, String roleCategory) {

        HashMap<String, JsonNode> roleAssignmentAttributes = new HashMap<>();
        roleAssignmentAttributes.put("caseId", convertValueJsonNode("1234567890123456"));
        roleAssignmentAttributes.put("requestedRole", convertValueJsonNode(roleName));
        roleAssignmentAttributes.put("caseType", convertValueJsonNode("notAsylum"));
        roleAssignmentAttributes.put("jurisdiction", convertValueJsonNode("IA"));

        assignmentRequest = TestDataBuilder.buildAssignmentRequestSpecialAccessApprover(
            "specific-access",
            roleName,
            RoleCategory.valueOf(roleCategory),
            roleAssignmentAttributes,
            Classification.RESTRICTED,
            GrantType.SPECIFIC,
            Status.CREATE_REQUESTED,
            "anyClient",
            false,
            "Access required for reasons"
        )
            .build();

        FeatureFlag featureFlag = FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_SPECIFIC_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        HashMap<String, JsonNode> existingAttributes = new HashMap<>();
        existingAttributes.put("jurisdiction", convertValueJsonNode("IA"));
        existingAttributes.put("caseType", convertValueJsonNode("Asylum"));
        existingAttributes.put("managedRoleCategory", convertValueJsonNode(roleCategory));
        existingAttributes.put("managedRole", convertValueJsonNode(roleName));
        existingAttributes.put("caseId", convertValueJsonNode("1234567890123456"));

        executeDroolRules(List.of(TestDataBuilder
                                      .buildExistingRoleForDrools(
                                          TestDataBuilder.CASE_ALLOCATOR_ID,
                                          "case-allocator",
                                          RoleCategory.valueOf(roleCategory),
                                          existingAttributes,
                                          Classification.PRIVATE,
                                          GrantType.STANDARD,
                                          RoleType.ORGANISATION
                                      )));

        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            Assertions.assertEquals(Status.APPROVED, roleAssignment.getStatus());
            Assertions.assertEquals("IA", roleAssignment.getAttributes().get("jurisdiction").asText());
            Assertions.assertEquals("Asylum", roleAssignment.getAttributes().get("caseType").asText());
            Assertions.assertEquals(
                List.of("CCD", "ExUI", "SSIC", "RefData"),
                roleAssignment.getAuthorisations()
            );
        });
    }

    @ParameterizedTest
    @CsvSource({
        "specific-access-judiciary,JUDICIAL",
        "specific-access-admin,ADMIN",
        "specific-access-legal-ops,LEGAL_OPERATIONS"
    })
    void shouldRejectAccessFor_SpecificAccess_CaseAllocator_selfApproval(String roleName, String roleCategory) {

        HashMap<String, JsonNode> roleAssignmentAttributes = new HashMap<>();
        roleAssignmentAttributes.put("caseId", convertValueJsonNode("1234567890123456"));
        roleAssignmentAttributes.put("requestedRole", convertValueJsonNode(roleName));
        roleAssignmentAttributes.put("caseType", convertValueJsonNode("notAsylum"));
        roleAssignmentAttributes.put("jurisdiction", convertValueJsonNode("IA"));

        assignmentRequest = TestDataBuilder.buildAssignmentRequestSpecialAccess(
                "specific-access",
                roleName,
                RoleCategory.valueOf(roleCategory),
                roleAssignmentAttributes,
                Classification.RESTRICTED,
                GrantType.SPECIFIC,
                Status.CREATE_REQUESTED,
                "anyClient",
                false,
                "Access required for reasons"
            )
            .build();

        FeatureFlag featureFlag = FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_SPECIFIC_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        HashMap<String, JsonNode> existingAttributes = new HashMap<>();
        existingAttributes.put("jurisdiction", convertValueJsonNode("IA"));
        existingAttributes.put("caseType", convertValueJsonNode("Asylum"));
        existingAttributes.put("managedRoleCategory", convertValueJsonNode(roleCategory));
        existingAttributes.put("managedRole", convertValueJsonNode(roleName));
        existingAttributes.put("caseId", convertValueJsonNode("1234567890123456"));

        executeDroolRules(List.of(TestDataBuilder
                                      .buildExistingRoleForDrools(
                                          TestDataBuilder.ACTORID,
                                          "case-allocator",
                                          RoleCategory.valueOf(roleCategory),
                                          existingAttributes,
                                          Classification.PRIVATE,
                                          GrantType.STANDARD,
                                          RoleType.ORGANISATION
                                      )));

        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            Assertions.assertEquals(Status.REJECTED, roleAssignment.getStatus());
            Assertions.assertEquals("IA", roleAssignment.getAttributes().get("jurisdiction").asText());
            Assertions.assertEquals("Asylum", roleAssignment.getAttributes().get("caseType").asText());
        });
    }

    @ParameterizedTest
    @CsvSource({
        "specific-access-judiciary,JUDICIAL",
        "specific-access-admin,ADMIN",
        "specific-access-legal-ops,LEGAL_OPERATIONS"
    })
    void shouldGrantAccessFor_SpecificAccess_CaseAllocator_MaxAttributes(String roleName, String roleCategory) {

        HashMap<String, JsonNode> roleAssignmentAttributes = new HashMap<>();
        roleAssignmentAttributes.put("caseId", convertValueJsonNode("1616161616161616"));
        roleAssignmentAttributes.put("requestedRole", convertValueJsonNode(roleName));
        roleAssignmentAttributes.put("caseType", convertValueJsonNode("notAsylum"));
        roleAssignmentAttributes.put("jurisdiction", convertValueJsonNode("IA"));

        assignmentRequest = TestDataBuilder.buildAssignmentRequestSpecialAccessApprover(
            "specific-access",
            roleName,
            RoleCategory.valueOf(roleCategory),
            roleAssignmentAttributes,
            Classification.RESTRICTED,
            GrantType.SPECIFIC,
            Status.CREATE_REQUESTED,
            "anyClient",
            false,
            "Access required for reasons"
        )
            .build();

        FeatureFlag featureFlag = FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_SPECIFIC_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        HashMap<String, JsonNode> existingAttributes = new HashMap<>();
        existingAttributes.put("jurisdiction", convertValueJsonNode("IA"));
        existingAttributes.put("caseType", convertValueJsonNode("Asylum"));
        existingAttributes.put("managedRoleCategory", convertValueJsonNode(roleCategory));
        existingAttributes.put("managedRole", convertValueJsonNode(roleName));
        existingAttributes.put("baseLocation", convertValueJsonNode("Newcastle"));
        existingAttributes.put("region", convertValueJsonNode("north-east"));
        existingAttributes.put("caseId", convertValueJsonNode("1616161616161616"));

        executeDroolRules(List.of(TestDataBuilder
                                      .buildExistingRoleForDrools(
                                          TestDataBuilder.CASE_ALLOCATOR_ID,
                                          "case-allocator",
                                          RoleCategory.valueOf(roleCategory),
                                          existingAttributes,
                                          Classification.PRIVATE,
                                          GrantType.STANDARD,
                                          RoleType.ORGANISATION
                                      )));

        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            Assertions.assertEquals(Status.APPROVED, roleAssignment.getStatus());
            Assertions.assertEquals("IA", roleAssignment.getAttributes().get("jurisdiction").asText());
            Assertions.assertEquals("Asylum", roleAssignment.getAttributes().get("caseType").asText());
            Assertions.assertEquals(List.of("CCD", "ExUI", "SSIC", "RefData"), roleAssignment.getAuthorisations());
        });
    }

    @ParameterizedTest
    @CsvSource({
        "specific-access-judiciary,JUDICIAL",
        "specific-access-admin,ADMIN",
        "specific-access-legal-ops,LEGAL_OPERATIONS"
    })
    void shouldGrantAccessFor_SpecificAccess_XuiClient(String roleName, String roleCategory) {

        HashMap<String, JsonNode> roleAssignmentAttributes = new HashMap<>();
        roleAssignmentAttributes.put("caseId", convertValueJsonNode("1234567890123456"));
        roleAssignmentAttributes.put("jurisdiction", convertValueJsonNode("notIA"));
        roleAssignmentAttributes.put("caseTypeId", convertValueJsonNode("notAsylum"));
        roleAssignmentAttributes.put("requestedRole", convertValueJsonNode(roleName));

        assignmentRequest = TestDataBuilder.buildAssignmentRequestSpecialAccess(
            "specific-access",
            "specific-access-granted",
            RoleCategory.valueOf(roleCategory),
            roleAssignmentAttributes,
            Classification.PRIVATE,
            GrantType.BASIC,
            Status.CREATE_REQUESTED,
            "xui_webapp",
            true,
            "Access required for reasons"
        )
            .build();

        FeatureFlag featureFlag = FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_SPECIFIC_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        HashMap<String, JsonNode> existingAttributes = new HashMap<>();
        existingAttributes.put("jurisdiction", convertValueJsonNode("IA"));
        existingAttributes.put("caseTypeId", convertValueJsonNode("Asylum"));
        existingAttributes.put("requestedRole", convertValueJsonNode(roleName));

        executeDroolRules(List.of(TestDataBuilder
                                      .buildExistingRoleForDrools(
                                          "4772dc44-268f-4d0c-8f83-f0fb662aac84",
                                          "specific-access-requested",
                                          RoleCategory.valueOf(roleCategory),
                                          existingAttributes,
                                          Classification.PRIVATE,
                                          GrantType.BASIC,
                                          RoleType.CASE
                                      )));

        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            Assertions.assertEquals(Status.APPROVED, roleAssignment.getStatus());
            Assertions.assertEquals("IA", roleAssignment.getAttributes().get("jurisdiction").asText());
            Assertions.assertEquals("Asylum", roleAssignment.getAttributes().get("caseType").asText());
        });
    }

    @ParameterizedTest
    @CsvSource({
        "JUDICIAL",
        "ADMIN",
        "LEGAL_OPERATIONS"
    })
    void shouldDeleteAccessFor_SpecificAccess(String roleCategory) {

        HashMap<String, JsonNode> roleAssignmentAttributes = new HashMap<>();
        roleAssignmentAttributes.put("requestedRole", convertValueJsonNode("specific-access"));

        assignmentRequest = TestDataBuilder.buildAssignmentRequestSpecialAccess(
            "specific-access",
            "specific-access-denied",
            RoleCategory.valueOf(roleCategory),
            roleAssignmentAttributes,
            Classification.PRIVATE,
            GrantType.BASIC,
            Status.DELETE_REQUESTED,
            "anyClient",
            false,
            "Access required for reasons"
        )
            .build();

        FeatureFlag featureFlag = FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_SPECIFIC_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        buildExecuteKieSession();

        assignmentRequest.getRequestedRoles()
            .forEach(roleAssignment -> Assertions.assertEquals(Status.DELETE_APPROVED, roleAssignment.getStatus()));
    }

    @ParameterizedTest
    @CsvSource({
        "JUDICIAL",
        "ADMIN",
        "LEGAL_OPERATIONS"
    })
    void shouldDeleteAccessFor_SpecificAccess_XuiClient(String roleCategory) {

        HashMap<String, JsonNode> roleAssignmentAttributes = new HashMap<>();
        roleAssignmentAttributes.put("requestedRole", convertValueJsonNode("specific-access"));
        assignmentRequest = TestDataBuilder.buildAssignmentRequestSpecialAccess(
            "specific-access",
            "specific-access-requested",
            RoleCategory.valueOf(roleCategory),
            roleAssignmentAttributes,
            Classification.PRIVATE,
            GrantType.BASIC,
            Status.DELETE_REQUESTED,
            "xui_webapp",
            false,
            "Access required for reasons"
        )
            .build();

        FeatureFlag featureFlag = FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_SPECIFIC_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        buildExecuteKieSession();

        assignmentRequest.getRequestedRoles()
            .forEach(roleAssignment -> Assertions.assertEquals(Status.DELETE_APPROVED, roleAssignment.getStatus()));
    }

    @ParameterizedTest
    @CsvSource({
        "specific-access-judiciary,JUDICIAL",
        "specific-access-admin,ADMIN",
        "specific-access-legal-ops,LEGAL_OPERATIONS"
    })
    void shouldRejectAccessFor_SpecificAccess_IncorrectFlagEnabled(String roleName, String roleCategory) {

        HashMap<String, JsonNode> roleAssignmentAttributes = new HashMap<>();
        roleAssignmentAttributes.put("caseId", convertValueJsonNode("1234567890123456"));
        roleAssignmentAttributes.put("requestedRole", convertValueJsonNode(roleName));
        roleAssignmentAttributes.put("caseType", convertValueJsonNode("notAsylum"));
        roleAssignmentAttributes.put("jurisdiction", convertValueJsonNode("notIA"));

        assignmentRequest = TestDataBuilder.buildAssignmentRequestSpecialAccess(
            "specific-access",
            "specific-access-requested",
            RoleCategory.valueOf(roleCategory),
            roleAssignmentAttributes,
            Classification.RESTRICTED,
            GrantType.BASIC,
            Status.CREATE_REQUESTED,
            "anyClient",
            false,
            "Access required for reasons"
        ).build();

        FeatureFlag featureFlag = FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_CHALLENGED_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        HashMap<String, JsonNode> existingAttributes = new HashMap<>();
        existingAttributes.put("jurisdiction", convertValueJsonNode("IA"));
        existingAttributes.put("caseTypeId", convertValueJsonNode("Asylum"));

        executeDroolRules(List.of(TestDataBuilder
                                      .buildExistingRoleForDrools(
                                          "4772dc44-268f-4d0c-8f83-f0fb662aac84",
                                          "case-allocator",
                                          RoleCategory.valueOf(roleCategory),
                                          existingAttributes,
                                          Classification.PRIVATE,
                                          GrantType.STANDARD,
                                          RoleType.ORGANISATION
                                      )));

        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> Assertions.assertEquals(
            Status.REJECTED,
            roleAssignment.getStatus()
        ));
    }

    @ParameterizedTest
    @CsvSource({
        "specific-access-judiciary,JUDICIAL",
        "specific-access-admin,ADMIN",
        "specific-access-legal-ops,LEGAL_OPERATIONS"
    })
    void shouldRejectAccessFor_SpecificAccess_InsufficientNotes(String roleName, String roleCategory) {

        HashMap<String, JsonNode> roleAssignmentAttributes = new HashMap<>();
        roleAssignmentAttributes.put("caseId", convertValueJsonNode("1234567890123456"));
        roleAssignmentAttributes.put("requestedRole", convertValueJsonNode(roleName));
        roleAssignmentAttributes.put("caseType", convertValueJsonNode("notAsylum"));
        roleAssignmentAttributes.put("jurisdiction", convertValueJsonNode("notIA"));

        assignmentRequest = TestDataBuilder.buildAssignmentRequestSpecialAccess(
            "specific-access",
            "specific-access-requested",
            RoleCategory.valueOf(roleCategory),
            roleAssignmentAttributes,
            Classification.RESTRICTED,
            GrantType.BASIC,
            Status.CREATE_REQUESTED,
            "anyClient",
            false,
            "A"
        )
            .build();

        FeatureFlag featureFlag = FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_SPECIFIC_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        HashMap<String, JsonNode> existingAttributes = new HashMap<>();
        existingAttributes.put("jurisdiction", convertValueJsonNode("IA"));
        existingAttributes.put("caseTypeId", convertValueJsonNode("Asylum"));

        executeDroolRules(List.of(TestDataBuilder
                                      .buildExistingRoleForDrools(
                                          "4772dc44-268f-4d0c-8f83-f0fb662aac84",
                                          "judge",
                                          RoleCategory.valueOf(roleCategory),
                                          existingAttributes,
                                          Classification.PRIVATE,
                                          GrantType.STANDARD,
                                          RoleType.ORGANISATION
                                      )));

        assignmentRequest.getRequestedRoles().forEach(roleAssignment ->
                                                  Assertions.assertEquals(Status.REJECTED, roleAssignment.getStatus()));
    }

    @ParameterizedTest
    @CsvSource({
        "specific-access-judge,JUDICIAL",
        "specific-access-administrator,ADMIN",
        "specific-access-legal-operator,LEGAL_OPERATIONS"
    })
    void shouldRejectAccessFor_Specific_IncorrectRoleName(String roleName, String roleCategory) {

        HashMap<String, JsonNode> roleAssignmentAttributes = new HashMap<>();
        roleAssignmentAttributes.put("caseId", convertValueJsonNode("1234567890123456"));
        roleAssignmentAttributes.put("requestedRole", convertValueJsonNode(roleName));
        roleAssignmentAttributes.put("caseType", convertValueJsonNode("notAsylum"));
        roleAssignmentAttributes.put("jurisdiction", convertValueJsonNode("notIA"));

        assignmentRequest = TestDataBuilder.buildAssignmentRequestSpecialAccess(
            "specific-access",
            roleName,
            RoleCategory.valueOf(roleCategory),
            roleAssignmentAttributes,
            Classification.RESTRICTED,
            GrantType.SPECIFIC,
            Status.CREATE_REQUESTED,
            "anyClient",
            false,
            "Access required for reasons"
        )
            .build();

        FeatureFlag featureFlag = FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_SPECIFIC_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        HashMap<String, JsonNode> existingAttributes = new HashMap<>();
        existingAttributes.put("jurisdiction", convertValueJsonNode("notIA"));
        existingAttributes.put("caseTypeId", convertValueJsonNode("notAsylum"));

        executeDroolRules(List.of(TestDataBuilder
                                      .buildExistingRoleForDrools(
                                          "4772dc44-268f-4d0c-8f83-f0fb662aac84",
                                          "case-allocator",
                                          RoleCategory.valueOf(roleCategory),
                                          existingAttributes,
                                          Classification.PRIVATE,
                                          GrantType.STANDARD,
                                          RoleType.ORGANISATION
                                      )));

        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> Assertions.assertEquals(
            Status.REJECTED,
            roleAssignment.getStatus()
        ));
    }

    @ParameterizedTest
    @CsvSource({
        "JUDICIAL",
        "ADMIN",
        "LEGAL_OPERATIONS"
    })
    void shouldRejectDeleteAccessFor_SpecificAccess_IncorrectRoleName(String roleCategory) {

        HashMap<String, JsonNode> roleAssignmentAttributes = new HashMap<>();
        roleAssignmentAttributes.put("requestedRole", convertValueJsonNode("specific-access"));

        assignmentRequest = TestDataBuilder.buildAssignmentRequestSpecialAccess(
            "specific-access",
            "specific-access",
            RoleCategory.valueOf(roleCategory),
            roleAssignmentAttributes,
            Classification.PRIVATE,
            GrantType.BASIC,
            Status.DELETE_REQUESTED,
            "anyClient",
            false,
            "Access required for reasons"
        )
            .build();

        FeatureFlag featureFlag = FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_SPECIFIC_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        buildExecuteKieSession();

        assignmentRequest.getRequestedRoles()
            .forEach(roleAssignment -> Assertions.assertEquals(Status.DELETE_REJECTED, roleAssignment.getStatus()));
    }

    @ParameterizedTest
    @CsvSource({
        "specific-access-judge,JUDICIAL",
        "specific-access-administrator,ADMIN",
        "specific-access-legal-operator,LEGAL_OPERATIONS"
    })
    void shouldRejectAccessFor_SpecificAccess_NotXuiClient(String roleName, String roleCategory) {

        HashMap<String, JsonNode> roleAssignmentAttributes = new HashMap<>();
        roleAssignmentAttributes.put("caseId", convertValueJsonNode("1234567890123456"));
        roleAssignmentAttributes.put("jurisdiction", convertValueJsonNode("IA"));
        roleAssignmentAttributes.put("caseTypeId", convertValueJsonNode("Asylum"));
        roleAssignmentAttributes.put("requestedRole", convertValueJsonNode(roleName));

        assignmentRequest = TestDataBuilder.buildAssignmentRequestSpecialAccess(
            "specific-access",
            "specific-access-granted",
            RoleCategory.valueOf(roleCategory),
            roleAssignmentAttributes,
            Classification.PRIVATE,
            GrantType.BASIC,
            Status.CREATE_REQUESTED,
            "not_xui_webapp",
            true,
            "Access required for reasons"
        ).build();

        FeatureFlag featureFlag = FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_SPECIFIC_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        HashMap<String, JsonNode> existingAttributes = new HashMap<>();
        existingAttributes.put("jurisdiction", convertValueJsonNode("IA"));
        existingAttributes.put("caseTypeId", convertValueJsonNode("Asylum"));
        existingAttributes.put("requestedRole", convertValueJsonNode(roleName));

        executeDroolRules(List.of(TestDataBuilder
                                      .buildExistingRoleForDrools(
                                          "4772dc44-268f-4d0c-8f83-f0fb662aac84",
                                          "specific-access-requested",
                                          RoleCategory.valueOf(roleCategory),
                                          existingAttributes,
                                          Classification.PRIVATE,
                                          GrantType.STANDARD,
                                          RoleType.CASE
                                      )));

        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> Assertions.assertEquals(
            Status.REJECTED,
            roleAssignment.getStatus()
        ));
    }

    @ParameterizedTest
    @CsvSource({
        "specific-access-judge,JUDICIAL",
        "specific-access-administrator,ADMIN",
        "specific-access-legal-operator,LEGAL_OPERATIONS"
    })
    void shouldRejectAccessFor_SpecificAccess_CaseAllocator(String roleName, String roleCategory) {

        HashMap<String, JsonNode> roleAssignmentAttributes = new HashMap<>();
        roleAssignmentAttributes.put("caseId", convertValueJsonNode("1234567890123456"));
        roleAssignmentAttributes.put("requestedRole", convertValueJsonNode(roleName));
        roleAssignmentAttributes.put("caseType", convertValueJsonNode("notAsylum"));
        roleAssignmentAttributes.put("jurisdiction", convertValueJsonNode("IA"));

        assignmentRequest = TestDataBuilder.buildAssignmentRequestSpecialAccess(
            "specific-access",
            roleName,
            RoleCategory.valueOf(roleCategory),
            roleAssignmentAttributes,
            Classification.RESTRICTED,
            GrantType.SPECIFIC,
            Status.CREATE_REQUESTED,
            "anyClient",
            false,
            "Access required for reasons"
        ).build();

        FeatureFlag featureFlag = FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_SPECIFIC_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        HashMap<String, JsonNode> existingAttributes = new HashMap<>();
        existingAttributes.put("jurisdiction", convertValueJsonNode("IA"));
        existingAttributes.put("caseType", convertValueJsonNode("Asylum"));
        existingAttributes.put("managedRoleCategory", convertValueJsonNode("notCorrect"));
        existingAttributes.put("managedRole", convertValueJsonNode(roleName));
        existingAttributes.put("caseId", convertValueJsonNode("1234567890123456"));

        executeDroolRules(List.of(TestDataBuilder
                                      .buildExistingRoleForDrools(
                                          "4772dc44-268f-4d0c-8f83-f0fb662aac84",
                                          "case-allocator",
                                          RoleCategory.valueOf(roleCategory),
                                          existingAttributes,
                                          Classification.PRIVATE,
                                          GrantType.STANDARD,
                                          RoleType.ORGANISATION
                                      )));

        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            Assertions.assertEquals(Status.REJECTED, roleAssignment.getStatus());
            Assertions.assertNull(roleAssignment.getAuthorisations());
        });
    }
}
