package uk.gov.hmcts.reform.roleassignment.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType.SPECIFIC;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType.STANDARD;
import static uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder.buildExistingRoleForIAC;
import static uk.gov.hmcts.reform.roleassignment.util.JacksonUtils.convertValueJsonNode;

@RunWith(MockitoJUnitRunner.class)
class DroolStaffCategoryTest extends DroolBase {

    @Test
    void shouldApprovedOrgRequestedRoleForTCW() {


        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.STAFF);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName("tribunal-caseworker");
            roleAssignment.setGrantType(STANDARD);
            roleAssignment.getAttributes().put("jurisdiction", convertValueJsonNode("IA"));
            roleAssignment.getAttributes().put("primaryLocation", convertValueJsonNode("abc"));
        });

        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            assertEquals(Status.APPROVED, roleAssignment.getStatus());
        });


    }

    @Test
    void shouldRejectOrgRequestedRoleForTCW_PrimaryLocationMissing() {


        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.STAFF);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName("tribunal-caseworker");
            roleAssignment.setGrantType(STANDARD);
            roleAssignment.getAttributes().put("jurisdiction", convertValueJsonNode("IA"));

        });

        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            assertNotEquals(Status.APPROVED, roleAssignment.getStatus());
        });


    }

    @Test
    void shouldApprovedCaseValidationForTCW() {

        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.STAFF);
            roleAssignment.setRoleType(RoleType.CASE);
            roleAssignment.setRoleName("tribunal-caseworker");
            roleAssignment.setGrantType(SPECIFIC);
            roleAssignment.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

        });

        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        //facts must contain existing role of assigner
        facts.add(buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
             "senior-tribunal-caseworker"));

        //facts must contain existing role of assignees
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment ->
                                         facts.add(buildExistingRoleForIAC(roleAssignment.getActorId(),
                                                                 "tribunal-caseworker")));


        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            assertEquals(Status.APPROVED, roleAssignment.getStatus());
        });


    }

    @Test
    void shouldRejectIACValidation_MissingExistingRoleOfAssignee() {

        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.STAFF);
            roleAssignment.setRoleType(RoleType.CASE);
            roleAssignment.setRoleName("tribunal-caseworker");
            roleAssignment.setGrantType(SPECIFIC);
            roleAssignment.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

        });

        // facts must contain all requested role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        //facts must contain existing role of assigner
        facts.add(buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
             "senior-tribunal-caseworker"));


        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            assertEquals(Status.REJECTED, roleAssignment.getStatus());

        });


    }

    @Test
    void shouldApprovedOrgRequestedRoleForSTCW() {

        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.STAFF);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName("senior-tribunal-caseworker");
            roleAssignment.setGrantType(STANDARD);
            roleAssignment.getAttributes().put("jurisdiction", convertValueJsonNode("IA"));
            roleAssignment.getAttributes().put("primaryLocation", convertValueJsonNode("abc"));
        });


        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            assertEquals(Status.APPROVED, roleAssignment.getStatus());
        });


    }

    @Test
    void shouldRejectOrgValidationForSTCW_MissingAttributeJurisdiction() {

        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.STAFF);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName("senior-tribunal-caseworker");
            roleAssignment.setGrantType(STANDARD);
            roleAssignment.getAttributes().put("primaryLocation", convertValueJsonNode("abc"));
        });


        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            assertNotEquals(Status.APPROVED, roleAssignment.getStatus());
        });


    }


    @Test
    void shouldPassDeleteRequestedRoleForOrg() {

        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setStatus(Status.DELETE_REQUESTED);
            roleAssignment.setRoleCategory(RoleCategory.STAFF);
            roleAssignment.setRoleType(RoleType.ORGANISATION);

        });


        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            assertEquals(Status.DELETE_APPROVED, roleAssignment.getStatus());
        });


    }

    @Test
    void shouldRejectDeleteRequestedRoleForOrg() {

        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setStatus(Status.DELETE_REQUESTED);
            roleAssignment.setRoleCategory(RoleCategory.STAFF);

        });


        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            assertEquals(Status.DELETE_REJECTED, roleAssignment.getStatus());
        });


    }


    @Test
    void shouldDeleteApprovedRequestedRoleForCase() {

        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setStatus(Status.DELETE_REQUESTED);
            roleAssignment.setRoleCategory(RoleCategory.STAFF);
            roleAssignment.setRoleType(RoleType.CASE);
            roleAssignment.setRoleName("tribunal-caseworker");
            roleAssignment.setGrantType(SPECIFIC);
            roleAssignment.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

        });

        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        //facts must contain existing role of assigner
        facts.add(buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
            "senior-tribunal-caseworker"));


        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            assertEquals(Status.DELETE_APPROVED, roleAssignment.getStatus());
        });


    }

    @Test
    void shouldRejectRequestedRoleForDelete_MissingExistingRole() {

        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setStatus(Status.DELETE_REQUESTED);
            roleAssignment.setRoleCategory(RoleCategory.STAFF);
            roleAssignment.setRoleType(RoleType.CASE);
            roleAssignment.setRoleName("tribunal-caseworker");
            roleAssignment.setGrantType(SPECIFIC);
            roleAssignment.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

        });

        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());


        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            assertEquals(Status.DELETE_REJECTED, roleAssignment.getStatus());
        });


    }
}
