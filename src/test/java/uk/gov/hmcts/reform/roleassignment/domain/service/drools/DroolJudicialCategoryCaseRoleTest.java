package uk.gov.hmcts.reform.roleassignment.domain.service.drools;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.roleassignment.domain.model.FeatureFlag;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Classification;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.FeatureFlagEnum;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder.getRequestedOrgRole;
import static uk.gov.hmcts.reform.roleassignment.util.JacksonUtils.convertValueJsonNode;

@RunWith(MockitoJUnitRunner.class)
class DroolJudicialCategoryCaseRoleTest extends DroolBase {

    @Test
    void shouldApproveRequestedRoleForCase_Judge() {

        RoleAssignment requestedRole1 =  getRequestedCaseRole(RoleCategory.JUDICIAL, "judge", GrantType.SPECIFIC);
        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

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
                                                                       Status.APPROVED,
                                                                       roleAssignment.getStatus()
                                                                   )
        );
    }

    @Test
    void shouldApproveRequestedRoleForCase_CaseAllocator() {

        RoleAssignment requestedRole1 =  getRequestedCaseRole_2(RoleCategory.JUDICIAL,
                                                                "case-allocator",
                                                                GrantType.SPECIFIC,
                                                                RoleType.CASE,
                                                                Classification.PUBLIC
                                                                );
        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));
        requestedRole1.getAttributes().put("jurisdiction", convertValueJsonNode("IA"));
        requestedRole1.getAttributes().put("caseTypeId", convertValueJsonNode("Asylum"));

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);

        assignmentRequest.setRequestedRoles(requestedRoles);

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
    void shouldApproveRequestedRoleForCase_TaskSupervisor() {

        RoleAssignment requestedRole1 =  getRequestedCaseRole(RoleCategory.JUDICIAL, "task-supervisor",
                                                              GrantType.STANDARD);
        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);

        assignmentRequest.setRequestedRoles(requestedRoles);

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
    void shouldApproveRequestedRoleForCase_MoreJudges() {

        RoleAssignment requestedRole1 =  getRequestedCaseRole(RoleCategory.JUDICIAL, "lead-judge",
                                                              GrantType.SPECIFIC);
        RoleAssignment requestedRole2 =  getRequestedCaseRole(RoleCategory.JUDICIAL, "hearing-judge",
                                                              GrantType.SPECIFIC);
        RoleAssignment requestedRole3 =  getRequestedCaseRole(RoleCategory.JUDICIAL, "ftpa-judge",
                                                              GrantType.SPECIFIC);
        RoleAssignment requestedRole4 =  getRequestedCaseRole(RoleCategory.JUDICIAL, "hearing-panel-judge",
                                                              GrantType.SPECIFIC);
        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));
        requestedRole2.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));
        requestedRole3.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));
        requestedRole4.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);
        requestedRoles.add(requestedRole2);
        requestedRoles.add(requestedRole3);
        requestedRoles.add(requestedRole4);

        assignmentRequest.setRequestedRoles(requestedRoles);
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {

            roleAssignment.setBeginTime(ZonedDateTime.now(ZoneOffset.UTC));
        });

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
}
