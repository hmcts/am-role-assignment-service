package uk.gov.hmcts.reform.roleassignment.domain.service.drools;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Classification;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType.SPECIFIC;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType.STANDARD;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.APPROVED;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.CREATE_REQUESTED;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.DELETE_REQUESTED;
import static uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder.getRequestedOrgRole;
import static uk.gov.hmcts.reform.roleassignment.util.JacksonUtils.convertValueJsonNode;

@RunWith(MockitoJUnitRunner.class)
class StaffCategoryOrgRoleTest extends DroolBase {

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
            assertEquals("Y", roleAssignment.getAttributes().get("substantive").asText());
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
            assertEquals(null, roleAssignment.getAttributes().get("jurisdiction"));
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
            assertEquals(null, roleAssignment.getAttributes().get("primaryLocation"));
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
            assertEquals(null, roleAssignment.getRoleType());
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
}
