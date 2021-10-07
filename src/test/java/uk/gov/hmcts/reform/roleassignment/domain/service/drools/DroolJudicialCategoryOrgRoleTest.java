package uk.gov.hmcts.reform.roleassignment.domain.service.drools;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType.STANDARD;
import static uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder.getRequestedOrgRole;
import static uk.gov.hmcts.reform.roleassignment.util.JacksonUtils.convertValueJsonNode;

@RunWith(MockitoJUnitRunner.class)
class DroolJudicialCategoryOrgRoleTest extends DroolBase {

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

        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment ->
                                                                   assertEquals(
                                                                       Status.APPROVED,
                                                                       roleAssignment.getStatus()
                                                                   )
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
}
