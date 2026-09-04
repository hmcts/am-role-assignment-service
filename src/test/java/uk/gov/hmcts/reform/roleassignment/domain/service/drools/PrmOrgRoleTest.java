package uk.gov.hmcts.reform.roleassignment.domain.service.drools;

import org.junit.jupiter.api.extension.ExtendWith;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.roleassignment.domain.model.FeatureFlag;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.FeatureFlagEnum;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Classification.RESTRICTED;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType.STANDARD;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.CREATE_REQUESTED;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.DELETE_APPROVED;
import static uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder.getRequestedOrgRole;
import static uk.gov.hmcts.reform.roleassignment.util.JacksonUtils.convertValueJsonNode;

@ExtendWith(MockitoExtension.class)
class PrmOrgRoleTest extends DroolBase {

    @ParameterizedTest
    @CsvSource({
        // NB: GA only role combinations from: `./src/main/resources/roleconfig/role_prm.json`
        //     i.e. those with mandatory caseAccessGroupId
        // :: PCS
        "claimant,PCS,TestCaseType,PCS:all-cases:111:11111",
        "claimant-solicitor,PCS,TestCaseType,PCS:all-cases:111:11111",
        "defendant-solicitor,PCS,TestCaseType,PCS:all-cases:222:22222",
        // :: PUBLICLAW : CARE_SUPERVISION_EPO
        "solicitor-respa,PUBLICLAW,CARE_SUPERVISION_EPO,care_supervision_epo:all-case:solicitor-respa:101",
        "solicitor-respb,PUBLICLAW,CARE_SUPERVISION_EPO,care_supervision_epo:all-case:solicitor-respb:102",
        "solicitor-respc,PUBLICLAW,CARE_SUPERVISION_EPO,care_supervision_epo:all-case:solicitor-respc:103",
        "solicitor-respd,PUBLICLAW,CARE_SUPERVISION_EPO,care_supervision_epo:all-case:solicitor-respd:104",
        "solicitor-respe,PUBLICLAW,CARE_SUPERVISION_EPO,care_supervision_epo:all-case:solicitor-respe:105",
        "solicitor-respf,PUBLICLAW,CARE_SUPERVISION_EPO,care_supervision_epo:all-case:solicitor-respf:106",
        "solicitor-respg,PUBLICLAW,CARE_SUPERVISION_EPO,care_supervision_epo:all-case:solicitor-respg:107",
        "solicitor-resph,PUBLICLAW,CARE_SUPERVISION_EPO,care_supervision_epo:all-case:solicitor-resph:108",
        "solicitor-respi,PUBLICLAW,CARE_SUPERVISION_EPO,care_supervision_epo:all-case:solicitor-respi:109",
        "solicitor-respj,PUBLICLAW,CARE_SUPERVISION_EPO,care_supervision_epo:all-case:solicitor-respj:110",
        "solicitor-childa,PUBLICLAW,CARE_SUPERVISION_EPO,care_supervision_epo:all-case:solicitor-childa:201",
        "solicitor-childb,PUBLICLAW,CARE_SUPERVISION_EPO,care_supervision_epo:all-case:solicitor-childb:202",
        "solicitor-childc,PUBLICLAW,CARE_SUPERVISION_EPO,care_supervision_epo:all-case:solicitor-childc:203",
        "solicitor-childd,PUBLICLAW,CARE_SUPERVISION_EPO,care_supervision_epo:all-case:solicitor-childd:204",
        "solicitor-childe,PUBLICLAW,CARE_SUPERVISION_EPO,care_supervision_epo:all-case:solicitor-childe:205",
        "solicitor-childf,PUBLICLAW,CARE_SUPERVISION_EPO,care_supervision_epo:all-case:solicitor-childf:206",
        "solicitor-childg,PUBLICLAW,CARE_SUPERVISION_EPO,care_supervision_epo:all-case:solicitor-childg:207",
        "solicitor-childh,PUBLICLAW,CARE_SUPERVISION_EPO,care_supervision_epo:all-case:solicitor-childh:208",
        "solicitor-childi,PUBLICLAW,CARE_SUPERVISION_EPO,care_supervision_epo:all-case:solicitor-childi:209",
        "solicitor-childj,PUBLICLAW,CARE_SUPERVISION_EPO,care_supervision_epo:all-case:solicitor-childj:210",
        "solicitor-childk,PUBLICLAW,CARE_SUPERVISION_EPO,care_supervision_epo:all-case:solicitor-childk:211",
        "solicitor-childl,PUBLICLAW,CARE_SUPERVISION_EPO,care_supervision_epo:all-case:solicitor-childl:212",
        "solicitor-childm,PUBLICLAW,CARE_SUPERVISION_EPO,care_supervision_epo:all-case:solicitor-childm:213",
        "solicitor-childn,PUBLICLAW,CARE_SUPERVISION_EPO,care_supervision_epo:all-case:solicitor-childn:214",
        "solicitor-childo,PUBLICLAW,CARE_SUPERVISION_EPO,care_supervision_epo:all-case:solicitor-childo:215",
        "solicitor-epsm,PUBLICLAW,CARE_SUPERVISION_EPO,care_supervision_epo:all-case:solicitor-epsm:111",
        "la-primary,PUBLICLAW,CARE_SUPERVISION_EPO,care_supervision_epo:all-case:la-primary:222",
        "la-primary,PUBLICLAW,CARE_SUPERVISION_EPO,care_supervision_epo:all-case:la-secondary:333",
        "la-mla,PUBLICLAW,CARE_SUPERVISION_EPO,care_supervision_epo:all-case:la-mla:444",
    })
    void shouldApproveOrRejectProfessionalOrgGroupAccessRoleRequest(String roleName,
                                                                    String jurisdiction,
                                                                    String caseType,
                                                                    String caseAccessGroupId) {

        RoleCategory roleCategory = RoleCategory.PROFESSIONAL;

        // wrong category
        verifyProfessionalOrgGroupAccessRequestedRole(RoleCategory.CITIZEN, // WRONG
                                                      roleName,
                                                      jurisdiction,
                                                      caseType,
                                                      caseAccessGroupId,
                                                      Status.REJECTED);
        // wrong jurisdiction
        verifyProfessionalOrgGroupAccessRequestedRole(roleCategory,
                                                      roleName,
                                                      "wrong-jurisdiction", // WRONG
                                                      caseType,
                                                      caseAccessGroupId,
                                                      Status.REJECTED);
        // missing case-type
        verifyProfessionalOrgGroupAccessRequestedRole(roleCategory,
                                                      roleName,
                                                      jurisdiction,
                                                      null, // MISSING
                                                      caseAccessGroupId,
                                                      Status.REJECTED);
        // missing caseAccessGroupId
        verifyProfessionalOrgGroupAccessRequestedRole(roleCategory,
                                                      roleName,
                                                      jurisdiction,
                                                      caseType,
                                                      null, // MISSING (NB: mandatory for GA role)
                                                      Status.REJECTED);

        // correct values should be approved
        verifyProfessionalOrgGroupAccessRequestedRole(roleCategory,
                                                      roleName,
                                                      jurisdiction,
                                                      caseType,
                                                      caseAccessGroupId,
                                                      Status.APPROVED);
    }

