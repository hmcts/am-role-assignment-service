package uk.gov.hmcts.reform.roleassignment.domain.service.drools;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.launchdarkly.FeatureFlagEnum;
import uk.gov.hmcts.reform.roleassignment.launchdarkly.LDFeatureFlag;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType.STANDARD;
import static uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder.getRequestedOrgRole;
import static uk.gov.hmcts.reform.roleassignment.util.JacksonUtils.convertValueJsonNode;

@RunWith(MockitoJUnitRunner.class)
class DroolJudicialCategoryTest extends DroolBase {

    @Test
    void shouldApproveRequestedRoleForCase() {

        RoleAssignment requestedRole1 =  getRequestedCaseRole(RoleCategory.JUDICIAL, "salaried-judge",
                                                              GrantType.SPECIFIC);
        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));
        requestedRole1.getAttributes().put("jurisdiction", convertValueJsonNode("JA"));
        requestedRole1.getAttributes().put("caseType", convertValueJsonNode("Salaried"));

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);

        assignmentRequest.setRequestedRoles(requestedRoles);
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {

            roleAssignment.setBeginTime(ZonedDateTime.now(ZoneOffset.UTC));
        });
        assignmentRequest.getRequest().setByPassOrgDroolRule(true);


        LDFeatureFlag ldFeatureFlag  =  LDFeatureFlag.builder().flagName(FeatureFlagEnum.getJudicialFlag.getValue())
            .status(true).build();
        ldFeatureFlags.add(ldFeatureFlag);

        //Execute Kie session
        buildExecuteKieSession();

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment ->
                                                                   assertEquals(
                                                                       Status.APPROVED,
                                                                       roleAssignment.getStatus()
                                                                   )
        );
    }

    @Test
    void shouldRejectCaseValidationForRequestedRoleMisssingCaseId() {

        RoleAssignment requestedRole1 =  getRequestedCaseRole(RoleCategory.JUDICIAL, "salaried-judge",
                                                              GrantType.SPECIFIC);

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);

        assignmentRequest.setRequestedRoles(requestedRoles);
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setBeginTime(ZonedDateTime.now(ZoneOffset.UTC));
        });

        //Execute Kie session
        buildExecuteKieSession();

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment ->
                                                                   assertEquals(
                                                                       Status.REJECTED,
                                                                       roleAssignment.getStatus()
                                                                   )
        );
    }

    @Test
    void shouldApprovedRequestedRoleForOrg() {

        assignmentRequest.setRequestedRoles(getRequestedOrgRole());
        assignmentRequest.getRequest().setByPassOrgDroolRule(true);
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.JUDICIAL);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setStatus(Status.CREATE_REQUESTED);
            roleAssignment.setRoleName("salaried-judge");
            roleAssignment.setBeginTime(ZonedDateTime.now());
            roleAssignment.setEndTime(ZonedDateTime.now());
            roleAssignment.setGrantType(STANDARD);
            roleAssignment.getAttributes().put("region", convertValueJsonNode("north-east"));
            roleAssignment.getAttributes().put("jurisdiction", convertValueJsonNode("IA"));
            roleAssignment.getAttributes().put("baseLocation", convertValueJsonNode("1351"));
            roleAssignment.getAttributes().put("contractType", convertValueJsonNode("salaried"));
        });

        LDFeatureFlag ldFeatureFlag  =  LDFeatureFlag.builder().flagName(FeatureFlagEnum.getJudicialFlag.getValue())
            .status(true).build();
        ldFeatureFlags.add(ldFeatureFlag);
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
            roleAssignment.setRoleName("salaried-judge");
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
