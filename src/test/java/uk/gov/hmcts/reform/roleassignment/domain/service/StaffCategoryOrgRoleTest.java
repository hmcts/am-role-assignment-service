package uk.gov.hmcts.reform.roleassignment.domain.service;

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
    void shouldApproveOrgRequestedRoleForTCW() {
        //clientId check not implemented yet
        //assignmentRequest.getRequest().setClientId("am_org_role_mapping_service");
        assignmentRequest.setRequestedRoles(getRequestedOrgRole());
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.STAFF);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName("tribunal-caseworker");
            roleAssignment.setGrantType(STANDARD);
            roleAssignment.getAttributes().put("jurisdiction", convertValueJsonNode("IA"));
            roleAssignment.getAttributes().put("primaryLocation", convertValueJsonNode("abc"));
        });

        BuildnExecuteKieSession();

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            assertEquals(Status.APPROVED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldApproveOrgRequestedRoleForSTCW() {
        //clientId check not implemented yet
        //assignmentRequest.getRequest().setClientId("am_org_role_mapping_service");
        assignmentRequest.setRequestedRoles(getRequestedOrgRole());
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.STAFF);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName("senior-tribunal-caseworker");
            roleAssignment.setGrantType(STANDARD);
            roleAssignment.getAttributes().put("jurisdiction", convertValueJsonNode("IA"));
            roleAssignment.getAttributes().put("primaryLocation", convertValueJsonNode("abc"));
        });

        BuildnExecuteKieSession();

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            assertEquals(Status.APPROVED, roleAssignment.getStatus());
        });
    }

    //@Test
    void shouldRejectOrgRequestedRoleForTCW_WrongClientID() {

        assignmentRequest.getRequest().setClientId("ccd-gw");

        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.STAFF);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName("tribunal-caseworker");
            roleAssignment.setGrantType(STANDARD);
            roleAssignment.getAttributes().put("jurisdiction", convertValueJsonNode("IA"));
            roleAssignment.getAttributes().put("primaryLocation", convertValueJsonNode("abc"));
        });
        BuildnExecuteKieSession();

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            assertNotEquals(Status.APPROVED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldRejectOrgValidationForTCW_WrongRoleCategory() {
        //clientId check not implemented yet
        //assignmentRequest.getRequest().setClientId("am_org_role_mapping_service");

        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.JUDICIAL);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName("tribunal-caseworker");
            roleAssignment.setGrantType(STANDARD);
            roleAssignment.getAttributes().put("jurisdiction", convertValueJsonNode("IA"));
            roleAssignment.getAttributes().put("primaryLocation", convertValueJsonNode("abc"));
        });
        BuildnExecuteKieSession();

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            assertNotEquals(Status.APPROVED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldRejectOrgValidationForTCW_WrongGrantType() {
        //clientId check not implemented yet
        //assignmentRequest.getRequest().setClientId("am_org_role_mapping_service");

        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.STAFF);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName("tribunal-caseworker");
            roleAssignment.setGrantType(SPECIFIC);
            roleAssignment.getAttributes().put("jurisdiction", convertValueJsonNode("IA"));
            roleAssignment.getAttributes().put("primaryLocation", convertValueJsonNode("abc"));
        });
        BuildnExecuteKieSession();

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            assertNotEquals(Status.APPROVED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldRejectOrgValidationForTCW_WrongClassification() {
        //clientId check not implemented yet
        //assignmentRequest.getRequest().setClientId("am_org_role_mapping_service");

        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.STAFF);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setClassification(Classification.RESTRICTED);
            roleAssignment.setRoleName("tribunal-caseworker");
            roleAssignment.setGrantType(STANDARD);
            roleAssignment.getAttributes().put("jurisdiction", convertValueJsonNode("IA"));
            roleAssignment.getAttributes().put("primaryLocation", convertValueJsonNode("abc"));
        });
        BuildnExecuteKieSession();

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            assertNotEquals(Status.APPROVED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldRejectOrgValidationForSTCW_MissingJurisdiction() {
        //clientId check not implemented yet
        //assignmentRequest.getRequest().setClientId("am_org_role_mapping_service");

        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.STAFF);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName("senior-tribunal-caseworker");
            roleAssignment.setGrantType(STANDARD);
            roleAssignment.getAttributes().put("primaryLocation", convertValueJsonNode("abc"));
        });
        BuildnExecuteKieSession();

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            assertNotEquals(Status.APPROVED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldRejectOrgValidationForTCW_WrongJurisdiction() {
        //clientId check not implemented yet
        //assignmentRequest.getRequest().setClientId("am_org_role_mapping_service");

        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.STAFF);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName("tribunal-caseworker");
            roleAssignment.setGrantType(STANDARD);
            roleAssignment.getAttributes().put("jurisdiction", convertValueJsonNode("CMC"));
            roleAssignment.getAttributes().put("primaryLocation", convertValueJsonNode("abc"));
        });
        BuildnExecuteKieSession();

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            assertNotEquals(Status.APPROVED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldRejectOrgValidationForTCW_MissingPrimaryLocation() {
        //clientId check not implemented yet
        //assignmentRequest.getRequest().setClientId("am_org_role_mapping_service");

        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.STAFF);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName("tribunal-caseworker");
            roleAssignment.setGrantType(STANDARD);
            roleAssignment.getAttributes().put("jurisdiction", convertValueJsonNode("IA"));

        });
        BuildnExecuteKieSession();

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            assertNotEquals(Status.APPROVED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldApproveDeleteRequestedRoleForOrg() {
        //clientId check not implemented yet
        //assignmentRequest.getRequest().setClientId("am_org_role_mapping_service");

        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setStatus(Status.DELETE_REQUESTED);
            roleAssignment.setRoleCategory(RoleCategory.STAFF);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
        });
        BuildnExecuteKieSession();

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            assertEquals(Status.DELETE_APPROVED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldRejectDeleteRequestedRole_MissingRoleType() {

        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setStatus(Status.DELETE_REQUESTED);
            roleAssignment.setRoleCategory(RoleCategory.STAFF);
        });
        BuildnExecuteKieSession();

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            assertEquals(Status.DELETE_REJECTED, roleAssignment.getStatus());
        });


    }


    //@Test
    void shouldRejectDeleteRequestedRoleForOrgWrongClientId() {

        assignmentRequest.getRequest().setClientId("ccd-gw");

        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setStatus(Status.DELETE_REQUESTED);
            roleAssignment.setRoleCategory(RoleCategory.STAFF);
            roleAssignment.setRoleType(RoleType.ORGANISATION);

        });
        BuildnExecuteKieSession();

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            assertEquals(Status.DELETE_REJECTED, roleAssignment.getStatus());
        });
    }

    private void BuildnExecuteKieSession() {
        // facts must contain the request
        facts.add(assignmentRequest.getRequest());
        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());
        // Run the rules
        kieSession.execute(facts);
    }

}
