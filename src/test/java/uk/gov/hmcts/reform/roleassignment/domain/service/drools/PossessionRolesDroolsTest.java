package uk.gov.hmcts.reform.roleassignment.domain.service.drools;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.gov.hmcts.reform.roleassignment.domain.model.FeatureFlag;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Classification;
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
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.APPROVED;
import static uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder.CASE_ALLOCATOR_ID;
import static uk.gov.hmcts.reform.roleassignment.util.JacksonUtils.convertValueJsonNode;

class PossessionRolesDroolsTest extends DroolBase {

    @ParameterizedTest
    @CsvSource({
        "PCS,GENERALAPPLICATION,allocated-judge,JUDICIAL,RESTRICTED,judge,Y",
        "PCS,GENERALAPPLICATION,allocated-judge,JUDICIAL,RESTRICTED,fee-paid-judge,Y",
        "PCS,GENERALAPPLICATION,hearing-judge,JUDICIAL,PUBLIC,judge,",
        "PCS,GENERALAPPLICATION,hearing-judge,JUDICIAL,PUBLIC,fee-paid-judge,",
        "PCS,GENERALAPPLICATION,hearing-legal-adviser,LEGAL_OPERATIONS,RESTRICTED,tribunal-caseworker,Y",
        "PCS,GENERALAPPLICATION,allocated-ctsc-caseworker,CTSC,RESTRICTED,ctsc,",
        "PCS,GENERALAPPLICATION,allocated-ctsc-caseworker,CTSC,RESTRICTED,ctsc-team-leader,",
        "PCS,GENERALAPPLICATION,allocated-admin-caseworker,ADMIN,RESTRICTED,hearing-centre-admin,",
        "PCS,GENERALAPPLICATION,allocated-admin-caseworker,ADMIN,RESTRICTED,hearing-centre-team-leader,",
        "PCS,GENERALAPPLICATION,allocated-wlu-caseworker,ADMIN,RESTRICTED,wlu-admin,Y",
        "PCS,GENERALAPPLICATION,allocated-wlu-caseworker,ADMIN,RESTRICTED,wlu-team-leader,Y"
    })
    void shouldGrantAccessFor_OrgRole(String jurisdiction, String caseType, String roleName,
                                       String roleCategory, String classification,
                                       String existingRoleName, String expectedSubstantive) {

        verifyGrantOrRejectAccessFor_CaseRole(
            jurisdiction,
            caseType,
            roleName,
            roleCategory,
            classification,
            RoleType.ORGANISATION,
            existingRoleName,
            expectedSubstantive,
            APPROVED
        );
    }

    private void verifyGrantOrRejectAccessFor_CaseRole(String jurisdiction, String caseType, String roleName,
                                                       String roleCategory, String classification,
                                                       RoleType roleType,
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
            roleType,
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

        setFeatureFlags();

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
                                          SPECIFIC,
                                          RoleType.ORGANISATION
                                      ),
                                  TestDataBuilder
                                      .buildExistingRoleForDrools(
                                          TestDataBuilder.CASE_ALLOCATOR_ID,
                                          existingRoleName,
                                          RoleCategory.valueOf(roleCategory),
                                          existingAttributes,
                                          RESTRICTED,
                                          SPECIFIC,
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

    private void setFeatureFlags() {
        List<String> flags = List.of("possessions_wa_1_0");

        for (String flag : flags) {
            featureFlags.add(
                FeatureFlag.builder().flagName(flag).status(true).build()
            );
        }
    }
}
