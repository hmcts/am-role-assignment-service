package uk.gov.hmcts.reform.roleassignment.domain.service.drools;

import com.fasterxml.jackson.databind.JsonNode;

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

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Classification.PUBLIC;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType.SPECIFIC;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.APPROVED;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.DELETE_REQUESTED;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.REJECTED;
import static uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder.CASE_ALLOCATOR_ID;
import static uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder.buildExistingRole;
import static uk.gov.hmcts.reform.roleassignment.util.JacksonUtils.convertValueJsonNode;

class CaseRolesDroolsTest extends DroolBase {


    @ParameterizedTest
    @CsvSource({
        "SSCS,Benefit,hearing-judge,JUDICIAL,RESTRICTED,judge,Y",
        "SSCS,Benefit,hearing-judge,JUDICIAL,RESTRICTED,fee-paid-judge,Y",
        "SSCS,Benefit,tribunal-member-1,JUDICIAL,RESTRICTED,medical,Y",
        "SSCS,Benefit,tribunal-member-1,JUDICIAL,RESTRICTED,fee-paid-medical,Y",
        "SSCS,Benefit,tribunal-member-1,JUDICIAL,RESTRICTED,fee-paid-disability,Y",
        "SSCS,Benefit,tribunal-member-1,JUDICIAL,RESTRICTED,fee-paid-financial,Y",
        "SSCS,Benefit,tribunal-member-1,JUDICIAL,RESTRICTED,fee-paid-tribunal-member,Y",
        "SSCS,Benefit,tribunal-member-2,JUDICIAL,RESTRICTED,medical,Y",
        "SSCS,Benefit,tribunal-member-2,JUDICIAL,RESTRICTED,fee-paid-medical,Y",
        "SSCS,Benefit,tribunal-member-2,JUDICIAL,RESTRICTED,fee-paid-disability,Y",
        "SSCS,Benefit,tribunal-member-2,JUDICIAL,RESTRICTED,fee-paid-financial,Y",
        "SSCS,Benefit,tribunal-member-2,JUDICIAL,RESTRICTED,fee-paid-tribunal-member,Y",
        "SSCS,Benefit,tribunal-member-3,JUDICIAL,RESTRICTED,medical,Y",
        "SSCS,Benefit,tribunal-member-3,JUDICIAL,RESTRICTED,fee-paid-medical,Y",
        "SSCS,Benefit,tribunal-member-3,JUDICIAL,RESTRICTED,fee-paid-disability,Y",
        "SSCS,Benefit,tribunal-member-3,JUDICIAL,RESTRICTED,fee-paid-financial,Y",
        "SSCS,Benefit,tribunal-member-3,JUDICIAL,RESTRICTED,fee-paid-tribunal-member,Y",
        "SSCS,Benefit,appraiser-1,JUDICIAL,RESTRICTED,judge,Y",
        "SSCS,Benefit,appraiser-1,JUDICIAL,RESTRICTED,fee-paid-judge,Y",
        "SSCS,Benefit,appraiser-1,JUDICIAL,RESTRICTED,fee-paid-medical,Y",
        "SSCS,Benefit,appraiser-1,JUDICIAL,RESTRICTED,medical,Y",
        "SSCS,Benefit,appraiser-1,JUDICIAL,RESTRICTED,fee-paid-disability,Y",
        "SSCS,Benefit,appraiser-1,JUDICIAL,RESTRICTED,fee-paid-financial,Y",
        "SSCS,Benefit,appraiser-2,JUDICIAL,RESTRICTED,medical,Y",
        "SSCS,Benefit,appraiser-2,JUDICIAL,RESTRICTED,fee-paid-medical,Y",
        "SSCS,Benefit,appraiser-2,JUDICIAL,RESTRICTED,judge,Y",
        "SSCS,Benefit,appraiser-2,JUDICIAL,RESTRICTED,fee-paid-judge,Y",
        "SSCS,Benefit,appraiser-2,JUDICIAL,RESTRICTED,fee-paid-disability,Y",
        "SSCS,Benefit,appraiser-2,JUDICIAL,RESTRICTED,fee-paid-financial,Y",
        "SSCS,Benefit,interloc-judge,JUDICIAL,RESTRICTED,judge,Y",
        "SSCS,Benefit,interloc-judge,JUDICIAL,RESTRICTED,fee-paid-judge,Y",
        "SSCS,Benefit,post-hearing-judge,JUDICIAL,RESTRICTED,judge,Y",
        "SSCS,Benefit,case-allocator,JUDICIAL,RESTRICTED,case-allocator,N",
        "SSCS,Benefit,case-allocator,LEGAL_OPERATIONS,RESTRICTED,case-allocator,N",
        "SSCS,Benefit,registrar,LEGAL_OPERATIONS,RESTRICTED,registrar,N",
        "SSCS,Benefit,tribunal-caseworker,LEGAL_OPERATIONS,RESTRICTED,tribunal-caseworker,N",
        "SSCS,Benefit,allocated-tribunal-caseworker,LEGAL_OPERATIONS,RESTRICTED,tribunal-caseworker,N",
        "SSCS,Benefit,allocated-admin-caseworker,ADMIN,RESTRICTED,hearing-centre-admin,N",
        "SSCS,Benefit,allocated-admin-caseworker,ADMIN,RESTRICTED,regional-centre-admin,N",
        "SSCS,Benefit,allocated-admin-caseworker,ADMIN,RESTRICTED,clerk,N",
        "SSCS,Benefit,allocated-ctsc-caseworker,CTSC,RESTRICTED,ctsc,N",
        "PRIVATELAW,PRLAPPS,hearing-judge,JUDICIAL,RESTRICTED,judge,",
        "PRIVATELAW,PRLAPPS,allocated-magistrate,JUDICIAL,RESTRICTED,magistrate,",
        "PUBLICLAW,CARE_SUPERVISION_EPO,hearing-judge,JUDICIAL,RESTRICTED,judge,",
        "PUBLICLAW,CARE_SUPERVISION_EPO,allocated-magistrate,JUDICIAL,RESTRICTED,magistrate,",
        "PUBLICLAW,CARE_SUPERVISION_EPO,allocated-judge,JUDICIAL,RESTRICTED,judge,",
        "PUBLICLAW,CARE_SUPERVISION_EPO,allocated-judge,JUDICIAL,RESTRICTED,fee-paid-judge,",
        "PUBLICLAW,CARE_SUPERVISION_EPO,allocated-legal-adviser,LEGAL_OPERATIONS,RESTRICTED,tribunal-caseworker,",
        "EMPLOYMENT,ET_EnglandWales,lead-judge,JUDICIAL,PUBLIC,leadership-judge,Y",
        "EMPLOYMENT,ET_EnglandWales,lead-judge,JUDICIAL,PUBLIC,judge,Y",
        "EMPLOYMENT,ET_EnglandWales,hearing-judge,JUDICIAL,PUBLIC,leadership-judge,Y",
        "EMPLOYMENT,ET_EnglandWales,hearing-judge,JUDICIAL,PUBLIC,judge,Y",
        "EMPLOYMENT,ET_EnglandWales,tribunal-member-1,JUDICIAL,PUBLIC,tribunal-member,Y",
        "EMPLOYMENT,ET_EnglandWales,tribunal-member-2,JUDICIAL,PUBLIC,tribunal-member,Y",
        "EMPLOYMENT,ET_EnglandWales,allocated-tribunal-caseworker,LEGAL_OPERATIONS,PUBLIC,tribunal-caseworker,N",
        "EMPLOYMENT,ET_EnglandWales,allocated-tribunal-caseworker,LEGAL_OPERATIONS,PUBLIC,senior-tribunal-caseworker,N",
        "EMPLOYMENT,ET_EnglandWales,allocated-admin-caseworker,ADMIN,PUBLIC,hearing-centre-admin,N",
        "EMPLOYMENT,ET_EnglandWales,allocated-admin-caseworker,ADMIN,PUBLIC,hearing-centre-team-leader,N",
        "EMPLOYMENT,ET_EnglandWales,allocated-admin-caseworker,ADMIN,PUBLIC,regional-centre-admin,N",
        "EMPLOYMENT,ET_EnglandWales,allocated-admin-caseworker,ADMIN,PUBLIC,regional-centre-team-leader,N",
        "EMPLOYMENT,ET_EnglandWales,allocated-admin-caseworker,ADMIN,PUBLIC,clerk,N",
        "EMPLOYMENT,ET_EnglandWales,allocated-ctsc-caseworker,CTSC,PUBLIC,ctsc,N",
        "EMPLOYMENT,ET_EnglandWales,allocated-ctsc-caseworker,CTSC,PUBLIC,ctsc-team-leader,N",
        "EMPLOYMENT,ET_Scotland,lead-judge,JUDICIAL,PUBLIC,leadership-judge,Y",
        "EMPLOYMENT,ET_Scotland,lead-judge,JUDICIAL,PUBLIC,judge,Y",
        "EMPLOYMENT,ET_Scotland,hearing-judge,JUDICIAL,PUBLIC,leadership-judge,Y",
        "EMPLOYMENT,ET_Scotland,hearing-judge,JUDICIAL,PUBLIC,judge,Y",
        "EMPLOYMENT,ET_Scotland,tribunal-member-1,JUDICIAL,PUBLIC,tribunal-member,Y",
        "EMPLOYMENT,ET_Scotland,tribunal-member-2,JUDICIAL,PUBLIC,tribunal-member,Y",
        "EMPLOYMENT,ET_Scotland,allocated-tribunal-caseworker,LEGAL_OPERATIONS,PUBLIC,tribunal-caseworker,N",
        "EMPLOYMENT,ET_Scotland,allocated-tribunal-caseworker,LEGAL_OPERATIONS,PUBLIC,senior-tribunal-caseworker,N",
        "EMPLOYMENT,ET_Scotland,allocated-admin-caseworker,ADMIN,PUBLIC,hearing-centre-admin,N",
        "EMPLOYMENT,ET_Scotland,allocated-admin-caseworker,ADMIN,PUBLIC,hearing-centre-team-leader,N",
        "EMPLOYMENT,ET_Scotland,allocated-admin-caseworker,ADMIN,PUBLIC,regional-centre-admin,N",
        "EMPLOYMENT,ET_Scotland,allocated-admin-caseworker,ADMIN,PUBLIC,regional-centre-team-leader,N",
        "EMPLOYMENT,ET_Scotland,allocated-admin-caseworker,ADMIN,PUBLIC,clerk,N",
        "EMPLOYMENT,ET_Scotland,allocated-ctsc-caseworker,CTSC,PUBLIC,ctsc,N",
        "EMPLOYMENT,ET_Scotland,allocated-ctsc-caseworker,CTSC,PUBLIC,ctsc-team-leader,N",
    })
    void shouldGrantAccessFor_CaseRole(String jurisdiction, String caseType, String roleName,
                                       String roleCategory, String classification,
                                       String existingRoleName, String expectedSubstantive) {

        verifyGrantOrRejectAccessFor_CaseRole(
            jurisdiction,
            caseType,
            roleName,
            roleCategory,
            classification,
            existingRoleName,
            expectedSubstantive,
            APPROVED
        );
    }


