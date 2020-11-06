package uk.gov.hmcts.reform.roleassignment.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType.SPECIFIC;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType.STANDARD;
import static uk.gov.hmcts.reform.roleassignment.util.JacksonUtils.convertValueJsonNode;

@RunWith(MockitoJUnitRunner.class)
class DroolJudicialCategoryTest extends DroolBase {

    @Test
    void shouldApproveRequestedRoleForCase() {


        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.JUDICIAL);
            roleAssignment.setRoleType(RoleType.CASE);
            roleAssignment.setRoleName("judge");
            roleAssignment.setGrantType(SPECIFIC);
            roleAssignment.setBeginTime(LocalDateTime.now());
            roleAssignment.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));
        });

        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());
        // Run the rules
        kieSession.execute(facts);
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment ->
                                                                   assertEquals(
                                                                       Status.APPROVED,
                                                                       roleAssignment.getStatus()
                                                                   )
        );


    }

    @Test
    void shouldRejectCaseValidationForRequestedRole() {


        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.JUDICIAL);
            roleAssignment.setRoleType(RoleType.CASE);
            roleAssignment.setRoleName("judge");
            roleAssignment.setGrantType(SPECIFIC);
            roleAssignment.setBeginTime(LocalDateTime.now());

        });

        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());
        // Run the rules
        kieSession.execute(facts);
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment ->
                                                                   assertNotEquals(
                                                                       Status.APPROVED,
                                                                       roleAssignment.getStatus()
                                                                   )
        );


    }

    @Test
    void shouldApprovedRequestedRoleForOrg() {


        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.JUDICIAL);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName("judge");
            roleAssignment.setGrantType(STANDARD);
            roleAssignment.getAttributes().put("region", convertValueJsonNode("north-east"));
            roleAssignment.getAttributes().put("jurisdiction", convertValueJsonNode("IA"));
        });

        // facts must contain all requested role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());
        // Run the rules
        kieSession.execute(facts);
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment ->
                                                                   assertEquals(
                                                                       Status.APPROVED,
                                                                       roleAssignment.getStatus()
                                                                   )
        );


    }

    @Test
    void shouldRejectOrgValidation_MissingAttributeJurisdiction() {


        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.JUDICIAL);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName("judge");
            roleAssignment.setGrantType(STANDARD);
            roleAssignment.getAttributes().put("region", convertValueJsonNode("north-east"));

        });

        // facts must contain all requested role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());
        // Run the rules
        kieSession.execute(facts);
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment ->
                                                                   assertNotEquals(
                                                                       Status.APPROVED,
                                                                       roleAssignment.getStatus()
                                                                   )
        );


    }
}
