package uk.gov.hmcts.reform.roleassignment.domain.service.drools;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.roleassignment.domain.model.ExistingRoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.FeatureFlag;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.FeatureFlagEnum;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType.CHALLENGED;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType.SPECIFIC;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.CREATE_REQUESTED;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.DELETE_REQUESTED;
import static uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder.buildExistingRoleForIAC;
import static uk.gov.hmcts.reform.roleassignment.util.JacksonUtils.convertValueJsonNode;
import static uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder.getRequestedCaseRole;
import static uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder.getRequestedCaseRole_ra;



@RunWith(MockitoJUnitRunner.class)
class DroolJudicialCategoryCaseRoleTest extends DroolBase {

    @Test
    void shouldApproveRequestedRoleForCase_CaseAllocator() {

        RoleAssignment requestedRole1 = getRequestedCaseRole_ra(RoleCategory.JUDICIAL,
                                                                "case-allocator",
                                                                SPECIFIC, "caseId",
                                                                "1234567890123456", CREATE_REQUESTED);

        assignmentRequest.setRequestedRoles(List.of(requestedRole1));
        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_JRD_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        executeDroolRules(List.of(buildExistingRoleForIAC(requestedRole1.getActorId(),
                                                          "case-allocator",
                                                          RoleCategory.JUDICIAL),
                                  buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
                                                  "case-allocator",
                                                  RoleCategory.JUDICIAL)));