    @ParameterizedTest
    @CsvSource({
        "EMPLOYMENT,ET_EnglandWales,lead-judge,JUDICIAL,PUBLIC",
        "EMPLOYMENT,ET_EnglandWales,hearing-judge,JUDICIAL,PUBLIC",
        "EMPLOYMENT,ET_EnglandWales,tribunal-member-1,JUDICIAL,PUBLIC",
        "EMPLOYMENT,ET_EnglandWales,tribunal-member-2,JUDICIAL,PUBLIC",
        "EMPLOYMENT,ET_EnglandWales,allocated-tribunal-caseworker,LEGAL_OPERATIONS,PUBLIC",
        "EMPLOYMENT,ET_EnglandWales,allocated-admin-caseworker,ADMIN,PUBLIC",
        "EMPLOYMENT,ET_EnglandWales,allocated-ctsc-caseworker,CTSC,PUBLIC",
        "EMPLOYMENT,ET_Scotland,lead-judge,JUDICIAL,PUBLIC",
        "EMPLOYMENT,ET_Scotland,hearing-judge,JUDICIAL,PUBLIC",
        "EMPLOYMENT,ET_Scotland,tribunal-member-1,JUDICIAL,PUBLIC",
        "EMPLOYMENT,ET_EnglandWales,tribunal-member-2,JUDICIAL,PUBLIC",
        "EMPLOYMENT,ET_Scotland,allocated-tribunal-caseworker,LEGAL_OPERATIONS,PUBLIC",
        "EMPLOYMENT,ET_Scotland,allocated-admin-caseworker,ADMIN,PUBLIC",
        "EMPLOYMENT,ET_Scotland,allocated-ctsc-caseworker,CTSC,PUBLIC",
    })
    void shouldRejectAccessFor_CaseRole_BadExistingRole(String jurisdiction, String caseType, String roleName,
                                                        String roleCategory, String classification) {
        verifyGrantOrRejectAccessFor_CaseRole(jurisdiction,
                                              caseType,
                                              roleName,
                                              roleCategory,
                                              classification,
                                              "bad-role-name",
                                              null,
                                              REJECTED);
    }

