package uk.gov.hmcts.reform.roleassignment.domain.service.drools;

import com.fasterxml.jackson.databind.JsonNode;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.gov.hmcts.reform.roleassignment.domain.model.FeatureFlag;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.FeatureFlagEnum;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Classification.PUBLIC;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Classification.RESTRICTED;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType.SPECIFIC;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.DELETE_REQUESTED;
import static uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder.CASE_ALLOCATOR_ID;
import static uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder.buildExistingRole;
import static uk.gov.hmcts.reform.roleassignment.util.JacksonUtils.convertValueJsonNode;

class CaseRolesDroolsTest extends DroolBase {


    @ParameterizedTest
    @CsvSource({
        "SSCS,Benefit,hearing-judge,JUDICIAL,judge,Y",
        "SSCS,Benefit,hearing-judge,JUDICIAL,fee-paid-judge,Y",
        "SSCS,Benefit,tribunal-member-1,JUDICIAL,medical,Y",
        "SSCS,Benefit,tribunal-member-1,JUDICIAL,fee-paid-medical,Y",
        "SSCS,Benefit,tribunal-member-2,JUDICIAL,fee-paid-disability,Y",
        "SSCS,Benefit,appraiser-1,JUDICIAL,judge,Y",
        "SSCS,Benefit,appraiser-2,JUDICIAL,medical,Y",
        "SSCS,Benefit,appraiser-2,JUDICIAL,fee-paid-medical,Y",
        "SSCS,Benefit,interloc-judge,JUDICIAL,judge,Y",
        "SSCS,Benefit,case-allocator,JUDICIAL,case-allocator,N",
        "SSCS,Benefit,case-allocator,LEGAL_OPERATIONS,case-allocator,N",
        "SSCS,Benefit,registrar,LEGAL_OPERATIONS,registrar,N",
        "SSCS,Benefit,tribunal-caseworker,LEGAL_OPERATIONS,tribunal-caseworker,N",
        "PRIVATELAW,PRLAPPS,hearing-judge,JUDICIAL,judge,",
        "PRIVATELAW,PRLAPPS,allocated-magistrate,JUDICIAL,magistrate,",
        "PUBLICLAW,CARE_SUPERVISION_EPO,hearing-judge,JUDICIAL,judge,",
        "PUBLICLAW,CARE_SUPERVISION_EPO,allocated-magistrate,JUDICIAL,magistrate,",
        "PUBLICLAW,CARE_SUPERVISION_EPO,allocated-judge,JUDICIAL,judge,",
        "PUBLICLAW,CARE_SUPERVISION_EPO,allocated-legal-adviser,LEGAL_OPERATIONS,tribunal-caseworker,",
    })
    void shouldGrantAccessFor_CaseRole(String jurisdiction, String caseType, String roleName, String roleCategory,
                                       String existingRoleName, String expectedSubstantive) {

        HashMap<String, JsonNode> roleAssignmentAttributes = new HashMap<>();
        roleAssignmentAttributes.put("caseId", convertValueJsonNode(caseMap.get(jurisdiction).getId()));
        roleAssignmentAttributes.put("requestedRole", convertValueJsonNode(roleName));
        roleAssignmentAttributes.put("caseType", convertValueJsonNode(caseType));
        roleAssignmentAttributes.put("jurisdiction", convertValueJsonNode(jurisdiction));

        assignmentRequest = TestDataBuilder.buildAssignmentRequestSpecialAccessGrant(
            "sscs-access",
            roleName,
            RoleCategory.valueOf(roleCategory),
            RoleType.CASE,
            roleAssignmentAttributes,
            RESTRICTED,
            SPECIFIC,
            Status.CREATE_REQUESTED,
            "am_org_role_mapping_service",
            false,
            "Access required for reasons",
            CASE_ALLOCATOR_ID,
            "reference"
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
                                          RoleType.ORGANISATION
                                      )
                                )
                          );

        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(jurisdiction, roleAssignment.getAttributes().get("jurisdiction").asText());
            assertEquals(caseType, roleAssignment.getAttributes().get("caseType").asText());
            assertEquals(roleName, roleAssignment.getRoleName());
            assertEquals(RoleCategory.valueOf(roleCategory), roleAssignment.getRoleCategory());
            if (expectedSubstantive != null) {
                assertEquals(expectedSubstantive, roleAssignment.getAttributes().get("substantive").asText());
            }
            assertEquals(Status.APPROVED, roleAssignment.getStatus());
        });
    }

    @ParameterizedTest
    @CsvSource({
        "SSCS,Benefit,hearing-judge",
        "SSCS,Benefit,panel-doctor",
        "SSCS,Benefit,panel-disability",
        "SSCS,Benefit,panel-financial",
        "SSCS,Benefit,panel-appraisal-judge",
        "SSCS,Benefit,panel-appraisal-medical",
        "SSCS,Benefit,interloc-judge",
        "SSCS,Benefit,case-allocator",
        "SSCS,Benefit,registrar",
        "SSCS,Benefit,tribunal-caseworker",
        "PRIVATELAW,PRLAPPS,hearing-judge",
        "PRIVATELAW,PRLAPPS,allocated-magistrate"
    })
    void shouldDelete_CaseRole(String jurisdiction, String caseType, String roleName) {

        HashMap<String, JsonNode> existingAttributes = new HashMap<>();
        existingAttributes.put("jurisdiction", convertValueJsonNode(jurisdiction));
        existingAttributes.put("caseType", convertValueJsonNode(caseType));
        existingAttributes.put("caseId", convertValueJsonNode(caseMap.get(jurisdiction).getId()));

        assignmentRequest = TestDataBuilder.buildAssignmentRequestSpecialAccessGrant(
            "delete-access",
            roleName,
            RoleCategory.valueOf(RoleCategory.JUDICIAL.name()),
            RoleType.CASE,
            existingAttributes,
            PUBLIC,
            SPECIFIC,
            DELETE_REQUESTED,
            "am_org_role_mapping_service",
            false,
            "Delete required for reasons",
            CASE_ALLOCATOR_ID,
            "reference"
        )
            .build();

        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.SSCS_WA_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        existingAttributes.put("allocatedRole", convertValueJsonNode(roleName));

        executeDroolRules(List.of(buildExistingRole(CASE_ALLOCATOR_ID,
                                                    "case-allocator",
                                                    RoleCategory.JUDICIAL,
                                                    existingAttributes,
                                                    RoleType.CASE,
                                                    PUBLIC,
                                                    GrantType.STANDARD,
                                                    Status.LIVE
        )));

        assignmentRequest.getRequestedRoles().forEach(ra -> assertEquals(Status.DELETE_APPROVED, ra.getStatus()));
    }

    @ParameterizedTest
    @CsvSource({
        "judge,JUDICIAL,judge,SSCS,Benefit,ORGANISATION",
        "hearing-judge,ADMIN,fee-paid-judge,SSCS,Benefit,ORGANISATION",
        "panel-doctor,JUDICIAL,caseworker,SSCS,Benefit,ORGANISATION",
        "panel-appraisal-medical,JUDICIAL,fee-paid-medical,IA,Benefit,ORGANISATION",
        "interloc-judge,JUDICIAL,judge,SSCS,Asylum,ORGANISATION",
        "panel-appraisal-judge,JUDICIAL,judge,SSCS,Benefit,CASE",
    })
    void shouldRejectAccessFor_SSCS_CaseRole(String roleName, String roleCategory, String existingRoleName,
                                             String jurisdiction, String caseType, String roleType) {

        HashMap<String, JsonNode> roleAssignmentAttributes = new HashMap<>();
        roleAssignmentAttributes.put("caseId", convertValueJsonNode("1212121212121212"));
        roleAssignmentAttributes.put("requestedRole", convertValueJsonNode(roleName));
        roleAssignmentAttributes.put("caseType", convertValueJsonNode(caseType));
        roleAssignmentAttributes.put("jurisdiction", convertValueJsonNode(jurisdiction));

        assignmentRequest = TestDataBuilder.buildAssignmentRequestSpecialAccessGrant(
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
            CASE_ALLOCATOR_ID,
            "reference"
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

        assignmentRequest.getRequestedRoles().forEach(roleAssignment ->
                                                          assertEquals(Status.REJECTED, roleAssignment.getStatus()));
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

        assignmentRequest = TestDataBuilder.buildAssignmentRequestSpecialAccessGrant(
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
            CASE_ALLOCATOR_ID,
            "reference"
        )
            .build();

        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.SSCS_WA_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        executeDroolRules(List.of(buildExistingRole(CASE_ALLOCATOR_ID,
                                                    roleName,
                                                    RoleCategory.JUDICIAL,
                                                    existingAttributes,
                                                    RoleType.CASE,
                                                    PUBLIC,
                                                    GrantType.STANDARD,
                                                    Status.LIVE)));

        assignmentRequest.getRequestedRoles().forEach(ra -> assertEquals(Status.DELETE_REJECTED, ra.getStatus()));
    }
}