    @ParameterizedTest
    @CsvSource({
        // NB: ORG role combinations from: `./src/main/resources/roleconfig/role_prm.json`
        //     i.e. those without mandatory caseAccessGroupId
        // :: PCS
        "duty-advisor-request,PCS,TestCaseType",
        // :: PUBLICLAW : CARE_SUPERVISION_EPO
        "solicitor-create,PUBLICLAW,CARE_SUPERVISION_EPO",
        "la-create,PUBLICLAW,CARE_SUPERVISION_EPO",
    })
    void shouldApproveOrRejectProfessionalOrgRoleRequest(String roleName,
                                                         String jurisdiction,
                                                         String caseType) {
        RoleCategory roleCategory = RoleCategory.PROFESSIONAL;

        // NB: ORG roles do not use caseAccessGroupId, so it should be null in these tests

        // wrong category
        verifyProfessionalOrgGroupAccessRequestedRole(RoleCategory.CITIZEN, // WRONG
                                                      roleName,
                                                      jurisdiction,
                                                      caseType,
                                                      null,
                                                      Status.REJECTED);
        // wrong jurisdiction
        verifyProfessionalOrgGroupAccessRequestedRole(roleCategory,
                                                      roleName,
                                                      "wrong-jurisdiction", // WRONG
                                                      caseType,
                                                      null,
                                                      Status.REJECTED);
        // missing case-type
        verifyProfessionalOrgGroupAccessRequestedRole(roleCategory,
                                                      roleName,
                                                      jurisdiction,
                                                      null, // MISSING
                                                      null,
                                                      Status.REJECTED);

        // correct values should be approved
        verifyProfessionalOrgGroupAccessRequestedRole(roleCategory,
                                                      roleName,
                                                      jurisdiction,
                                                      caseType,
                                                      null,
                                                      Status.APPROVED);
    }