    private void verifyGrantOrRejectAccessFor_CaseRole(String jurisdiction, String caseType, String roleName,
                                                       String roleCategory, String classification,
                                                       String existingRoleName, String expectedSubstantive,
                                                       Status expectedRoleAssignmentStatus) {

        HashMap<String, JsonNode> roleAssignmentAttributes = new HashMap<>();
        roleAssignmentAttributes.put("caseId", convertValueJsonNode(getCaseFromMap(jurisdiction, caseType).getId()));
        roleAssignmentAttributes.put("requestedRole", convertValueJsonNode(roleName));
        roleAssignmentAttributes.put("caseType", convertValueJsonNode(caseType));
        roleAssignmentAttributes.put("jurisdiction", convertValueJsonNode(jurisdiction));

        assignmentRequest = TestDataBuilder.buildAssignmentRequestSpecialAccessGrant(
            "sscs-access",
            roleName,
            RoleCategory.valueOf(roleCategory),
            RoleType.CASE,
            roleAssignmentAttributes,
            Classification.valueOf(classification),
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

        if (expectedRoleAssignmentStatus == APPROVED) {
            assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
                assertEquals(jurisdiction, roleAssignment.getAttributes().get("jurisdiction").asText());
                assertEquals(caseType, roleAssignment.getAttributes().get("caseType").asText());
                assertEquals(roleName, roleAssignment.getRoleName());
                assertEquals(RoleCategory.valueOf(roleCategory), roleAssignment.getRoleCategory());
                assertEquals(Classification.valueOf(classification), roleAssignment.getClassification());
                if (expectedSubstantive != null) {
                    assertEquals(expectedSubstantive, roleAssignment.getAttributes().get("substantive").asText());
                }
                assertEquals(Status.APPROVED, roleAssignment.getStatus());
            });
        } else {
            assignmentRequest.getRequestedRoles().forEach(ra -> assertEquals(Status.REJECTED, ra.getStatus()));
        }
    }

    @ParameterizedTest
    @CsvSource({
        "SSCS,Benefit,hearing-judge",
        "SSCS,Benefit,tribunal-member-1",
        "SSCS,Benefit,tribunal-member-2",
        "SSCS,Benefit,tribunal-member-3",
        "SSCS,Benefit,appraiser-1",
        "SSCS,Benefit,appraiser-2",
        "SSCS,Benefit,panel-doctor",
        "SSCS,Benefit,panel-disability",
        "SSCS,Benefit,panel-financial",
        "SSCS,Benefit,panel-appraisal-judge",
        "SSCS,Benefit,panel-appraisal-medical",
        "SSCS,Benefit,interloc-judge",
        "SSCS,Benefit,post-hearing-judge",
        "SSCS,Benefit,case-allocator",
        "SSCS,Benefit,registrar",
        "SSCS,Benefit,allocated-tribunal-caseworker",
        "SSCS,Benefit,allocated-admin-caseworker",
        "SSCS,Benefit,allocated-ctsc-caseworker",
        "SSCS,Benefit,tribunal-caseworker",
        "PRIVATELAW,PRLAPPS,hearing-judge",
        "PRIVATELAW,PRLAPPS,allocated-magistrate",
        "EMPLOYMENT,ET_EnglandWales,lead-judge",
        "EMPLOYMENT,ET_EnglandWales,hearing-judge",
        "EMPLOYMENT,ET_EnglandWales,tribunal-member-1",
        "EMPLOYMENT,ET_EnglandWales,tribunal-member-2",
        "EMPLOYMENT,ET_EnglandWales,allocated-tribunal-caseworker",
        "EMPLOYMENT,ET_EnglandWales,allocated-admin-caseworker",
        "EMPLOYMENT,ET_EnglandWales,allocated-ctsc-caseworker",
        "EMPLOYMENT,ET_Scotland,lead-judge",
        "EMPLOYMENT,ET_Scotland,hearing-judge",
        "EMPLOYMENT,ET_Scotland,tribunal-member-1",
        "EMPLOYMENT,ET_Scotland,tribunal-member-2",
        "EMPLOYMENT,ET_Scotland,allocated-tribunal-caseworker",
        "EMPLOYMENT,ET_Scotland,allocated-admin-caseworker",
        "EMPLOYMENT,ET_Scotland,allocated-ctsc-caseworker",
    })
    void shouldDelete_CaseRole(String jurisdiction, String caseType, String roleName) {

        HashMap<String, JsonNode> existingAttributes = new HashMap<>();
        existingAttributes.put("jurisdiction", convertValueJsonNode(jurisdiction));
        existingAttributes.put("caseType", convertValueJsonNode(caseType));
        existingAttributes.put("caseId", convertValueJsonNode(getCaseFromMap(jurisdiction, caseType).getId()));

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
