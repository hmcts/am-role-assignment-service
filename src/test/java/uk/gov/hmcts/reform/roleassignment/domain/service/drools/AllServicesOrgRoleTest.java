package uk.gov.hmcts.reform.roleassignment.domain.service.drools;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Classification;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType.SPECIFIC;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType.STANDARD;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.APPROVED;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.CREATE_REQUESTED;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.DELETE_REQUESTED;
import static uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder.ACTORID;
import static uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder.getRequestedOrgRole;
import static uk.gov.hmcts.reform.roleassignment.util.JacksonUtils.convertValueJsonNode;

@RunWith(MockitoJUnitRunner.class)
class AllServicesOrgRoleTest extends DroolBase {

    @Test
    void shouldApproveOrgRequestedRoleForTCW_S001() {
        assignmentRequest.getRequest().setClientId("am_org_role_mapping_service");
        assignmentRequest.setRequestedRoles(getRequestedOrgRole());
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.LEGAL_OPERATIONS);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName("tribunal-caseworker");
            roleAssignment.setGrantType(STANDARD);
            roleAssignment.setStatus(CREATE_REQUESTED);
            roleAssignment.getAttributes().put("jurisdiction", convertValueJsonNode("IA"));
            roleAssignment.getAttributes().put("primaryLocation", convertValueJsonNode("abc"));
        });

        buildExecuteKieSession();

        //assertion
        assertFalse(assignmentRequest.getRequest().isByPassOrgDroolRule());
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            assertEquals(APPROVED, roleAssignment.getStatus());
            assertEquals("tribunal-caseworker", roleAssignment.getRoleName());
            String substantive = roleAssignment.getRoleType() == RoleType.ORGANISATION ? "Y" : "N";
            assertEquals(substantive, roleAssignment.getAttributes().get("substantive").asText());
        });
    }

    @Test
    void shouldApproveOrgRequestedRoleForSTCW_S002() {
        assignmentRequest.getRequest().setClientId("am_org_role_mapping_service");
        assignmentRequest.setRequestedRoles(getRequestedOrgRole());
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.LEGAL_OPERATIONS);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName("senior-tribunal-caseworker");
            roleAssignment.setGrantType(STANDARD);
            roleAssignment.setStatus(CREATE_REQUESTED);

            roleAssignment.getAttributes().put("jurisdiction", convertValueJsonNode("IA"));
            roleAssignment.getAttributes().put("primaryLocation", convertValueJsonNode("abc"));
        });

        buildExecuteKieSession();

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            assertEquals(APPROVED, roleAssignment.getStatus());
            assertEquals("senior-tribunal-caseworker", roleAssignment.getRoleName());
            assertEquals("Y", roleAssignment.getAttributes().get("substantive").asText());
        });
    }

    //@Test
    void shouldRejectOrgRequestedRoleForTCW_WrongClientID_S003() {

        assignmentRequest.getRequest().setClientId("ccd-gw");
        assignmentRequest.setRequestedRoles(getRequestedOrgRole());
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.LEGAL_OPERATIONS);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName("tribunal-caseworker");
            roleAssignment.setGrantType(STANDARD);
            roleAssignment.getAttributes().put("jurisdiction", convertValueJsonNode("IA"));
            roleAssignment.getAttributes().put("primaryLocation", convertValueJsonNode("abc"));
        });
        buildExecuteKieSession();

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            assertNotEquals(Status.APPROVED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldRejectOrgValidationForTCW_WrongRoleCategory_S004() {
        assignmentRequest.getRequest().setClientId("am_org_role_mapping_service");
        assignmentRequest.setRequestedRoles(getRequestedOrgRole());
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.JUDICIAL);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName("tribunal-caseworker");
            roleAssignment.setGrantType(STANDARD);
            roleAssignment.setStatus(CREATE_REQUESTED);
            roleAssignment.getAttributes().put("jurisdiction", convertValueJsonNode("IA"));
            roleAssignment.getAttributes().put("primaryLocation", convertValueJsonNode("abc"));
        });
        buildExecuteKieSession();

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            assertEquals(RoleCategory.JUDICIAL, roleAssignment.getRoleCategory());
            assertEquals("tribunal-caseworker", roleAssignment.getRoleName());
            assertEquals(Status.REJECTED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldRejectOrgValidationForTCW_WrongGrantType_S005() {
        assignmentRequest.getRequest().setClientId("am_org_role_mapping_service");
        assignmentRequest.setRequestedRoles(getRequestedOrgRole());
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.LEGAL_OPERATIONS);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName("tribunal-caseworker");
            roleAssignment.setGrantType(SPECIFIC);
            roleAssignment.getAttributes().put("jurisdiction", convertValueJsonNode("IA"));
            roleAssignment.getAttributes().put("primaryLocation", convertValueJsonNode("abc"));
        });
        buildExecuteKieSession();

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            assertEquals(SPECIFIC, roleAssignment.getGrantType());
            assertEquals(Status.REJECTED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldRejectOrgValidationForTCW_WrongClassification_S006() {
        assignmentRequest.getRequest().setClientId("am_org_role_mapping_service");
        assignmentRequest.setRequestedRoles(getRequestedOrgRole());
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.LEGAL_OPERATIONS);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setClassification(Classification.RESTRICTED);
            roleAssignment.setRoleName("tribunal-caseworker");
            roleAssignment.setGrantType(STANDARD);
            roleAssignment.setStatus(CREATE_REQUESTED);
            roleAssignment.getAttributes().put("jurisdiction", convertValueJsonNode("IA"));
            roleAssignment.getAttributes().put("primaryLocation", convertValueJsonNode("abc"));
        });
        buildExecuteKieSession();

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            assertEquals(Classification.RESTRICTED, roleAssignment.getClassification());
            assertEquals(Status.REJECTED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldRejectOrgValidationForSTCW_MissingJurisdiction_S007() {
        assignmentRequest.getRequest().setClientId("am_org_role_mapping_service");
        assignmentRequest.setRequestedRoles(getRequestedOrgRole());
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.LEGAL_OPERATIONS);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName("senior-tribunal-caseworker");
            roleAssignment.setGrantType(STANDARD);
            roleAssignment.setStatus(CREATE_REQUESTED);
            roleAssignment.getAttributes().put("primaryLocation", convertValueJsonNode("abc"));
        });
        buildExecuteKieSession();

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            assertNull(roleAssignment.getAttributes().get("jurisdiction"));
            assertEquals(Status.REJECTED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldRejectOrgValidationForTCW_WrongJurisdiction_S008() {
        assignmentRequest.getRequest().setClientId("am_org_role_mapping_service");
        assignmentRequest.setRequestedRoles(getRequestedOrgRole());
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.LEGAL_OPERATIONS);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName("tribunal-caseworker");
            roleAssignment.setGrantType(STANDARD);
            roleAssignment.setStatus(CREATE_REQUESTED);
            roleAssignment.getAttributes().put("primaryLocation", convertValueJsonNode("abc"));
        });
        buildExecuteKieSession();

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            assertEquals(Status.REJECTED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldRejectOrgValidationForTCW_MissingPrimaryLocation_S009() {
        assignmentRequest.getRequest().setClientId("am_org_role_mapping_service");
        assignmentRequest.setRequestedRoles(getRequestedOrgRole());
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.LEGAL_OPERATIONS);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName("tribunal-caseworker");
            roleAssignment.setGrantType(STANDARD);
            roleAssignment.setStatus(CREATE_REQUESTED);
            roleAssignment.getAttributes().put("jurisdiction", convertValueJsonNode("IA"));
        });
        buildExecuteKieSession();

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            assertNull(roleAssignment.getAttributes().get("primaryLocation"));
            assertEquals(Status.REJECTED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldApproveDeleteRequestedRoleForOrg_S010() {
        assignmentRequest.getRequest().setClientId("am_org_role_mapping_service");
        assignmentRequest.setRequestedRoles(getRequestedOrgRole());
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setStatus(Status.DELETE_REQUESTED);
            roleAssignment.setRoleCategory(RoleCategory.LEGAL_OPERATIONS);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
        });
        buildExecuteKieSession();

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            assertEquals(Status.DELETE_APPROVED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldApproveDeleteRequestedRoleWithBadClientIdAndBypassDroolRule() {
        assignmentRequest.getRequest().setClientId("not_am_org_role_mapping_service");
        assignmentRequest.getRequest().setByPassOrgDroolRule(true);
        assignmentRequest.setRequestedRoles(getRequestedOrgRole());
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setStatus(Status.DELETE_REQUESTED);
            roleAssignment.setRoleCategory(RoleCategory.LEGAL_OPERATIONS);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
        });
        buildExecuteKieSession();

        //assertion
        assertTrue(assignmentRequest.getRequest().isByPassOrgDroolRule());
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            assertEquals(Status.DELETE_APPROVED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldApproveDeleteRequestedRoleWithGoodClientIdAndNoBypassDroolRule() {
        assignmentRequest.getRequest().setClientId("am_org_role_mapping_service");
        assignmentRequest.getRequest().setByPassOrgDroolRule(false);
        assignmentRequest.setRequestedRoles(getRequestedOrgRole());
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setStatus(Status.DELETE_REQUESTED);
            roleAssignment.setRoleCategory(RoleCategory.LEGAL_OPERATIONS);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
        });
        buildExecuteKieSession();

        //assertion
        assertFalse(assignmentRequest.getRequest().isByPassOrgDroolRule());
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            assertEquals(Status.DELETE_APPROVED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldRejectDeleteRequestedRoleBadClientIdAndNoBypassDroolRule() {
        assignmentRequest.getRequest().setClientId("not_am_org_role_mapping_service");
        assignmentRequest.getRequest().setByPassOrgDroolRule(false);
        assignmentRequest.setRequestedRoles(getRequestedOrgRole());
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setStatus(DELETE_REQUESTED);
            roleAssignment.setRoleCategory(RoleCategory.LEGAL_OPERATIONS);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
        });
        buildExecuteKieSession();

        //assertion
        assertFalse(assignmentRequest.getRequest().isByPassOrgDroolRule());
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            assertEquals(Status.DELETE_REJECTED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldRejectDeleteRequestedRole_MissingRoleType_S012() {
        assignmentRequest.setRequestedRoles(getRequestedOrgRole());
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setStatus(Status.DELETE_REQUESTED);
            roleAssignment.setRoleCategory(RoleCategory.LEGAL_OPERATIONS);
        });
        buildExecuteKieSession();

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            assertNull(roleAssignment.getRoleType());
            assertEquals(Status.DELETE_REJECTED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldRejectDeleteRequestedRoleForOrgWrongClientId_S011() {

        assignmentRequest.getRequest().setClientId("ccd-gw");
        assignmentRequest.setRequestedRoles(getRequestedOrgRole());
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setStatus(DELETE_REQUESTED);
            roleAssignment.setRoleCategory(RoleCategory.LEGAL_OPERATIONS);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
        });
        buildExecuteKieSession();

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            assertEquals(Status.DELETE_REJECTED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldApproveOrgRequestedRoleForSTCW_withoutSubstantive() {
        assignmentRequest.getRequest().setClientId("am_org_role_mapping_service");
        assignmentRequest.setRequestedRoles(getRequestedOrgRole());
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.LEGAL_OPERATIONS);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName("task-supervisor");
            roleAssignment.setGrantType(STANDARD);
            roleAssignment.setStatus(CREATE_REQUESTED);

            roleAssignment.getAttributes().put("jurisdiction", convertValueJsonNode("IA"));
            roleAssignment.getAttributes().put("primaryLocation", convertValueJsonNode("abc"));
        });

        buildExecuteKieSession();

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            assertEquals(APPROVED, roleAssignment.getStatus());
            assertEquals("task-supervisor", roleAssignment.getRoleName());
            assertEquals("N", roleAssignment.getAttributes().get("substantive").asText());
        });
    }

    @Test
    void shouldApprovedRequestedRoleForOrg() {

        assignmentRequest.setRequestedRoles(getRequestedOrgRole());
        assignmentRequest.getRequest().setClientId("not_am_org_role_mapping_service");
        assignmentRequest.getRequest().setByPassOrgDroolRule(true);
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.JUDICIAL);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setStatus(Status.CREATE_REQUESTED);
            roleAssignment.setRoleName("judge");
            roleAssignment.setGrantType(STANDARD);
            roleAssignment.getAttributes().put("region", convertValueJsonNode("north-east"));
            roleAssignment.getAttributes().put("jurisdiction", convertValueJsonNode("IA"));
        });

        //Execute Kie session
        buildExecuteKieSession();

        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
                assertEquals(Status.APPROVED,roleAssignment.getStatus());
                assertEquals("Y", roleAssignment.getAttributes().get("substantive").asText());
            }
        );
    }

    @Test
    void shouldRejectOrgValidation_MissingAttributeJurisdiction() {

        assignmentRequest.setRequestedRoles(getRequestedOrgRole());

        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.JUDICIAL);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName("judge");
            roleAssignment.setGrantType(STANDARD);
            roleAssignment.setStatus(Status.CREATE_REQUESTED);
            roleAssignment.getAttributes().put("region", convertValueJsonNode("north-east"));
        });

        //Execute Kie session
        buildExecuteKieSession();

        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment ->
                                                                   assertEquals(
                                                                       Status.REJECTED,
                                                                       roleAssignment.getStatus()
                                                                   )
        );
    }

    @ParameterizedTest
    @CsvSource({
        "hearing-manager,LEGAL_OPERATIONS,STANDARD,north-east,SSCS,UK,ORGANISATION,N,Null,PUBLIC",
        "hearing-manager,ADMIN,STANDARD,north-east,SSCS,UK,ORGANISATION,N,Null,PUBLIC",
        "hearing-viewer,JUDICIAL,STANDARD,north-east,SSCS,UK,ORGANISATION,N,Null,PUBLIC",
        "hearing-viewer,LEGAL_OPERATIONS,STANDARD,north-east,SSCS,UK,ORGANISATION,N,Null,PUBLIC",
        "hearing-viewer,ADMIN,STANDARD,north-east,SSCS,UK,ORGANISATION,N,Null,PUBLIC",
        "listed-hearing-viewer,OTHER_GOV_DEPT,STANDARD,north-east,SSCS,UK,ORGANISATION,N,Null,PUBLIC",
        "registrar,LEGAL_OPERATIONS,STANDARD,south-east,SSCS,London,ORGANISATION,N,Null,PUBLIC",
        "superuser,ADMIN,STANDARD,north-east,SSCS,UK,ORGANISATION,Y,Null,PUBLIC",
        "clerk,ADMIN,STANDARD,south-east,SSCS,UK,ORGANISATION,Y,Null,PUBLIC",
        "dwp,OTHER_GOV_DEPT,STANDARD,south-east,SSCS,UK,ORGANISATION,Y,Null,PUBLIC",
        "hmrc,OTHER_GOV_DEPT,STANDARD,south-east,SSCS,UK,ORGANISATION,Y,Null,PUBLIC",
        "case-allocator,JUDICIAL,STANDARD,north-east,SSCS,UK,ORGANISATION,N,Null,PUBLIC",
        "case-allocator,LEGAL_OPERATIONS,STANDARD,north-east,SSCS,UK,ORGANISATION,N,Null,PUBLIC",
        "case-allocator,ADMIN,STANDARD,north-east,SSCS,UK,ORGANISATION,N,Null,PUBLIC",
        "task-supervisor,JUDICIAL,STANDARD,north-east,SSCS,UK,ORGANISATION,N,Null,PUBLIC",
        "task-supervisor,LEGAL_OPERATIONS,STANDARD,north-east,SSCS,UK,ORGANISATION,N,Null,PUBLIC",
        "task-supervisor,ADMIN,STANDARD,north-east,SSCS,UK,ORGANISATION,N,Null,PUBLIC",
        "hmcts-judiciary,JUDICIAL,BASIC,north-east,SSCS,UK,ORGANISATION,N,SALARIED,PRIVATE",
        "hmcts-legal-operations,LEGAL_OPERATIONS,BASIC,north-east,SSCS,UK,ORGANISATION,N,SALARIED,PRIVATE",
        "hmcts-admin,ADMIN,BASIC,north-east,SSCS,UK,ORGANISATION,N,SALARIED,PRIVATE",
        "judge,JUDICIAL,STANDARD,north-east,SSCS,UK,ORGANISATION,Y,Null,PUBLIC",
        "fee-paid-judge,JUDICIAL,STANDARD,north-east,SSCS,UK,ORGANISATION,Y,Null,PUBLIC",
        "tribunal-caseworker,LEGAL_OPERATIONS,STANDARD,north-east,SSCS,UK,ORGANISATION,Y,Null,PUBLIC",
        "hmcts-judiciary,JUDICIAL,BASIC,north-east,CIVIL,UK,ORGANISATION,N,SALARIED,PRIVATE",
        "judge,JUDICIAL,STANDARD,north-east,CIVIL,UK,ORGANISATION,Y,Salaried,PUBLIC",
        "fee-paid-judge,JUDICIAL,STANDARD,north-east,CIVIL,UK,ORGANISATION,Y,Fee-Paid,PUBLIC",
        "circuit-judge,JUDICIAL,STANDARD,north-east,CIVIL,UK,ORGANISATION,Y,Salaried,PUBLIC",
        "leadership-judge,JUDICIAL,STANDARD,north-east,CIVIL,UK,ORGANISATION,Y,Salaried,PUBLIC"
    })
    void shouldApproveRequestedRoleForOrg(String roleName, String roleCategory, String grantType,
                                          String region, String jurisdiction, String primaryLocation,
                                          String roleType, String expectedSubstantive, String contractType,
                                          String classification) {

        assignmentRequest.setRequestedRoles(getRequestedOrgRole());
        assignmentRequest.getRequest().setClientId("am_org_role_mapping_service");
        assignmentRequest.getRequest().setAssignerId(ACTORID);
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.valueOf(roleCategory));
            roleAssignment.setRoleType(RoleType.valueOf(roleType));
            roleAssignment.setStatus(Status.CREATE_REQUESTED);
            roleAssignment.setClassification(Classification.valueOf(classification));
            roleAssignment.setRoleName(roleName);
            roleAssignment.setGrantType(GrantType.valueOf(grantType));
            roleAssignment.getAttributes().put("region", convertValueJsonNode(region));
            roleAssignment.getAttributes().put("primaryLocation", convertValueJsonNode(primaryLocation));
            roleAssignment.getAttributes().put("jurisdiction", convertValueJsonNode(jurisdiction));
            roleAssignment.getAttributes().put("contractType", convertValueJsonNode(contractType));
        });

        //Execute Kie session
        buildExecuteKieSession();

        assignmentRequest.getRequestedRoles()
            .forEach(roleAssignment -> {
                assertEquals(jurisdiction, roleAssignment.getAttributes().get("jurisdiction").asText());
                assertEquals(roleName, roleAssignment.getRoleName());
                assertEquals(RoleCategory.valueOf(roleCategory), roleAssignment.getRoleCategory());
                assertEquals(expectedSubstantive, roleAssignment.getAttributes().get("substantive").asText());
                assertEquals(Status.APPROVED, roleAssignment.getStatus());
            });
    }

    @ParameterizedTest
    @CsvSource({
        "manager,LEGAL_OPERATIONS,STANDARD,north-east,SSCS,ORGANISATION",
        "hearing-manager,JUDICIAL,STANDARD,north-east,SSCS,ORGANISATION",
        "hearing-viewer,JUDICIAL,SPECIFIC,north-east,SSCS,ORGANISATION",
        "hearing-viewer,LEGAL_OPERATIONS,STANDARD,north-east,IA,ORGANISATION",
        "listed-hearing-viewer,OTHER_GOV_DEPT,STANDARD,north-east,SSCS,CASE",
        "judge,JUDICIAL,BASIC,north-east,SSCS,ORGANISATION",
        "fee-paid-judge,JUDICIAL,STANDARD,north-east,CIVIL1,ORGANISATION",
        "circuit-judge,LEGAL_OPERATIONS,STANDARD,north-east,CIVIL,ORGANISATION",
        "leadership-judge,JUDICIAL,STANDARD,north-east,SSCS,ORGANISATION"
    })
    void shouldRejectRequestedRoleForOrg(String roleName, String roleCategory,
                                         String grantType, String region, String jurisdiction,
                                         String org) {

        assignmentRequest.setRequestedRoles(getRequestedOrgRole());
        assignmentRequest.getRequest().setClientId("am_org_role_mapping_service");
        assignmentRequest.getRequest().setAssignerId(ACTORID);
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.valueOf(roleCategory));
            roleAssignment.setRoleType(RoleType.valueOf(org));
            roleAssignment.setStatus(Status.CREATE_REQUESTED);
            roleAssignment.setClassification(Classification.PUBLIC);
            roleAssignment.setRoleName(roleName);
            roleAssignment.setGrantType(GrantType.valueOf(grantType));
            roleAssignment.getAttributes().put("region", convertValueJsonNode(region));
            roleAssignment.getAttributes().put("jurisdiction", convertValueJsonNode(jurisdiction));
            roleAssignment.getAttributes().put("substantive", convertValueJsonNode("N"));
        });

        //Execute Kie session
        buildExecuteKieSession();

        assignmentRequest.getRequestedRoles()
            .forEach(roleAssignment -> assertEquals(Status.REJECTED, roleAssignment.getStatus()));
    }
}
