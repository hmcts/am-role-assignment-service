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
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;

import static uk.gov.hmcts.reform.roleassignment.util.JacksonUtils.convertValueJsonNode;

import java.util.HashMap;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
class SpecificAccessDroolsTest extends DroolBase {

    @Test
    void shouldGrantAccessFor_SpecificAccessJudiciary() {

        HashMap<String, JsonNode> roleAssignmentAttributes = new HashMap<>();
        roleAssignmentAttributes.put("caseId", convertValueJsonNode("1234567890123456"));
        roleAssignmentAttributes.put("requestedRole", convertValueJsonNode("specific-access-judiciary"));
        roleAssignmentAttributes.put("caseType", convertValueJsonNode("NotIA"));
        roleAssignmentAttributes.put("jurisdiction", convertValueJsonNode("NotAsylum"));

        assignmentRequest = TestDataBuilder.buildAssignmentRequestSpecificAccess("specific-access",
                                                                                 "specific-access-requested",
                                                                                 RoleCategory.JUDICIAL,
                                                                                 roleAssignmentAttributes,
                                                                                 Classification.PRIVATE,
                                                                                 GrantType.BASIC).build();

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
                                                                  GrantType.STANDARD)));

        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            Assertions.assertEquals(Status.APPROVED, roleAssignment.getStatus());
            Assertions.assertEquals("IA", roleAssignment.getAttributes().get("jurisdiction").asText());
            Assertions.assertEquals("Asylum", roleAssignment.getAttributes().get("caseType").asText());
        });
    }

    @Test
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
                                                                                 GrantType.BASIC).build();

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
                                                                  GrantType.STANDARD)));

        assignmentRequest.getRequestedRoles().forEach(e -> {
            Assertions.assertEquals(Status.APPROVED, e.getStatus());
            Assertions.assertEquals("IA",e.getAttributes().get("jurisdiction").asText());
            Assertions.assertEquals("Asylum",e.getAttributes().get("caseType").asText());
        });

    }

    @Test
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
                                                                                 GrantType.BASIC).build();

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
                                                                  GrantType.STANDARD)));

        assignmentRequest.getRequestedRoles().forEach(e -> {
            Assertions.assertEquals(Status.APPROVED,e.getStatus());
            Assertions.assertEquals("IA",e.getAttributes().get("jurisdiction").asText());
            Assertions.assertEquals("Asylum",e.getAttributes().get("caseType").asText());
        });

    }

}
