package uk.gov.hmcts.reform.roleassignment.domain.service.drools;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.roleassignment.domain.model.FeatureFlag;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.FeatureFlagEnum;
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

        HashMap<String, JsonNode> attr = new HashMap<>();
        attr.put("caseId", convertValueJsonNode("1234567890123456"));
        attr.put("requestedRole", convertValueJsonNode("specific-access-judiciary"));
        attr.put("caseType", convertValueJsonNode("NotIA"));
        attr.put("jurisdiction", convertValueJsonNode("NotAsylum"));
        assignmentRequest = TestDataBuilder.buildAssignmentRequestSpecificAccess(
            "specific-access", "specific-access-requested", RoleCategory.JUDICIAL, attr).build();

        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_SPECIFIC_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        executeDroolRules(List.of(TestDataBuilder
                                      .buildExistingRoleForIAC("4772dc44-268f-4d0c-8f83-f0fb662aac84",
                                                               "case-allocator",
                                                               RoleCategory.JUDICIAL)));

        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            Assertions.assertEquals(Status.APPROVED, roleAssignment.getStatus());
            Assertions.assertEquals("IA", roleAssignment.getAttributes().get("jurisdiction").asText());
            Assertions.assertEquals("Asylum", roleAssignment.getAttributes().get("caseType").asText());
        });
    }

    @Test
    void shouldGrantAccessFor_SpecificAccessLegalOps() {

        HashMap<String, JsonNode> attr = new HashMap<>();
        attr.put("jurisdiction", convertValueJsonNode("anyJurisdiction"));
        attr.put("caseType", convertValueJsonNode("anyCase"));
        attr.put("caseId",convertValueJsonNode("9234567890123456"));
        attr.put("requestedRole", convertValueJsonNode("specific-access-legal-operations"));

        assignmentRequest = TestDataBuilder.buildAssignmentRequestSpecificAccess("specific-access","specific-access-requested",
                                                                                 RoleCategory.LEGAL_OPERATIONS, attr).build();

        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_SPECIFIC_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        executeDroolRules(List.of(TestDataBuilder.buildExistingRoleForIAC("4772dc44-268f-4d0c-8f83-f0fb662aac84", "case-allocator",
                                                               RoleCategory.LEGAL_OPERATIONS)));

        assignmentRequest.getRequestedRoles().forEach(e -> {
            Assertions.assertEquals(Status.APPROVED, e.getStatus());
            Assertions.assertEquals("IA",e.getAttributes().get("jurisdiction").asText());
            Assertions.assertEquals("Asylum",e.getAttributes().get("caseType").asText());
        });

    }

    @Test
    void shouldGrantAccessFor_SpecificAccessAdmin() {
        HashMap<String, JsonNode> attribute = new HashMap<>();
        attribute.put("jurisdiction", convertValueJsonNode("anyJurisdiction"));
        attribute.put("caseType",convertValueJsonNode("anyCase"));
        attribute.put("caseId",convertValueJsonNode("1234567890123456"));
        attribute.put("requestedRole", convertValueJsonNode("specific-access-admin"));

        assignmentRequest = TestDataBuilder.buildAssignmentRequestSpecificAccess("specific-access","specific-access-requested",
                                                                                 RoleCategory.ADMIN,attribute).build();

        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_SPECIFIC_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        executeDroolRules(List.of(TestDataBuilder.buildExistingRoleForIAC("4772dc44-268f-4d0c-8f83-f0fb662aac84","case-allocator",
                                                                          RoleCategory.ADMIN)));

        assignmentRequest.getRequestedRoles().forEach(e -> {
            Assertions.assertEquals(Status.APPROVED,e.getStatus());
            Assertions.assertEquals("IA",e.getAttributes().get("jurisdiction").asText());
            Assertions.assertEquals("Asylum",e.getAttributes().get("caseType").asText());
        });

    }

}