    private void verifyProfessionalOrgGroupAccessRequestedRole(RoleCategory roleCategory,
                                                               String roleName,
                                                               String jurisdiction,
                                                               String caseType,
                                                               String caseAccessGroupId,
                                                               Status expectedStatus) {
        // GIVEN
        assignmentRequest.getRequest().setClientId("am_org_role_mapping_service");
        assignmentRequest.getRequest().setProcess("professional-organisational-role-mapping");
        assignmentRequest.getRequest().setReplaceExisting(true);
        assignmentRequest.setRequestedRoles(getRequestedOrgRole());
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(roleCategory);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName(roleName);
            roleAssignment.setGrantType(STANDARD);
            roleAssignment.setClassification(RESTRICTED);
            roleAssignment.setStatus(CREATE_REQUESTED);
            roleAssignment.getAttributes().put("jurisdiction", convertValueJsonNode(jurisdiction));
            if (StringUtils.isNotEmpty(caseType)) {
                roleAssignment.getAttributes().put("caseType", convertValueJsonNode(caseType));
            }
            if (StringUtils.isNotEmpty(caseAccessGroupId)) {
                roleAssignment.getAttributes().put("caseAccessGroupId", convertValueJsonNode(caseAccessGroupId));
            }
        });

        FeatureFlag featureFlag = FeatureFlag.builder()
            .flagName(FeatureFlagEnum.GA_PRM_1_0.getValue())
            .status(true)
            .build();
        featureFlags.add(featureFlag);

        // WHEN
        buildExecuteKieSession();

        // THEN
        assertFalse(assignmentRequest.getRequest().isByPassOrgDroolRule());
        assertTrue(assignmentRequest.getRequestedRoles().size() > 0, "No requested roles found");
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(expectedStatus, roleAssignment.getStatus());
            assertEquals(roleCategory, roleAssignment.getRoleCategory());
            assertEquals(RoleType.ORGANISATION, roleAssignment.getRoleType());
            assertEquals(roleName, roleAssignment.getRoleName());
            assertEquals(jurisdiction, roleAssignment.getAttributes().get("jurisdiction").asText());
            if (StringUtils.isNotEmpty(caseType)) {
                assertEquals(caseType, roleAssignment.getAttributes().get("caseType").asText());
            } else {
                assertFalse(roleAssignment.getAttributes().containsKey("caseType"));
            }
            if (StringUtils.isNotEmpty(caseAccessGroupId)) {
                assertEquals(caseAccessGroupId, roleAssignment.getAttributes().get("caseAccessGroupId").asText());
            } else {
                assertFalse(roleAssignment.getAttributes().containsKey("caseAccessGroupId"));
            }

            // If PROFESSIONAL role then these tests should always pass stage 1 processing
            assertEquals(
                roleCategory == RoleCategory.PROFESSIONAL,
                roleAssignment.getLog().contains("Create approved : prm_create_org_role"),
                "Role has not passed stage 1 of PRM role validation"
            );

            // however they should only pass validation of role_config pattern if expected status is APPROVED
            assertEquals(
                expectedStatus == Status.APPROVED,
                roleAssignment.getLog().contains("Approved : validate_role_assignment_against_patterns"),
                "Wrong outcome for role validation against role_config patterns"
            );
        });
    }

    @ParameterizedTest
    @CsvSource({
        // NB: All role combinations from: `./src/main/resources/roleconfig/role_prm.json`
        // :: PCS : GA roles
        "claimant,PCS",
        "claimant-solicitor,PCS",
        "defendant-solicitor,PCS",
        // :: PCS : ORG roles
        "duty-advisor-request,PCS",
        // :: PUBLICLAW : GA roles
        "solicitor-respa,PUBLICLAW",
        "solicitor-respb,PUBLICLAW",
        "solicitor-respc,PUBLICLAW",
        "solicitor-respd,PUBLICLAW",
        "solicitor-respe,PUBLICLAW",
        "solicitor-respf,PUBLICLAW",
        "solicitor-respg,PUBLICLAW",
        "solicitor-resph,PUBLICLAW",
        "solicitor-respi,PUBLICLAW",
        "solicitor-respj,PUBLICLAW",
        "solicitor-childa,PUBLICLAW",
        "solicitor-childb,PUBLICLAW",
        "solicitor-childc,PUBLICLAW",
        "solicitor-childd,PUBLICLAW",
        "solicitor-childe,PUBLICLAW",
        "solicitor-childf,PUBLICLAW",
        "solicitor-childg,PUBLICLAW",
        "solicitor-childh,PUBLICLAW",
        "solicitor-childi,PUBLICLAW",
        "solicitor-childj,PUBLICLAW",
        "solicitor-childk,PUBLICLAW",
        "solicitor-childl,PUBLICLAW",
        "solicitor-childm,PUBLICLAW",
        "solicitor-childn,PUBLICLAW",
        "solicitor-childo,PUBLICLAW",
        "solicitor-epsm,PUBLICLAW",
        "la-primary,PUBLICLAW",
        "la-secondary,PUBLICLAW",
        "la-mla,PUBLICLAW",
        // :: PUBLICLAW : ORG roles
        "solicitor-create,PUBLICLAW",
        "la-create,PUBLICLAW",
    })
    void shouldDeleteProfessionalOrgRole(String roleName, String jurisdiction) {

        // GIVEN
        assignmentRequest.getRequest().setClientId("am_org_role_mapping_service");
        assignmentRequest.setRequestedRoles(getRequestedOrgRole());
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.PROFESSIONAL);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName(roleName);
            roleAssignment.setGrantType(STANDARD);
            roleAssignment.setClassification(RESTRICTED);
            roleAssignment.setStatus(Status.DELETE_REQUESTED);
            roleAssignment.getAttributes().put("jurisdiction", convertValueJsonNode(jurisdiction));
        });

        // WHEN
        buildExecuteKieSession();

        // THEN
        assertFalse(assignmentRequest.getRequest().isByPassOrgDroolRule());
        assertTrue(assignmentRequest.getRequestedRoles().size() > 0, "No requested roles found");
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(DELETE_APPROVED, roleAssignment.getStatus());
            assertEquals(RoleCategory.PROFESSIONAL, roleAssignment.getRoleCategory());
            assertEquals(RoleType.ORGANISATION, roleAssignment.getRoleType());
            assertEquals(roleName, roleAssignment.getRoleName());
            assertEquals(jurisdiction, roleAssignment.getAttributes().get("jurisdiction").asText());

            assertTrue(
                roleAssignment.getLog().contains("Delete approved : prm_delete_org_role"),
                "Role has not passed stage 1 of PRM role deletion validation"
            );
        });
    }

}
