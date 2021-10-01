package uk.gov.hmcts.reform.roleassignment.domain.service.drools;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.roleassignment.domain.model.ExistingRoleAssignment;
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
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType.SPECIFIC;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.CREATE_REQUESTED;
import static uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder.buildExistingRoleForIAC;
import static uk.gov.hmcts.reform.roleassignment.util.JacksonUtils.convertValueJsonNode;

@RunWith(MockitoJUnitRunner.class)
class DroolJudicialCategoryCaseRoleTest extends DroolBase {

    @Test
    void shouldApproveRequestedRoleForCase_Judge() {

        RoleAssignment requestedRole1 =  getRequestedCaseRole(RoleCategory.JUDICIAL, "judge", GrantType.SPECIFIC);
        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);

        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_1_1.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

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
    void shouldDeleteApprovedRequestedRoleForCase_Judge() {

        RoleAssignment requestedRole1 =  getRequestedCaseRole_2(RoleCategory.JUDICIAL,
                                                                "judge",
                                                                GrantType.SPECIFIC,
                                                                RoleType.CASE,
                                                                Classification.PUBLIC,
                                                                Status.DELETE_REQUESTED);
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
                                                                       Status.DELETE_APPROVED,
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
                                                                Classification.PUBLIC,
                                                                Status.CREATE_REQUESTED
                                                                );
        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));
        requestedRole1.getAttributes().put("jurisdiction", convertValueJsonNode("IA"));
        requestedRole1.getAttributes().put("caseType", convertValueJsonNode("Asylum"));

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
    void shouldDeleteApprovedRequestedRoleForCase_CaseAllocator() {

        RoleAssignment requestedRole1 =  getRequestedCaseRole_2(RoleCategory.JUDICIAL,
                                                                "case-allocator",
                                                                GrantType.SPECIFIC,
                                                                RoleType.CASE,
                                                                Classification.PUBLIC,
                                                                Status.DELETE_REQUESTED);
        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));
        requestedRole1.getAttributes().put("jurisdiction", convertValueJsonNode("IA"));
        requestedRole1.getAttributes().put("caseType", convertValueJsonNode("Asylum"));

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);

        assignmentRequest.setRequestedRoles(requestedRoles);

        //Execute Kie session
        buildExecuteKieSession();

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment ->
                                                                   assertEquals(
                                                                       Status.DELETE_APPROVED,
                                                                       roleAssignment.getStatus()
                                                                   )
        );
    }

    //Commented out for now as DroolRule is missing for 'task-supervisor'
    //    @Test
    //    void shouldApproveRequestedRoleForCase_TaskSupervisor() {
    //
    //        RoleAssignment requestedRole1 =  getRequestedCaseRole_2(RoleCategory.JUDICIAL,
    //                                                                "task-supervisor",
    //                                                                GrantType.STANDARD,
    //                                                                RoleType.ORGANISATION,
    //                                                                Classification.PUBLIC,
    //                                                                Status.CREATE_REQUESTED);
    //        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));
    //        requestedRole1.getAttributes().put("jurisdiction", convertValueJsonNode("IA"));
    //        requestedRole1.getAttributes().put("caseType", convertValueJsonNode("Asylum"));
    //
    //        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_1_0.getValue())
    //            .status(true).build();
    //        featureFlags.add(featureFlag);
    //
    //        List<RoleAssignment> requestedRoles = new ArrayList<>();
    //        requestedRoles.add(requestedRole1);
    //
    //        assignmentRequest.setRequestedRoles(requestedRoles);
    //
    //        //Execute Kie session
    //        buildExecuteKieSession();
    //
    //        //assertion
    //        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment ->
    //                                                                   assertEquals(
    //                                                                       Status.APPROVED,
    //                                                                       roleAssignment.getStatus()
    //                                                                   )
    //        );
    //    }
    @Test
    void shouldApproveRequestedRoleForCase_DifferentJudgeRoles() {

        RoleAssignment requestedRole1 =  getRequestedCaseRole(RoleCategory.JUDICIAL, "lead-judge",
                                                              GrantType.SPECIFIC);
        RoleAssignment requestedRole2 =  getRequestedCaseRole(RoleCategory.JUDICIAL, "hearing-judge",
                                                              GrantType.SPECIFIC);
        RoleAssignment requestedRole3 =  getRequestedCaseRole(RoleCategory.JUDICIAL, "ftpa-judge",
                                                              GrantType.SPECIFIC);
        RoleAssignment requestedRole4 =  getRequestedCaseRole(RoleCategory.JUDICIAL, "hearing-panel-judge",
                                                              GrantType.SPECIFIC);
        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));
        requestedRole1.getAttributes().put("jurisdiction", convertValueJsonNode("IA"));
        requestedRole1.getAttributes().put("caseType", convertValueJsonNode("Asylum"));

        requestedRole2.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));
        requestedRole2.getAttributes().put("jurisdiction", convertValueJsonNode("IA"));
        requestedRole2.getAttributes().put("caseType", convertValueJsonNode("Asylum"));

        requestedRole3.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));
        requestedRole3.getAttributes().put("jurisdiction", convertValueJsonNode("IA"));
        requestedRole3.getAttributes().put("caseType", convertValueJsonNode("Asylum"));

        requestedRole4.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));
        requestedRole4.getAttributes().put("jurisdiction", convertValueJsonNode("IA"));
        requestedRole4.getAttributes().put("caseType", convertValueJsonNode("Asylum"));

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);
        requestedRoles.add(requestedRole2);
        requestedRoles.add(requestedRole3);
        requestedRoles.add(requestedRole4);

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
    void shouldDeleteApprovedRequestedRoleForCase_DifferentJudgeRoles() {

        RoleAssignment requestedRole1 =  getRequestedCaseRole_2(RoleCategory.JUDICIAL, "lead-judge",
                                                                GrantType.SPECIFIC, RoleType.CASE,
                                                                Classification.PUBLIC,
                                                                Status.DELETE_REQUESTED);
        RoleAssignment requestedRole2 =  getRequestedCaseRole_2(RoleCategory.JUDICIAL, "hearing-judge",
                                                                GrantType.SPECIFIC, RoleType.CASE,
                                                                Classification.PUBLIC,
                                                                Status.DELETE_REQUESTED);
        RoleAssignment requestedRole3 =  getRequestedCaseRole_2(RoleCategory.JUDICIAL, "ftpa-judge",
                                                                GrantType.SPECIFIC, RoleType.CASE,
                                                                Classification.PUBLIC,
                                                                Status.DELETE_REQUESTED);
        RoleAssignment requestedRole4 =  getRequestedCaseRole_2(RoleCategory.JUDICIAL, "hearing-panel-judge",
                                                                GrantType.SPECIFIC, RoleType.CASE,
                                                                Classification.PUBLIC,
                                                                Status.DELETE_REQUESTED);
        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));
        requestedRole1.getAttributes().put("jurisdiction", convertValueJsonNode("IA"));
        requestedRole1.getAttributes().put("caseType", convertValueJsonNode("Asylum"));

        requestedRole2.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));
        requestedRole2.getAttributes().put("jurisdiction", convertValueJsonNode("IA"));
        requestedRole2.getAttributes().put("caseType", convertValueJsonNode("Asylum"));

        requestedRole3.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));
        requestedRole3.getAttributes().put("jurisdiction", convertValueJsonNode("IA"));
        requestedRole3.getAttributes().put("caseType", convertValueJsonNode("Asylum"));

        requestedRole4.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));
        requestedRole4.getAttributes().put("jurisdiction", convertValueJsonNode("IA"));
        requestedRole4.getAttributes().put("caseType", convertValueJsonNode("Asylum"));

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);
        requestedRoles.add(requestedRole2);
        requestedRoles.add(requestedRole3);
        requestedRoles.add(requestedRole4);

        assignmentRequest.setRequestedRoles(requestedRoles);

        //Execute Kie session
        buildExecuteKieSession();

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment ->
                                                                   assertEquals(
                                                                       Status.DELETE_APPROVED,
                                                                       roleAssignment.getStatus()
                                                                   )
        );
    }

    @Test
    void shouldRejectRequestedRoleForWrongData_GrantType() {

        RoleAssignment requestedRole1 =  getRequestedCaseRole_2(RoleCategory.JUDICIAL,
                                                                "judge",
                                                                GrantType.STANDARD,
                                                                RoleType.CASE,
                                                                Classification.PUBLIC,
                                                                Status.CREATE_REQUESTED);
        //GrantType should be SPECIFIC or CHALLENGED
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
                                                                       Status.REJECTED,
                                                                       roleAssignment.getStatus()
                                                                   )
        );
    }

    @Test
    void shouldRejectRequestedRoleForWrongData_RoleType() {

        RoleAssignment requestedRole1 =  getRequestedCaseRole_2(RoleCategory.JUDICIAL,
                                                                "lead-judge",
                                                                GrantType.SPECIFIC,
                                                                RoleType.ORGANISATION,
                                                                Classification.PUBLIC,
                                                                Status.CREATE_REQUESTED
        );
        //RoleType should be CASE for lead-judge
        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));
        requestedRole1.getAttributes().put("jurisdiction", convertValueJsonNode("IA"));
        requestedRole1.getAttributes().put("caseType", convertValueJsonNode("Asylum"));

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);

        assignmentRequest.setRequestedRoles(requestedRoles);

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
    void shouldRejectRequestedRoleForWrongData_Classification() {

        RoleAssignment requestedRole1 =  getRequestedCaseRole_2(RoleCategory.JUDICIAL,
                                                                "judge",
                                                                GrantType.SPECIFIC,
                                                                RoleType.CASE,
                                                                Classification.PRIVATE,
                                                                Status.CREATE_REQUESTED);
        //Classification should be PUBLIC
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
                                                                       Status.REJECTED,
                                                                       roleAssignment.getStatus()
                                                                   )
        );
    }


    @Test
    void shouldRejectRequestedRoleForNullMandatoryField_BeginTime() {

        RoleAssignment requestedRole1 =  getRequestedCaseRole(RoleCategory.JUDICIAL,
                                                                "judge",
                                                                GrantType.SPECIFIC);
        //BeginTime is a mandatory field - not provided
        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);

        assignmentRequest.setRequestedRoles(requestedRoles);

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
    void shouldRejectRequestedRoleForNullMandatoryFiled_Jurisdiction() {

        RoleAssignment requestedRole1 =  getRequestedCaseRole_2(RoleCategory.JUDICIAL,
                                                                "case-allocator",
                                                                GrantType.SPECIFIC,
                                                                RoleType.CASE,
                                                                Classification.PUBLIC,
                                                                Status.CREATE_REQUESTED);
        //Jurisdiction is a mandatory field - not provided
        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));
        requestedRole1.getAttributes().put("caseType", convertValueJsonNode("Asylum"));

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);

        assignmentRequest.setRequestedRoles(requestedRoles);

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
    void shouldRejectRequestedRoleForNullMandatoryField_CaseId() {

        RoleAssignment requestedRole1 =  getRequestedCaseRole(RoleCategory.JUDICIAL, "judge", GrantType.SPECIFIC);

        //CaseId is a mandatory field - not provided

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
    void shouldRejectRequestedRoleForCreate_IACFlagFalse() {
        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.JUDICIAL, "judge",
                                                             SPECIFIC, "caseId",
                                                             "1234567890123456", CREATE_REQUESTED);

        assignmentRequest.setRequestedRoles(List.of(requestedRole1));
        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_1_1.getValue())
            .status(false).build();
        featureFlags.add(featureFlag);

        executeDroolRules(List.of(buildExistingRoleForIAC(requestedRole1.getActorId(),
                                                          "judge",
                                                          RoleCategory.JUDICIAL)));
        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(Status.REJECTED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldRejectRequestedRoleForCreate_WrongExistingRoleID() {
        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.JUDICIAL, "judge",
                                                             SPECIFIC, "caseId",
                                                             "1234567890123456", CREATE_REQUESTED);

        assignmentRequest.setRequestedRoles(List.of(requestedRole1));
        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_1_1.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        executeDroolRules(List.of(buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId() + "23",
                                                          "judge",
                                                          RoleCategory.JUDICIAL)));
        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(Status.REJECTED, roleAssignment.getStatus());
        });
    }

    //    @Test
    //    void shouldRejectRequestedRoleForDelete_WrongExistingRoleID() {
    //        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.JUDICIAL, "judge",
    //                                                             SPECIFIC, "caseId",
    //                                                             "1234567890123456", DELETE_REQUESTED);
    //
    //        assignmentRequest.setRequestedRoles(List.of(requestedRole1));
    //        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_1_1.getValue())
    //            .status(true).build();
    //        featureFlags.add(featureFlag);
    //
    //        executeDroolRules(List.of(buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId() + "23",
    //                                                          "judge",
    //                                                          RoleCategory.JUDICIAL)));
    //        //assertion
    //        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
    //            assertEquals(Status.DELETE_REJECTED, roleAssignment.getStatus());
    //        });
    //    }

    private void executeDroolRules(List<ExistingRoleAssignment> existingRoleAssignments) {
        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        // facts must contain all existing role assignments
        facts.addAll(existingRoleAssignments);

        // facts must contain the request
        facts.add(assignmentRequest.getRequest());

        facts.addAll(featureFlags);

        // Run the rules
        kieSession.execute(facts);
    }
}
