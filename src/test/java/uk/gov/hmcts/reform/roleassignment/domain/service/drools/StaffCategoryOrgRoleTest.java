package uk.gov.hmcts.reform.roleassignment.domain.service.drools;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Classification;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType.SPECIFIC;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType.STANDARD;
import static uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder.getRequestedOrgRole;
import static uk.gov.hmcts.reform.roleassignment.util.JacksonUtils.convertValueJsonNode;

@RunWith(MockitoJUnitRunner.class)
class StaffCategoryOrgRoleTest extends DroolBase {

    @Test
    void shouldApproveOrgRequestedRoleForTCW_S001() {
        //clientId check not implemented yet
        //assignmentRequest.getRequest().setClientId("am_org_role_mapping_service");
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
            assertEquals(Status.APPROVED, roleAssignment.getStatus());
            assertEquals("tribunal-caseworker", roleAssignment.getRoleName());
        });
    }

    @Test
    void shouldApproveOrgRequestedRoleForSTCW_S002() {
        //clientId check not implemented yet
        //assignmentRequest.getRequest().setClientId("am_org_role_mapping_service");
        assignmentRequest.setRequestedRoles(getRequestedOrgRole());
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.LEGAL_OPERATIONS);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName("senior-tribunal-caseworker");
            roleAssignment.setGrantType(STANDARD);
            roleAssignment.getAttributes().put("jurisdiction", convertValueJsonNode("IA"));
            roleAssignment.getAttributes().put("primaryLocation", convertValueJsonNode("abc"));
        });

        buildExecuteKieSession();

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            assertEquals(Status.APPROVED, roleAssignment.getStatus());
            assertEquals("senior-tribunal-caseworker", roleAssignment.getRoleName());
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
        //clientId check not implemented yet
        //assignmentRequest.getRequest().setClientId("am_org_role_mapping_service");
        assignmentRequest.setRequestedRoles(getRequestedOrgRole());
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.JUDICIAL);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName("tribunal-caseworker");
            roleAssignment.setGrantType(STANDARD);
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
        //clientId check not implemented yet
        //assignmentRequest.getRequest().setClientId("am_org_role_mapping_service");
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
        //clientId check not implemented yet
        //assignmentRequest.getRequest().setClientId("am_org_role_mapping_service");
        assignmentRequest.setRequestedRoles(getRequestedOrgRole());
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.LEGAL_OPERATIONS);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setClassification(Classification.RESTRICTED);
            roleAssignment.setRoleName("tribunal-caseworker");
            roleAssignment.setGrantType(STANDARD);
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
        //clientId check not implemented yet
        //assignmentRequest.getRequest().setClientId("am_org_role_mapping_service");
        assignmentRequest.setRequestedRoles(getRequestedOrgRole());
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.LEGAL_OPERATIONS);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName("senior-tribunal-caseworker");
            roleAssignment.setGrantType(STANDARD);
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
        //clientId check not implemented yet
        //assignmentRequest.getRequest().setClientId("am_org_role_mapping_service");
        assignmentRequest.setRequestedRoles(getRequestedOrgRole());
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.LEGAL_OPERATIONS);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName("tribunal-caseworker");
            roleAssignment.setGrantType(STANDARD);
            roleAssignment.getAttributes().put("jurisdiction", convertValueJsonNode("CMC"));
            roleAssignment.getAttributes().put("primaryLocation", convertValueJsonNode("abc"));
        });
        buildExecuteKieSession();

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            assertEquals("\"CMC\"", roleAssignment.getAttributes().get("jurisdiction").toString());
            assertEquals(Status.REJECTED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldRejectOrgValidationForTCW_MissingPrimaryLocation_S009() {
        //clientId check not implemented yet
        //assignmentRequest.getRequest().setClientId("am_org_role_mapping_service");
        assignmentRequest.setRequestedRoles(getRequestedOrgRole());
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.LEGAL_OPERATIONS);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName("tribunal-caseworker");
            roleAssignment.setGrantType(STANDARD);
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
        //clientId check not implemented yet
        //assignmentRequest.getRequest().setClientId("am_org_role_mapping_service");
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

    //@Test
    void shouldRejectDeleteRequestedRoleForOrgWrongClientId_S011() {

        assignmentRequest.getRequest().setClientId("ccd-gw");
        assignmentRequest.setRequestedRoles(getRequestedOrgRole());
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setStatus(Status.DELETE_REQUESTED);
            roleAssignment.setRoleCategory(RoleCategory.LEGAL_OPERATIONS);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
        });
        buildExecuteKieSession();

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            assertEquals(Status.DELETE_REJECTED, roleAssignment.getStatus());
        });
    }

}
