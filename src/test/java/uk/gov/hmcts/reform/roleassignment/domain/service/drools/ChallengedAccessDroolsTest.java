package uk.gov.hmcts.reform.roleassignment.domain.service.drools;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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

    @ParameterizedTest
    @CsvSource({
        "challenged-access-judiciary,JUDICIAL",
        "challenged-access-admin,ADMIN",
        "challenged-access-legal-ops,LEGAL_OPERATIONS",
    })
    void shouldGrantAccessFor_ChallengedAccess(String roleName, String roleCategory) {
        HashMap<String, JsonNode> roleAssignmentAttributes = new HashMap<>();
        roleAssignmentAttributes.put("caseId", convertValueJsonNode("1234567890123456"));
        roleAssignmentAttributes.put("requestedRole", convertValueJsonNode(roleName));
        roleAssignmentAttributes.put("caseType", convertValueJsonNode("notAsylum"));
        roleAssignmentAttributes.put("jurisdiction", convertValueJsonNode("IA"));

        assignmentRequest = TestDataBuilder.buildAssignmentRequestSpecialAccess(
            "challenged-access",
            roleName,
            RoleCategory.valueOf(roleCategory),
            roleAssignmentAttributes,
            Classification.PUBLIC,
            GrantType.CHALLENGED,
            Status.CREATE_REQUESTED,
            "anyClient",
            false,
            "Access required for reasons"
        )
            .build();

        FeatureFlag featureFlag = FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_CHALLENGED_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        HashMap<String, JsonNode> existingAttributes = new HashMap<>();
        existingAttributes.put("jurisdiction", convertValueJsonNode("IA"));
        existingAttributes.put("caseType", convertValueJsonNode("Asylum"));
        existingAttributes.put("substantive", convertValueJsonNode("Y"));
        executeDroolRules(List.of(TestDataBuilder
                                      .buildExistingRoleForDrools(
                                          "4772dc44-268f-4d0c-8f83-f0fb662aac84",
                                          "anyRoleName",
                                          RoleCategory.valueOf(roleCategory),
                                          existingAttributes,
                                          Classification.PRIVATE,
                                          GrantType.STANDARD,
                                          RoleType.ORGANISATION
                                      )));

        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            Assertions.assertEquals(Status.APPROVED, roleAssignment.getStatus());
            Assertions.assertEquals("Asylum", roleAssignment.getAttributes().get("caseType").asText());
            Assertions.assertEquals(Classification.PUBLIC, roleAssignment.getClassification());
            Assertions.assertEquals(
                List.of("CCD", "ExUI", "SSIC", "RefData"),
                roleAssignment.getAuthorisations()
            );
        });
    }

    @ParameterizedTest
    @CsvSource({
        "challenged-access-judiciary,JUDICIAL",
        "challenged-access-admin,ADMIN",
        "challenged-access-legal-ops,LEGAL_OPERATIONS",
    })
    void shouldGrantAccessFor_ChallengedAccess_MaxAttributes(String roleName, String roleCategory) {
        HashMap<String, JsonNode> roleAssignmentAttributes = new HashMap<>();
        roleAssignmentAttributes.put("caseId", convertValueJsonNode("1616161616161616"));
        roleAssignmentAttributes.put("requestedRole", convertValueJsonNode(roleName));
        roleAssignmentAttributes.put("caseType", convertValueJsonNode("notAsylum"));
        roleAssignmentAttributes.put("jurisdiction", convertValueJsonNode("IA"));

        assignmentRequest = TestDataBuilder.buildAssignmentRequestSpecialAccess(
            "challenged-access",
            roleName,
            RoleCategory.valueOf(roleCategory),
            roleAssignmentAttributes,
            Classification.PUBLIC,
            GrantType.CHALLENGED,
            Status.CREATE_REQUESTED,
            "anyClient",
            false,
            "Access required for reasons"
        )
            .build();

        FeatureFlag featureFlag = FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_CHALLENGED_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        HashMap<String, JsonNode> existingAttributes = new HashMap<>();
        existingAttributes.put("jurisdiction", convertValueJsonNode("IA"));
        existingAttributes.put("caseType", convertValueJsonNode("Asylum"));
        existingAttributes.put("substantive", convertValueJsonNode("Y"));
        executeDroolRules(List.of(TestDataBuilder
                                      .buildExistingRoleForDrools(
                                          "4772dc44-268f-4d0c-8f83-f0fb662aac84",
                                          "anyRoleName",
                                          RoleCategory.valueOf(roleCategory),
                                          existingAttributes,
                                          Classification.PRIVATE,
                                          GrantType.STANDARD,
                                          RoleType.ORGANISATION
                                      )));

        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            Assertions.assertEquals(Status.APPROVED, roleAssignment.getStatus());
            Assertions.assertEquals("Asylum", roleAssignment.getAttributes().get("caseType").asText());
            Assertions.assertEquals(Classification.PUBLIC, roleAssignment.getClassification());
            Assertions.assertEquals(
                List.of("CCD", "ExUI", "SSIC", "RefData"),
                roleAssignment.getAuthorisations()
            );
        });
    }

    @ParameterizedTest
    @CsvSource({
        "challenged-access-legal-ops,JUDICIAL",
        "challenged-access-judiciary,ADMIN",
        "challenged-access-admin,LEGAL_OPERATIONS",
    })
    void shouldRejectAccessFor_ChallengedAccess_IncorrectRoleName(String roleName, String roleCategory) {

        HashMap<String, JsonNode> roleAssignmentAttributes = new HashMap<>();
        roleAssignmentAttributes.put("caseId", convertValueJsonNode("1234567890123456"));
        roleAssignmentAttributes.put("requestedRole", convertValueJsonNode(roleName));
        roleAssignmentAttributes.put("caseType", convertValueJsonNode("notAsylum"));
        roleAssignmentAttributes.put("jurisdiction", convertValueJsonNode("IA"));

        assignmentRequest = TestDataBuilder.buildAssignmentRequestSpecialAccess(
            "challenged-access",
            roleName,
            RoleCategory.valueOf(roleCategory),
            roleAssignmentAttributes,
            Classification.PUBLIC,
            GrantType.CHALLENGED,
            Status.CREATE_REQUESTED,
            "anyClient",
            false,
            "Access required for reasons"
        )
            .build();

        FeatureFlag featureFlag = FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_CHALLENGED_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        HashMap<String, JsonNode> existingAttributes = new HashMap<>();
        existingAttributes.put("jurisdiction", convertValueJsonNode("IA"));
        existingAttributes.put("caseType", convertValueJsonNode("Asylum"));
        existingAttributes.put("substantive", convertValueJsonNode("Y"));
        executeDroolRules(List.of(TestDataBuilder
                                      .buildExistingRoleForDrools(
                                          "4772dc44-268f-4d0c-8f83-f0fb662aac84",
                                          "anyRoleName",
                                          RoleCategory.valueOf(roleCategory),
                                          existingAttributes,
                                          Classification.PRIVATE,
                                          GrantType.STANDARD,
                                          RoleType.ORGANISATION
                                      )));

        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            Assertions.assertEquals(Status.REJECTED, roleAssignment.getStatus());
        });
    }

    @ParameterizedTest
    @CsvSource({
        "challenged-access-judiciary,JUDICIAL,IAC_SPECIFIC_1_0",
        "challenged-access-admin,ADMIN,IAC_SPECIFIC_1_0",
        "challenged-access-legal-ops,LEGAL_OPERATIONS,IAC_SPECIFIC_1_0",
    })
    void shouldRejectAccessFor_ChallengedAccess_IncorrectFlagEnabled(String roleName,
                                                                     String roleCategory,
                                                                     String flag) {

        HashMap<String, JsonNode> roleAssignmentAttributes = new HashMap<>();
        roleAssignmentAttributes.put("caseId", convertValueJsonNode("1234567890123456"));
        roleAssignmentAttributes.put("requestedRole", convertValueJsonNode(roleName));
        roleAssignmentAttributes.put("caseType", convertValueJsonNode("notAsylum"));
        roleAssignmentAttributes.put("jurisdiction", convertValueJsonNode("IA"));

        assignmentRequest = TestDataBuilder.buildAssignmentRequestSpecialAccess(
            "challenged-access",
            roleName,
            RoleCategory.valueOf(roleCategory),
            roleAssignmentAttributes,
            Classification.PUBLIC,
            GrantType.CHALLENGED,
            Status.CREATE_REQUESTED,
            "anyClient",
            false,
            "Access required for reasons"
        )
            .build();

        FeatureFlag featureFlag = FeatureFlag.builder().flagName(FeatureFlagEnum.valueOf(flag).getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        HashMap<String, JsonNode> existingAttributes = new HashMap<>();
        existingAttributes.put("jurisdiction", convertValueJsonNode("IA"));
        existingAttributes.put("caseType", convertValueJsonNode("Asylum"));
        existingAttributes.put("substantive", convertValueJsonNode("Y"));
        executeDroolRules(List.of(TestDataBuilder
                                      .buildExistingRoleForDrools(
                                          "4772dc44-268f-4d0c-8f83-f0fb662aac84",
                                          "anyRoleName",
                                          RoleCategory.valueOf(roleCategory),
                                          existingAttributes,
                                          Classification.PRIVATE,
                                          GrantType.STANDARD,
                                          RoleType.ORGANISATION
                                      )));

        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            Assertions.assertEquals(Status.REJECTED, roleAssignment.getStatus());
        });
    }

    @ParameterizedTest
    @CsvSource({
        "challenged-access-judiciary,JUDICIAL",
        "challenged-access-admin,ADMIN",
        "challenged-access-legal-ops,LEGAL_OPERATIONS",
    })
    void shouldRejectAccessFor_ChallengedAccess_InsufficientNotes(String roleName, String roleCategory) {
        HashMap<String, JsonNode> roleAssignmentAttributes = new HashMap<>();
        roleAssignmentAttributes.put("caseId", convertValueJsonNode("1234567890123456"));
        roleAssignmentAttributes.put("requestedRole", convertValueJsonNode(roleName));
        roleAssignmentAttributes.put("caseType", convertValueJsonNode("notAsylum"));
        roleAssignmentAttributes.put("jurisdiction", convertValueJsonNode("IA"));

        assignmentRequest = TestDataBuilder.buildAssignmentRequestSpecialAccess(
            "challenged-access",
            roleName,
            RoleCategory.valueOf(roleCategory),
            roleAssignmentAttributes,
            Classification.PUBLIC,
            GrantType.CHALLENGED,
            Status.CREATE_REQUESTED,
            "anyClient",
            false,
            "A"
        )
            .build();

        FeatureFlag featureFlag = FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_CHALLENGED_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        HashMap<String, JsonNode> existingAttributes = new HashMap<>();
        existingAttributes.put("jurisdiction", convertValueJsonNode("IA"));
        existingAttributes.put("caseType", convertValueJsonNode("Asylum"));
        existingAttributes.put("substantive", convertValueJsonNode("Y"));
        executeDroolRules(List.of(TestDataBuilder
                                      .buildExistingRoleForDrools(
                                          "4772dc44-268f-4d0c-8f83-f0fb662aac84",
                                          "anyRoleName",
                                          RoleCategory.valueOf(roleCategory),
                                          existingAttributes,
                                          Classification.PRIVATE,
                                          GrantType.STANDARD,
                                          RoleType.ORGANISATION
                                      )));

        assignmentRequest.getRequestedRoles()
            .forEach(roleAssignment -> Assertions.assertEquals(Status.REJECTED, roleAssignment.getStatus()));
    }
}