        //assertion
        assignmentRequest.getRequestedRoles().forEach(ra -> {
            assertEquals(Status.APPROVED, ra.getStatus());
        });
    }

    @Test
    void shouldDeleteApprovedRequestedRoleForCase_CaseAllocator() {

        RoleAssignment requestedRole1 = getRequestedCaseRole_ra(RoleCategory.JUDICIAL,
                                                                "case-allocator",
                                                                SPECIFIC, "caseId",
                                                                "1234567890123456", DELETE_REQUESTED);

        assignmentRequest.setRequestedRoles(List.of(requestedRole1));
        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_JRD_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        executeDroolRules(List.of(buildExistingRoleForIAC(requestedRole1.getActorId(),
                                                          "case-allocator",
                                                          RoleCategory.JUDICIAL),
                                  buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
                                                          "case-allocator",
                                                          RoleCategory.JUDICIAL)));

        //assertion
        assignmentRequest.getRequestedRoles().forEach(ra -> {
            assertEquals(Status.DELETE_APPROVED, ra.getStatus());
        });
    }

    @Test
    void shouldApproveRequestedRoleForCase_JudgeRoles_Lead_Hearing() {

        RoleAssignment requestedRole1 = getRequestedCaseRole_ra(RoleCategory.JUDICIAL,
                                                                "lead-judge",
                                                                SPECIFIC, "caseId",
                                                                "1234567890123456", CREATE_REQUESTED);
        RoleAssignment requestedRole2 = getRequestedCaseRole_ra(RoleCategory.JUDICIAL,
                                                                "hearing-judge",
                                                                SPECIFIC, "caseId",
                                                                "1234567890123456", CREATE_REQUESTED);

        assignmentRequest.setRequestedRoles(List.of(requestedRole1, requestedRole2));
        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_JRD_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        executeDroolRules(List.of(buildExistingRoleForIAC(requestedRole1.getActorId(),
                                                          "leadership-judge",
                                                          RoleCategory.JUDICIAL),
                                  buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
                                                          "case-allocator",
                                                          RoleCategory.JUDICIAL),
                                  buildExistingRoleForIAC(requestedRole2.getActorId(),
                                                          "judge",
                                                          RoleCategory.JUDICIAL),
                                  buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
                                                          "case-allocator",
                                                          RoleCategory.JUDICIAL)));

        //assertion
        assignmentRequest.getRequestedRoles().forEach(ra -> {
            assertEquals(Status.APPROVED, ra.getStatus());
        });
    }

    @Test
    void shouldApproveRequestedRoleForCase_JudgeRoles_Ftpa_HearingPanel() {


        RoleAssignment requestedRole1 = getRequestedCaseRole_ra(RoleCategory.JUDICIAL,
                                                                "ftpa-judge",
                                                                SPECIFIC, "caseId",
                                                                "1234567890123456", CREATE_REQUESTED);
        RoleAssignment requestedRole2 = getRequestedCaseRole_ra(RoleCategory.JUDICIAL,
                                                                "hearing-panel-judge",
                                                                SPECIFIC, "caseId",
                                                                "1234567890123456", CREATE_REQUESTED);

        assignmentRequest.setRequestedRoles(List.of(requestedRole1, requestedRole2));
        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_JRD_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        executeDroolRules(List.of(buildExistingRoleForIAC(requestedRole1.getActorId(),
                                                          "judge",
                                                          RoleCategory.JUDICIAL),
                                  buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
                                                          "case-allocator",
                                                          RoleCategory.JUDICIAL),
                                  buildExistingRoleForIAC(requestedRole2.getActorId(),
                                                          "judge",
                                                          RoleCategory.JUDICIAL),
                                  buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
                                                          "case-allocator",
                                                          RoleCategory.JUDICIAL)));

        //assertion
        assignmentRequest.getRequestedRoles().forEach(ra -> {
            assertEquals(Status.APPROVED, ra.getStatus());
        });
    }

    @Test
    void shouldDeleteApprovedRequestedRoleForCase_LeadJudge() {

        RoleAssignment requestedRole1 = getRequestedCaseRole_ra(RoleCategory.JUDICIAL,
                                                                "lead-judge",
                                                                SPECIFIC, "caseId",
                                                                "1234567890123456", DELETE_REQUESTED);

        assignmentRequest.setRequestedRoles(List.of(requestedRole1));
        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_JRD_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        executeDroolRules(List.of(buildExistingRoleForIAC(requestedRole1.getActorId(),
                                                          "lead-judge",
                                                          RoleCategory.JUDICIAL),
                                  buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
                                                          "case-allocator",
                                                          RoleCategory.JUDICIAL)));

        //assertion
        assignmentRequest.getRequestedRoles().forEach(ra -> {
            assertEquals(Status.DELETE_APPROVED, ra.getStatus());
        });
    }

    @Test
    void shouldDeleteApprovedRequestedRoleForCase_HearingJudge() {

        RoleAssignment requestedRole1 = getRequestedCaseRole_ra(RoleCategory.JUDICIAL,
                                                                "hearing-judge",
                                                                SPECIFIC, "caseId",
                                                                "1234567890123456", DELETE_REQUESTED);

        assignmentRequest.setRequestedRoles(List.of(requestedRole1));
        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_JRD_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        executeDroolRules(List.of(buildExistingRoleForIAC(requestedRole1.getActorId(),
                                                          "hearing-judge",
                                                          RoleCategory.JUDICIAL),
                                  buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
                                                          "case-allocator",
                                                          RoleCategory.JUDICIAL)));

        //assertion
        assignmentRequest.getRequestedRoles().forEach(ra -> {
            assertEquals(Status.DELETE_APPROVED, ra.getStatus());
        });
    }

    @Test
    void shouldDeleteApprovedRequestedRoleForCase_FtpaJudge() {

        RoleAssignment requestedRole1 = getRequestedCaseRole_ra(RoleCategory.JUDICIAL,
                                                                "ftpa-judge",
                                                                SPECIFIC, "caseId",
                                                                "1234567890123456", DELETE_REQUESTED);

        assignmentRequest.setRequestedRoles(List.of(requestedRole1));
        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_JRD_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        executeDroolRules(List.of(buildExistingRoleForIAC(requestedRole1.getActorId(),
                                                          "ftpa-judge",
                                                          RoleCategory.JUDICIAL),
                                  buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
                                                          "case-allocator",
                                                          RoleCategory.JUDICIAL)));

        //assertion
        assignmentRequest.getRequestedRoles().forEach(ra -> {
            assertEquals(Status.DELETE_APPROVED, ra.getStatus());
        });
    }

    @Test
    void shouldDeleteApprovedRequestedRoleForCase_HearingPanelJudge() {

        RoleAssignment requestedRole1 = getRequestedCaseRole_ra(RoleCategory.JUDICIAL,
                                                                "hearing-panel-judge",
                                                                SPECIFIC, "caseId",
                                                                "1234567890123456", DELETE_REQUESTED);

        assignmentRequest.setRequestedRoles(List.of(requestedRole1));
        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_JRD_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        executeDroolRules(List.of(buildExistingRoleForIAC(requestedRole1.getActorId(),
                                                          "hearing-panel-judge",
                                                          RoleCategory.JUDICIAL),
                                  buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
                                                          "case-allocator",
                                                          RoleCategory.JUDICIAL)));

        //assertion
        assignmentRequest.getRequestedRoles().forEach(ra -> {
            assertEquals(Status.DELETE_APPROVED, ra.getStatus());
        });
    }

    @Test
    void shouldRejectRequestedRole_WrongRequesterRoleName() {

        RoleAssignment requestedRole1 = getRequestedCaseRole_ra(RoleCategory.JUDICIAL,
                                                                "hearing-panel-judge",
                                                                SPECIFIC, "caseId",
                                                                "1234567890123456", CREATE_REQUESTED);

        assignmentRequest.setRequestedRoles(List.of(requestedRole1));
        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_JRD_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        executeDroolRules(List.of(buildExistingRoleForIAC(requestedRole1.getActorId(),
                                                          "hearing-panel-judge",
                                                          RoleCategory.JUDICIAL),
                                  buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
                                                          "Hearing-panel-judge",
                                                          RoleCategory.JUDICIAL)));

        //assertion
        assignmentRequest.getRequestedRoles().forEach(ra -> {
            assertEquals(Status.REJECTED, ra.getStatus());
        });
    }

    @Test
    void shouldRejectRequestedRoleForWrongData_GrantType() {

        RoleAssignment requestedRole1 = getRequestedCaseRole_ra(RoleCategory.JUDICIAL,
                                                                "case-allocator",
                                                                CHALLENGED, "caseId",
                                                                "1234567890123456", CREATE_REQUESTED);

        assignmentRequest.setRequestedRoles(List.of(requestedRole1));
        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_JRD_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        executeDroolRules(List.of(buildExistingRoleForIAC(requestedRole1.getActorId(),
                                                          "case-allocator",
                                                          RoleCategory.JUDICIAL),
                                  buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
                                                          "case-allocator",
                                                          RoleCategory.JUDICIAL)));

        //assertion
        assignmentRequest.getRequestedRoles().forEach(ra -> {
            assertEquals(Status.REJECTED, ra.getStatus());
        });
    }

    @Test
    void shouldRejectRequestedRole_WrongAssignerID() {

        RoleAssignment requestedRole1 = getRequestedCaseRole_ra(RoleCategory.JUDICIAL,
                                                                "case-allocator",
                                                                SPECIFIC, "caseId",
                                                                "1234567890123456", CREATE_REQUESTED);

        assignmentRequest.setRequestedRoles(List.of(requestedRole1));

        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_JRD_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        executeDroolRules(List.of(buildExistingRoleForIAC(requestedRole1.getActorId(),
                                                          "case-allocator",
                                                          RoleCategory.JUDICIAL),
                                  buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId() + "12",
                                                          "case-allocator",
                                                          RoleCategory.JUDICIAL)));

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(Status.REJECTED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldRejectRequestedRoleForDelete_WrongAssignerID() {

        RoleAssignment requestedRole1 = getRequestedCaseRole_ra(RoleCategory.JUDICIAL,
                                                                "case-allocator",
                                                                SPECIFIC, "caseId",
                                                                "1234567890123456", DELETE_REQUESTED);

        assignmentRequest.setRequestedRoles(List.of(requestedRole1));

        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_JRD_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        executeDroolRules(List.of(buildExistingRoleForIAC(requestedRole1.getActorId(),
                                                          "case-allocator",
                                                          RoleCategory.JUDICIAL),
                                  buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId() + "12",
                                                          "case-allocator",
                                                          RoleCategory.JUDICIAL)));

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(Status.DELETE_REJECTED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldRejectRequestedRole_MissingCaseId() {
        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.JUDICIAL, "case-allocator",
                                                             SPECIFIC);
        assignmentRequest.setRequestedRoles(List.of(requestedRole1));

        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_JRD_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        executeDroolRules(List.of(buildExistingRoleForIAC(requestedRole1.getActorId(),
                                                          "case-allocator",
                                                          RoleCategory.JUDICIAL),
                                  buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
                                                          "case-allocator",
                                                          RoleCategory.JUDICIAL)));

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(Status.REJECTED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldRejectCaseValidation_MissingExistingRA() {
        RoleAssignment requestedRole1 = getRequestedCaseRole_ra(RoleCategory.JUDICIAL, "lead-judge",
                                                                SPECIFIC, "caseId",
                                                                "1234567890123456", CREATE_REQUESTED);
        assignmentRequest.setRequestedRoles(List.of(requestedRole1));

        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_JRD_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);


        executeDroolRules(List.of(buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
                                                          "case-allocator",
                                                          RoleCategory.JUDICIAL)));

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment ->
                                                          assertEquals(Status.REJECTED, roleAssignment.getStatus()));
    }

    @Test
    void shouldRejectRequestedRoleF_WrongRequesterJurisdiction() {

        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.JUDICIAL, "case-allocator",
                                                             SPECIFIC);
        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);
        assignmentRequest.setRequestedRoles(requestedRoles);

        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_JRD_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        ExistingRoleAssignment existingActorAssignment1 = buildExistingRoleForIAC(
            requestedRole1.getActorId(),
            "case-allocator",
            RoleCategory.JUDICIAL
        );

        ExistingRoleAssignment existingRequesterAssignment1 = buildExistingRoleForIAC(
            assignmentRequest.getRequest().getAssignerId(), "case-allocator",
            RoleCategory.JUDICIAL);

        existingRequesterAssignment1.getAttributes().put("jurisdiction", convertValueJsonNode("CMC"));

        List<ExistingRoleAssignment> existingRoleAssignments = new ArrayList<>();
        existingRoleAssignments.add(existingActorAssignment1);
        existingRoleAssignments.add(existingRequesterAssignment1);

        // facts must contain the request
        executeDroolRules(existingRoleAssignments);

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(Status.REJECTED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldRejectRequestedRoleF_WrongAssigneeJurisdiction() {

        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.JUDICIAL, "lead-judge",
                                                             SPECIFIC);
        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);
        assignmentRequest.setRequestedRoles(requestedRoles);

        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_JRD_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        ExistingRoleAssignment existingActorAssignment1 = buildExistingRoleForIAC(
            requestedRole1.getActorId(),
            "leadership-judge",
            RoleCategory.JUDICIAL
        );

        ExistingRoleAssignment existingRequesterAssignment1 = buildExistingRoleForIAC(
            assignmentRequest.getRequest().getAssignerId(), "case-allocator",
            RoleCategory.JUDICIAL);

        existingActorAssignment1.getAttributes().put("jurisdiction", convertValueJsonNode("CMC"));

        List<ExistingRoleAssignment> existingRoleAssignments = new ArrayList<>();
        existingRoleAssignments.add(existingActorAssignment1);
        existingRoleAssignments.add(existingRequesterAssignment1);

        // facts must contain the request
        executeDroolRules(existingRoleAssignments);

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(Status.REJECTED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldRejectRequestedRoleForCreate_IACFlagFalse() {
        RoleAssignment requestedRole1 = getRequestedCaseRole_ra(RoleCategory.JUDICIAL,
                                                                "case-allocator",
                                                                SPECIFIC, "caseId",
                                                                "1234567890123456", CREATE_REQUESTED);

        assignmentRequest.setRequestedRoles(List.of(requestedRole1));
        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_JRD_1_0.getValue())
            .status(false).build();
        featureFlags.add(featureFlag);

        executeDroolRules(List.of(buildExistingRoleForIAC(requestedRole1.getActorId(),
                                                          "case-allocator",
                                                          RoleCategory.JUDICIAL),
                                  buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
                                                          "case-allocator",
                                                          RoleCategory.JUDICIAL)));

        //assertion
        assignmentRequest.getRequestedRoles().forEach(ra -> {
            assertEquals(Status.REJECTED, ra.getStatus());
        });
    }


}
