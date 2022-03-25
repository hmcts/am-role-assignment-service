package uk.gov.hmcts.reform.roleassignment.domain.service.drools;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.gov.hmcts.reform.roleassignment.domain.model.FeatureFlag;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.FeatureFlagEnum;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Classification.PUBLIC;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType.SPECIFIC;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.DELETE_REQUESTED;
import static uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder.CASE_ALLOCATOR_ID;
import static uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder.buildExistingRoleForSSCS;
import static uk.gov.hmcts.reform.roleassignment.util.JacksonUtils.convertValueJsonNode;

import java.util.HashMap;
import java.util.List;

class SscsCaseRolesDroolsTest extends DroolBase {


    @ParameterizedTest
    @CsvSource({
        "hearing-judge,JUDICIAL,caseworker-sscs-judge",
        "hearing-judge,JUDICIAL,caseworker-sscs-judge-feepaid",
        "panel-doctor,JUDICIAL,caseworker-sscs-medical",
        "panel-doctor,JUDICIAL,caseworker-sscs-medical-feepaid",
        "panel-disability,JUDICIAL,caseworker-sscs-disability-feepaid",
        "panel-financial,JUDICIAL,caseworker-sscs-financial-feepaid",
        "panel-appraisal-judge,JUDICIAL,caseworker-sscs-judge",
        "panel-appraisal-medical,JUDICIAL,caseworker-sscs-medical",
        "panel-appraisal-medical,JUDICIAL,caseworker-sscs-medical-feepaid",
        "interloc-judge,JUDICIAL,caseworker-sscs-judge",
        "case-allocator,JUDICIAL,case-allocator"

    })
    void shouldGrantAccessFor_SSCS_CaseRole(String roleName, String roleCategory, String existingRoleName) {

        HashMap<String, JsonNode> roleAssignmentAttributes = new HashMap<>();
        roleAssignmentAttributes.put("caseId", convertValueJsonNode("1212121212121212"));
        roleAssignmentAttributes.put("requestedRole", convertValueJsonNode(roleName));
        roleAssignmentAttributes.put("caseType", convertValueJsonNode("Benefit"));
        roleAssignmentAttributes.put("jurisdiction", convertValueJsonNode("SSCS"));

        assignmentRequest = TestDataBuilder.buildAssignmentRequestSpecialAccessApprover(
            "sscs-access",
            roleName,
            RoleCategory.valueOf(roleCategory),
            RoleType.CASE,
            roleAssignmentAttributes,
            PUBLIC,
            SPECIFIC,
            Status.CREATE_REQUESTED,
            "am_org_role_mapping_service",
            false,
            "Access required for reasons",
            CASE_ALLOCATOR_ID
        )
            .build();

        FeatureFlag featureFlag = FeatureFlag.builder().flagName(FeatureFlagEnum.SSCS_WA_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        HashMap<String, JsonNode> existingAttributes = new HashMap<>();
        existingAttributes.put("jurisdiction", convertValueJsonNode("SSCS"));
        existingAttributes.put("caseType", convertValueJsonNode("Benefit"));

        executeDroolRules(List.of(TestDataBuilder
                                      .buildExistingRoleForDrools(
                                          TestDataBuilder.CASE_ALLOCATOR_ID,
                                          "case-allocator",
                                          RoleCategory.valueOf(roleCategory),
                                          existingAttributes,
                                          PUBLIC,
                                          GrantType.STANDARD,
                                          RoleType.ORGANISATION
                                      ),
                                  TestDataBuilder
                                      .buildExistingRoleForDrools(
                                          TestDataBuilder.CASE_ALLOCATOR_ID,
                                          existingRoleName,
                                          RoleCategory.valueOf(roleCategory),
                                          existingAttributes,
                                          PUBLIC,
                                          GrantType.STANDARD,
                                          RoleType.ORGANISATION
                                      )
                                )
                          );

        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            Assertions.assertEquals(Status.APPROVED, roleAssignment.getStatus());
        });
    }

    @ParameterizedTest
    @CsvSource({
        "hearing-judge",
        "panel-doctor",
        "panel-disability",
        "panel-financial",
        "panel-appraisal-judge",
        "panel-appraisal-medical",
        "interloc-judge",
        "case-allocator"
    })
    void shouldDelete_SSCS_CaseRole(String roleName) {

        HashMap<String, JsonNode> existingAttributes = new HashMap<>();
        existingAttributes.put("jurisdiction", convertValueJsonNode("SSCS"));
        existingAttributes.put("caseType", convertValueJsonNode("Benefit"));
        existingAttributes.put("caseId", convertValueJsonNode("1212121212121212"));
        existingAttributes.put("requestedRole", convertValueJsonNode(roleName));

        assignmentRequest = TestDataBuilder.buildAssignmentRequestSpecialAccessApprover(
            "delete-access",
            roleName,
            RoleCategory.valueOf(RoleCategory.JUDICIAL.name()),
            RoleType.ORGANISATION,
            existingAttributes,
            PUBLIC,
            SPECIFIC,
            DELETE_REQUESTED,
            "am_org_role_mapping_service",
            false,
            "Delete required for reasons",
            CASE_ALLOCATOR_ID
        )
            .build();

        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.SSCS_WA_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        executeDroolRules(List.of(buildExistingRoleForSSCS(CASE_ALLOCATOR_ID,
                                                           roleName,
                                                           RoleCategory.JUDICIAL,
                                                           existingAttributes,
                                                           RoleType.CASE)));

        assignmentRequest.getRequestedRoles().forEach(ra -> assertEquals(Status.DELETE_APPROVED, ra.getStatus()));
    }

    @ParameterizedTest
    @CsvSource({
        "judge,JUDICIAL,caseworker-sscs-judge,SSCS,Benefit,ORGANISATION",
        "hearing-judge,ADMIN,caseworker-sscs-judge-feepaid,SSCS,Benefit,ORGANISATION",
        "panel-doctor,JUDICIAL,caseworker,SSCS,Benefit,ORGANISATION",
        "panel-appraisal-medical,JUDICIAL,caseworker-sscs-medical-feepaid,IA,Benefit,ORGANISATION",
        "interloc-judge,JUDICIAL,caseworker-sscs-judge,SSCS,Asylum,ORGANISATION",
        "panel-appraisal-judge,JUDICIAL,caseworker-sscs-judge,SSCS,Benefit,CASE",
    })
    void shouldRejectAccessFor_SSCS_CaseRole(String roleName, String roleCategory, String existingRoleName,
                                             String jurisdiction, String caseType, String roleType) {

        HashMap<String, JsonNode> roleAssignmentAttributes = new HashMap<>();
        roleAssignmentAttributes.put("caseId", convertValueJsonNode("1212121212121212"));
        roleAssignmentAttributes.put("requestedRole", convertValueJsonNode(roleName));
        roleAssignmentAttributes.put("caseType", convertValueJsonNode(caseType));
        roleAssignmentAttributes.put("jurisdiction", convertValueJsonNode(jurisdiction));

        assignmentRequest = TestDataBuilder.buildAssignmentRequestSpecialAccessApprover(
            "sscs-access",
            roleName,
            RoleCategory.valueOf(roleCategory),
            RoleType.CASE,
            roleAssignmentAttributes,
            PUBLIC,
            SPECIFIC,
            Status.CREATE_REQUESTED,
            "am_org_role_mapping_service",
            false,
            "Access required for reasons",
            CASE_ALLOCATOR_ID
        )
            .build();

        FeatureFlag featureFlag = FeatureFlag.builder().flagName(FeatureFlagEnum.SSCS_WA_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        HashMap<String, JsonNode> existingAttributes = new HashMap<>();
        existingAttributes.put("jurisdiction", convertValueJsonNode(jurisdiction));
        existingAttributes.put("caseType", convertValueJsonNode(caseType));

        executeDroolRules(List.of(TestDataBuilder
                                      .buildExistingRoleForDrools(
                                          TestDataBuilder.CASE_ALLOCATOR_ID,
                                          "case-allocator",
                                          RoleCategory.valueOf(roleCategory),
                                          existingAttributes,
                                          PUBLIC,
                                          GrantType.STANDARD,
                                          RoleType.ORGANISATION
                                      ),
                                  TestDataBuilder
                                      .buildExistingRoleForDrools(
                                          TestDataBuilder.CASE_ALLOCATOR_ID,
                                          existingRoleName,
                                          RoleCategory.valueOf(roleCategory),
                                          existingAttributes,
                                          PUBLIC,
                                          GrantType.STANDARD,
                                          RoleType.valueOf(roleType)
                                      )
                          )
        );

        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            Assertions.assertEquals(Status.REJECTED, roleAssignment.getStatus());
        });
    }

    @ParameterizedTest
    @CsvSource({
        "panel-appraisal-judge,CITIZEN,am_org_role_mapping_service,ORGANISATION",
        "interloc-judge,JUDICIAL,am_role_assignment_service,ORGANISATION",
        "hearing-judge,JUDICIAL,am_org_role_mapping_service,CASE"
    })
    void shouldRejectDeleteRequest_SSCS_CaseRole(String roleName,
                                                 String roleCategory,
                                                 String clientId,
                                                 String roleType) {

        HashMap<String, JsonNode> existingAttributes = new HashMap<>();
        existingAttributes.put("jurisdiction", convertValueJsonNode("SSCS"));
        existingAttributes.put("caseType", convertValueJsonNode("Benefit"));
        existingAttributes.put("caseId", convertValueJsonNode("1212121212121212"));
        existingAttributes.put("requestedRole", convertValueJsonNode(roleName));

        assignmentRequest = TestDataBuilder.buildAssignmentRequestSpecialAccessApprover(
            "delete-access",
            roleName,
            RoleCategory.valueOf(roleCategory),
            RoleType.valueOf(roleType),
            existingAttributes,
            PUBLIC,
            SPECIFIC,
            DELETE_REQUESTED,
            clientId,
            false,
            "Delete required for reasons",
            CASE_ALLOCATOR_ID
        )
            .build();

        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.SSCS_WA_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        executeDroolRules(List.of(buildExistingRoleForSSCS(CASE_ALLOCATOR_ID,
                                                           roleName,
                                                           RoleCategory.JUDICIAL,
                                                           existingAttributes,
                                                           RoleType.CASE)));

        assignmentRequest.getRequestedRoles().forEach(ra -> assertEquals(Status.DELETE_REJECTED, ra.getStatus()));
    }
}
