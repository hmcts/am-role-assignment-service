package uk.gov.hmcts.reform.roleassignment.domain.service.drools;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.roleassignment.domain.model.ExistingRoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.FeatureFlag;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.FeatureFlagEnum;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType.CHALLENGED;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType.SPECIFIC;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.CREATE_REQUESTED;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.DELETE_REQUESTED;
import static uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder.buildExistingRoleForIAC;
import static uk.gov.hmcts.reform.roleassignment.util.JacksonUtils.convertValueJsonNode;

@RunWith(MockitoJUnitRunner.class)
class StaffCategoryCaseTest extends DroolBase {


    @Test
    void shouldApproveCaseRequestedRoles_RequesterOrgRoleTCW_S001() {
        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.LEGAL_OPERATIONS, "tribunal-caseworker",
                                                             SPECIFIC, "caseId",
                                                                "1234567890123456", CREATE_REQUESTED);
        assignmentRequest.setRequestedRoles(List.of(requestedRole1));
        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        executeDroolRules(List.of(buildExistingRoleForIAC(requestedRole1.getActorId(),
                                                          "senior-tribunal-caseworker",
                                                          RoleCategory.LEGAL_OPERATIONS),
                                  buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
                                                          "tribunal-caseworker",
                                                          RoleCategory.LEGAL_OPERATIONS)));
        //assertion
        assignmentRequest.getRequestedRoles().forEach(ra -> {
            assertEquals(Status.APPROVED, ra.getStatus());
        });
    }

    @Test
    void shouldRejectCaseRequestedRole_MissingExistingRoleOfRequester_S017() {
        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.LEGAL_OPERATIONS, "tribunal-caseworker",
                                                             SPECIFIC, "caseId",
                                                             "1234567890123456", DELETE_REQUESTED);
        assignmentRequest.setRequestedRoles(List.of(requestedRole1));

        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        executeDroolRules(List.of(buildExistingRoleForIAC(
            requestedRole1.getActorId(),"senior-tribunal-caseworker",
            RoleCategory.LEGAL_OPERATIONS)
        ));

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(Status.DELETE_REJECTED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldDeleteApprovedRequestedRoleForCase_S022() {
        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.LEGAL_OPERATIONS, "tribunal-caseworker",
                                                             SPECIFIC, "caseId",
                                                             "1234567890123456", DELETE_REQUESTED);
        assignmentRequest.setRequestedRoles(List.of(requestedRole1));
        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        executeDroolRules(List.of(buildExistingRoleForIAC(requestedRole1.getActorId(),
                                                          "tribunal-caseworker",
                                                          RoleCategory.LEGAL_OPERATIONS),
                                  buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
                                                          "senior-tribunal-caseworker",
                                                          RoleCategory.LEGAL_OPERATIONS)));

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(Status.DELETE_APPROVED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldRejectRequestedRoleForDelete_WrongExistingRoleID_S023() {
        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.LEGAL_OPERATIONS, "tribunal-caseworker",
                                                             SPECIFIC, "caseId",
                                                             "1234567890123456", DELETE_REQUESTED);

        assignmentRequest.setRequestedRoles(List.of(requestedRole1));
        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        executeDroolRules(List.of(buildExistingRoleForIAC(requestedRole1.getActorId(),
                                                          "senior-tribunal-caseworker",
                                                          RoleCategory.LEGAL_OPERATIONS),
                                  buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId() + "23",
                                                          "tribunal-caseworker",
                                                          RoleCategory.LEGAL_OPERATIONS)));
        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(Status.DELETE_REJECTED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldRejectRequestedRoleForDelete_WrongExistingRoleName_S019() {
        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.LEGAL_OPERATIONS, "tribunal-caseworker",
                                                             SPECIFIC, "caseId",
                                                             "1234567890123456", DELETE_REQUESTED);
        assignmentRequest.setRequestedRoles(List.of(requestedRole1));
        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        executeDroolRules(List.of(buildExistingRoleForIAC(requestedRole1.getActorId(),
                                                          "tribunal-caseworker",
                                                          RoleCategory.LEGAL_OPERATIONS),
                                  buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
                                                          "judge",
                                                          RoleCategory.LEGAL_OPERATIONS)));

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(Status.DELETE_REJECTED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldRejectRequestedRoleForDelete_WrongExistingAttributeValue_S020() {
        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.LEGAL_OPERATIONS, "tribunal-caseworker",
                                                             SPECIFIC
        );
        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));
        requestedRole1.setStatus(DELETE_REQUESTED);

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);
        assignmentRequest.setRequestedRoles(requestedRoles);

        ExistingRoleAssignment existingActorAssignment1 = buildExistingRoleForIAC(
            requestedRole1.getActorId(),
            "tribunal-caseworker",
            RoleCategory.LEGAL_OPERATIONS
        );

        ExistingRoleAssignment existingRequesterAssignment1 = buildExistingRoleForIAC(
            assignmentRequest.getRequest()
                .getAssignerId(),
            "senior-tribunal-caseworker",
            RoleCategory.LEGAL_OPERATIONS
        );
        existingRequesterAssignment1.getAttributes().put("jurisdiction", convertValueJsonNode("CMC"));

        List<ExistingRoleAssignment> existingRoleAssignments = new ArrayList<>();
        existingRoleAssignments.add(existingActorAssignment1);
        existingRoleAssignments.add(existingRequesterAssignment1);
        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        // facts must contain the request
        executeDroolRules(existingRoleAssignments);

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(Status.DELETE_REJECTED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldApprovedCaseValidationForTCW_ForAssignee2STCW_RequesterTCW_S024() {
        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.LEGAL_OPERATIONS, "tribunal-caseworker",
                                                             SPECIFIC, "caseId",
                                                             "1234567890123456", CREATE_REQUESTED);
        assignmentRequest.setRequestedRoles(List.of(requestedRole1));

        RoleAssignment requestedRole2 = getRequestedCaseRole(RoleCategory.LEGAL_OPERATIONS, "tribunal-caseworker",
                                                             SPECIFIC, "caseId",
                                                             "1234567890123456", CREATE_REQUESTED);
        assignmentRequest.setRequestedRoles(List.of(requestedRole2));
        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        executeDroolRules(List.of(buildExistingRoleForIAC(requestedRole1.getActorId(),
                                                          "tribunal-caseworker",
                                                          RoleCategory.LEGAL_OPERATIONS),
                                  buildExistingRoleForIAC(requestedRole2.getActorId(),
                                                          "senior-tribunal-caseworker",
                                                          RoleCategory.LEGAL_OPERATIONS),
                                  buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
                                                          "tribunal-caseworker",
                                                          RoleCategory.LEGAL_OPERATIONS)));

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(Status.APPROVED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldApprovedCaseValidationForTCW_RequesterSTCW_S002() {
        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.LEGAL_OPERATIONS, "tribunal-caseworker",
                                                             SPECIFIC, "caseId",
                                                             "1234567890123456", CREATE_REQUESTED);
        assignmentRequest.setRequestedRoles(List.of(requestedRole1));
        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        executeDroolRules(List.of(buildExistingRoleForIAC(requestedRole1.getActorId(),
                                                          "tribunal-caseworker",
                                                          RoleCategory.LEGAL_OPERATIONS),
                                  buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
                                                          "senior-tribunal-caseworker",
                                                          RoleCategory.LEGAL_OPERATIONS)));

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(Status.APPROVED, roleAssignment.getStatus());
            assertEquals("Y", roleAssignment.getAttributes().get("substantive").asText());
        });
    }

    @ParameterizedTest
    @CsvSource({
        "tribunal-caseworker, senior-tribunal-caseworker, judge",
        "judge, judge, senior-tribunal-caseworker",
        "tribunal-caseworker, senior-tribunal-caseworker, judge",
    })
    void shouldRejectCaseValidationForTCW_RequesterJudge_S004A(String role1, String role2, String role3) {
        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.LEGAL_OPERATIONS, role1,
                                                             SPECIFIC, "caseId",
                                                             "1234567890123456", CREATE_REQUESTED);
        assignmentRequest.setRequestedRoles(List.of(requestedRole1));

        executeDroolRules(List.of(buildExistingRoleForIAC(requestedRole1.getActorId(),
                                                          role2, RoleCategory.LEGAL_OPERATIONS),
                                  buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
                                                          role3, RoleCategory.LEGAL_OPERATIONS)));

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(Status.REJECTED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldRejectCaseValidationForTCW_RequesterSCTW_WrongAssignerID_S005() {
        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.LEGAL_OPERATIONS,
                                                             "tribunal-caseworker",
                                                             SPECIFIC, "caseId",
                                                             "1234567890123456", CREATE_REQUESTED);
        assignmentRequest.setRequestedRoles(List.of(requestedRole1));

        executeDroolRules(List.of(buildExistingRoleForIAC(requestedRole1.getActorId(),
                                                          "tribunal-caseworker",
                                                          RoleCategory.LEGAL_OPERATIONS),
                                  buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId() + "12",
                                                          "senior-tribunal-caseworker",
                                                          RoleCategory.LEGAL_OPERATIONS)));

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(Status.REJECTED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldRejectCaseValidationForTCW_RequesterSCTW_WrongRoleCategory_S006() {
        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.JUDICIAL, "tribunal-caseworker",
                                                             SPECIFIC, "caseId",
                                                             "1234567890123456", CREATE_REQUESTED);
        assignmentRequest.setRequestedRoles(List.of(requestedRole1));

        executeDroolRules(List.of(buildExistingRoleForIAC(requestedRole1.getActorId(),
                                                          "tribunal-caseworker",
                                                          RoleCategory.LEGAL_OPERATIONS),
                                  buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
                                                          "senior-tribunal-caseworker",
                                                          RoleCategory.LEGAL_OPERATIONS)));

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(Status.REJECTED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldRejectCaseValidationForTCW_RequesterSCTW_MissingCaseID_S007() {
        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.LEGAL_OPERATIONS, "tribunal-caseworker",
                                                             SPECIFIC);
        assignmentRequest.setRequestedRoles(List.of(requestedRole1));

        executeDroolRules(List.of(buildExistingRoleForIAC(requestedRole1.getActorId(),
                                                          "tribunal-caseworker",
                                                          RoleCategory.LEGAL_OPERATIONS),
                                  buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
                                                          "senior-tribunal-caseworker",
                                                          RoleCategory.LEGAL_OPERATIONS)));

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(Status.REJECTED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldRejectCaseValidationForTCW_RequesterSCTW_WrongCaseType_S009() {
        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.LEGAL_OPERATIONS, "tribunal-caseworker",
                                                             SPECIFIC, "caseId",
                                                             "1234567890123457", CREATE_REQUESTED);
        assignmentRequest.setRequestedRoles(List.of(requestedRole1));

        executeDroolRules(List.of(buildExistingRoleForIAC(requestedRole1.getActorId(),
                                                          "tribunal-caseworker",
                                                          RoleCategory.LEGAL_OPERATIONS),
                                  buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
                                                          "senior-tribunal-caseworker",
                                                          RoleCategory.LEGAL_OPERATIONS)));

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment ->
            assertEquals(Status.REJECTED, roleAssignment.getStatus()));
    }

    @Test
    void shouldRejectCaseValidationForTCW_RequesterSCTW_WrongAssigneeJurisdiction_S008() {
        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.LEGAL_OPERATIONS, "tribunal-caseworker",
                                                             SPECIFIC, "caseId",
                                                             "1234567890123458", CREATE_REQUESTED);
        assignmentRequest.setRequestedRoles(List.of(requestedRole1));

        executeDroolRules(List.of(buildExistingRoleForIAC(requestedRole1.getActorId(),
                                                          "tribunal-caseworker",
                                                          RoleCategory.LEGAL_OPERATIONS),
                                  buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
                                                          "senior-tribunal-caseworker",
                                                          RoleCategory.LEGAL_OPERATIONS)));
        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment ->
            assertEquals(Status.REJECTED, roleAssignment.getStatus()));
    }

    @Test
    void shouldRejectCaseValidationForTCW_RequesterSCTW_WithBeginEndTimeForAssignee_S027() {
        ZonedDateTime timeStamp = ZonedDateTime.now(ZoneOffset.UTC);

        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.LEGAL_OPERATIONS, "tribunal-caseworker",
                                                             SPECIFIC, "caseId",
                                                             "1234567890123456", CREATE_REQUESTED);

        requestedRole1.setBeginTime(timeStamp.plusDays(1));
        requestedRole1.setEndTime(timeStamp.plusDays(100));
        assignmentRequest.setRequestedRoles(List.of(requestedRole1));
        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        executeDroolRules(List.of(buildExistingRoleForIAC(requestedRole1.getActorId(),
                                                          "tribunal-caseworker",
                                                          RoleCategory.LEGAL_OPERATIONS),
                                  buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
                                                          "senior-tribunal-caseworker",
                                                          RoleCategory.LEGAL_OPERATIONS)));

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment ->
            assertEquals(Status.APPROVED, roleAssignment.getStatus()));
    }

    @Test
    void shouldAcceptedCaseValidationForTCW_RequesterSCTW_NoBeginEndTimeForAssignee_S026() {
        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.LEGAL_OPERATIONS, "tribunal-caseworker",
                                                             SPECIFIC, "caseId",
                                                             "1234567890123456", CREATE_REQUESTED);
        assignmentRequest.setRequestedRoles(List.of(requestedRole1));
        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        executeDroolRules(List.of(buildExistingRoleForIAC(requestedRole1.getActorId(),
                                                          "tribunal-caseworker",
                                                          RoleCategory.LEGAL_OPERATIONS),
                                  buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
                                                          "senior-tribunal-caseworker",
                                                          RoleCategory.LEGAL_OPERATIONS)));

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment ->
            assertEquals(Status.APPROVED, roleAssignment.getStatus()));
    }

    @Test
    void shouldRejectCaseValidationForTCW_WrongGrantType_S013() {
        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.LEGAL_OPERATIONS, "tribunal-caseworker",
                                                             CHALLENGED, "caseId",
                                                             "1234567890123456", CREATE_REQUESTED);
        assignmentRequest.setRequestedRoles(List.of(requestedRole1));

        executeDroolRules(List.of(buildExistingRoleForIAC(requestedRole1.getActorId(),
                                                          "tribunal-caseworker",
                                                          RoleCategory.LEGAL_OPERATIONS),
                                  buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
                                                          "senior-tribunal-caseworker",
                                                          RoleCategory.LEGAL_OPERATIONS)));

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment ->
            assertEquals(Status.REJECTED, roleAssignment.getStatus()));
    }

    @Test
    void shouldRejectCaseValidationForTCW_RequesterSCTW_MissingExistingRA_S025() {
        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.LEGAL_OPERATIONS, "tribunal-caseworker",
                                                             SPECIFIC, "caseId",
                                                             "1234567890123456", CREATE_REQUESTED);
        assignmentRequest.setRequestedRoles(List.of(requestedRole1));

        executeDroolRules(List.of(buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
                                                          "senior-tribunal-caseworker",
                                                          RoleCategory.LEGAL_OPERATIONS)));

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment ->
            assertEquals(Status.REJECTED, roleAssignment.getStatus()));
    }

    @Test
    void shouldRejectCaseDeleteRequestedRole_WrongAssignerId_S018() {
        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.LEGAL_OPERATIONS,
                                                             "tribunal-caseworker",
                                                             SPECIFIC, "caseId",
                                                             "1234567890123456", DELETE_REQUESTED);
        assignmentRequest.setRequestedRoles(List.of(requestedRole1));

        executeDroolRules(List.of(buildExistingRoleForIAC(requestedRole1.getActorId(),
                                                          "tribunal-caseworker",
                                                          RoleCategory.LEGAL_OPERATIONS),
                                  buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId() + "99",
                                                          "senior-tribunal-caseworker",
                                                          RoleCategory.LEGAL_OPERATIONS)));

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(Status.DELETE_REJECTED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldRejectCaseRequestedRole_ForRequester_WrongRequesterJurisdiction_S021() {

        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.LEGAL_OPERATIONS, "tribunal-caseworker",
                                                             SPECIFIC);
        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);
        assignmentRequest.setRequestedRoles(requestedRoles);

        ExistingRoleAssignment existingActorAssignment1 = buildExistingRoleForIAC(
            requestedRole1.getActorId(),
            "tribunal-caseworker",
            RoleCategory.LEGAL_OPERATIONS
        );

        ExistingRoleAssignment existingRequesterAssignment1 = buildExistingRoleForIAC(
            assignmentRequest.getRequest().getAssignerId(), "tribunal-caseworker",
            RoleCategory.LEGAL_OPERATIONS);

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
    void shouldApprovedCaseValidationForTCW_RequesterSTCW_Version1_1() {
        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.LEGAL_OPERATIONS,
                                                             "tribunal-caseworker",
                                                             SPECIFIC, "caseId",
                                                             "1234567890123456", CREATE_REQUESTED);
        assignmentRequest.setRequestedRoles(List.of(requestedRole1));
        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_1_1.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        executeDroolRules(List.of(buildExistingRoleForIAC(requestedRole1.getActorId(),
                                                          "senior-tribunal-caseworker",
                                                          RoleCategory.LEGAL_OPERATIONS),
                                  buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
                                                          "tribunal-caseworker",
                                                          RoleCategory.LEGAL_OPERATIONS)));
        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(Status.APPROVED, roleAssignment.getStatus());
            assertEquals("Y", roleAssignment.getAttributes().get("substantive").asText());
        });
    }

    @Test
    void shouldDeleteApprovedRequestedRoleForCase_V1_1() {
        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.LEGAL_OPERATIONS,
                                                             "tribunal-caseworker",
                                                             SPECIFIC, "caseId",
                                                             "1234567890123456", DELETE_REQUESTED);
        assignmentRequest.setRequestedRoles(List.of(requestedRole1));
        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_1_1.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        executeDroolRules(List.of(buildExistingRoleForIAC(requestedRole1.getActorId(),
                                                          "tribunal-caseworker",
                                                          RoleCategory.LEGAL_OPERATIONS),
                                  buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
                                                          "senior-tribunal-caseworker",
                                                          RoleCategory.LEGAL_OPERATIONS)));
        buildExecuteKieSession();

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(Status.DELETE_APPROVED, roleAssignment.getStatus());
        });
    }

    @Test
    @DisplayName("Approve the case-manager, case-allocator roles with assigner is case-allocator and actioned by "
        + "parametered user")
    void shouldAcceptCaseRolesCreation_V1_1() {
        verifyCreateCaseRole_V1_1("case-manager","tribunal-caseworker");
        verifyCreateCaseRole_V1_1("case-manager","senior-tribunal-caseworker");
        verifyCreateCaseRole_V1_1("case-allocator","case-allocator");
    }

    @DisplayName("Approve the case-manager, case-allocator roles with assigner is case-allocator and actioned by "
        + "parametered user")
    private void verifyCreateCaseRole_V1_1(String roleName, String existingRole) {
        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.LEGAL_OPERATIONS, roleName,
                                                             SPECIFIC, "caseId",
                                                             "1234567890123456", CREATE_REQUESTED);
        requestedRole1.getAttributes().putAll(Map.of("jurisdiction", convertValueJsonNode("IA"),
                                                     "caseType", convertValueJsonNode("Asylum"),
                                                     "caseId", convertValueJsonNode("1234567890123456")));
        assignmentRequest.setRequestedRoles(List.of(requestedRole1));
        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_1_1.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        executeDroolRules(List.of(buildExistingRoleForIAC(requestedRole1.getActorId(),
                                                          existingRole,
                                                          RoleCategory.LEGAL_OPERATIONS),
                                  buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
                                                          "case-allocator",
                                                          RoleCategory.LEGAL_OPERATIONS)));

        //assertion
        assignmentRequest.getRequestedRoles().forEach(ra -> assertEquals(Status.APPROVED, ra.getStatus()));
    }

    @Test
    @DisplayName("Reject the case-manager, case-allocator roles with disabled IAC_1_1 flag")
    void shouldRejectCaseRolesCreation_disableFlag() {
        verifyCreateCaseRole_V1_0("case-manager", "tribunal-caseworker");
        verifyCreateCaseRole_V1_0("case-manager", "senior-tribunal-caseworker");
        verifyCreateCaseRole_V1_0("case-allocator", "case-allocator");
    }

    private void verifyCreateCaseRole_V1_0(String roleName, String existingRole) {
        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.LEGAL_OPERATIONS, roleName,
                                                             SPECIFIC, "caseId",
                                                             "1234567890123456", CREATE_REQUESTED);
        requestedRole1.getAttributes().putAll(Map.of("jurisdiction", convertValueJsonNode("IA"),
                                                     "caseType", convertValueJsonNode("Asylum"),
                                                     "caseId", convertValueJsonNode("1234567890123456")));
        assignmentRequest.setRequestedRoles(List.of(requestedRole1));
        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        executeDroolRules(List.of(buildExistingRoleForIAC(requestedRole1.getActorId(),
                                                          existingRole,
                                                          RoleCategory.LEGAL_OPERATIONS),
                                  buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
                                                          "case-allocator",
                                                          RoleCategory.LEGAL_OPERATIONS)));

        //assertion
        assignmentRequest.getRequestedRoles().forEach(ra -> assertEquals(Status.REJECTED, ra.getStatus()));
    }

    @Test
    @DisplayName("Reject creation of the case-manager role actioned by neither TCW nor STCW."
        + "expected Actioned by case-allocator")
    void shouldRejectCaseManagerRole_NoTCW_NoSTCW() {

        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.LEGAL_OPERATIONS, "case-manager",
                                                             SPECIFIC, "caseId",
                                                             "1234567890123456", CREATE_REQUESTED);
        requestedRole1.getAttributes().putAll(Map.of("jurisdiction", convertValueJsonNode("IA"),
                                                     "caseType", convertValueJsonNode("Asylum"),
                                                     "caseId", convertValueJsonNode("1234567890123456")));
        assignmentRequest.setRequestedRoles(List.of(requestedRole1));
        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_1_1.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        executeDroolRules(List.of(buildExistingRoleForIAC(requestedRole1.getActorId(),
                                                          "case-allocator",
                                                          RoleCategory.LEGAL_OPERATIONS),
                                  buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
                                                          "case-allocator",
                                                          RoleCategory.LEGAL_OPERATIONS)));

        //assertion
        assignmentRequest.getRequestedRoles().forEach(ra -> assertEquals(Status.REJECTED, ra.getStatus()));
    }

    @Test
    @DisplayName("Reject creation of the case-manager role with no assigner. expected assigner is case-allocator")
    void shouldRejectCaseManagerRoleCreation_NoAssigner() {

        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.LEGAL_OPERATIONS, "case-manager",
                                                             SPECIFIC, "caseId",
                                                             "1234567890123456", CREATE_REQUESTED);
        requestedRole1.getAttributes().putAll(Map.of("jurisdiction", convertValueJsonNode("IA"),
                                                     "caseType", convertValueJsonNode("Asylum"),
                                                     "caseId", convertValueJsonNode("1234567890123456")));
        assignmentRequest.setRequestedRoles(List.of(requestedRole1));
        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_1_1.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        executeDroolRules(List.of());

        //assertion
        assignmentRequest.getRequestedRoles().forEach(ra -> assertEquals(Status.REJECTED, ra.getStatus()));
    }

    @Test
    @DisplayName("Reject creation of the case-allocator role actioned by TCW. expected is case-allocator")
    void shouldRejectCaseAllocatorRoleCreation_actionByTCW() {

        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.LEGAL_OPERATIONS, "case-allocator",
                                                             SPECIFIC, "caseId",
                                                             "1234567890123456", CREATE_REQUESTED);
        requestedRole1.getAttributes().putAll(Map.of("jurisdiction", convertValueJsonNode("IA"),
                                                     "caseType", convertValueJsonNode("Asylum"),
                                                     "caseId", convertValueJsonNode("1234567890123456")));
        assignmentRequest.setRequestedRoles(List.of(requestedRole1));
        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_1_1.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        executeDroolRules(List.of(buildExistingRoleForIAC(requestedRole1.getActorId(),
                                                          "tribunal-caseworker",
                                                          RoleCategory.LEGAL_OPERATIONS),
                                  buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
                                                          "case-allocator",
                                                          RoleCategory.LEGAL_OPERATIONS)));

        //assertion
        assignmentRequest.getRequestedRoles().forEach(ra -> assertEquals(Status.REJECTED, ra.getStatus()));
    }

    @Test
    @DisplayName("Reject creation of the case-allocator role with wrong Category. expected is LEGAL_OPERATIONS")
    void shouldRejectCaseAllocatorRoleCreation_withWrongMappingFields() {

        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.ADMIN, "case-allocator",
                                                             SPECIFIC, "caseId",
                                                             "1234567890123456", CREATE_REQUESTED);
        requestedRole1.getAttributes().putAll(Map.of("jurisdiction", convertValueJsonNode("IA"),
                                                     "caseType", convertValueJsonNode("Asylum"),
                                                     "caseId", convertValueJsonNode("1234567890123456")));
        assignmentRequest.setRequestedRoles(List.of(requestedRole1));
        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_1_1.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        executeDroolRules(List.of(buildExistingRoleForIAC(requestedRole1.getActorId(),
                                                          "case-allocator",
                                                          RoleCategory.LEGAL_OPERATIONS),
                                  buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
                                                          "case-allocator",
                                                          RoleCategory.LEGAL_OPERATIONS)));

        //assertion
        assignmentRequest.getRequestedRoles().forEach(ra -> assertEquals(Status.REJECTED, ra.getStatus()));
    }

    @Test
    @DisplayName("Accept the delete case-manager, case-allocator role Assigner as case-allocator.")
    void shouldAcceptDeleteCaseRoles_V1_1() {
        verifyDeleteCaseRole_V1_1("case-manager");
        verifyDeleteCaseRole_V1_1("case-allocator");
    }

    private void verifyDeleteCaseRole_V1_1(String roleName) {
        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.LEGAL_OPERATIONS, roleName,
                                                             SPECIFIC, "caseId",
                                                             "1234567890123456", DELETE_REQUESTED);
        requestedRole1.getAttributes().putAll(Map.of("jurisdiction", convertValueJsonNode("IA"),
                                                     "caseType", convertValueJsonNode("Asylum"),
                                                     "caseId", convertValueJsonNode("1234567890123456")));
        assignmentRequest.setRequestedRoles(List.of(requestedRole1));
        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_1_1.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        executeDroolRules(List.of(buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
                                                          "case-allocator",
                                                          RoleCategory.LEGAL_OPERATIONS)));

        //assertion
        assignmentRequest.getRequestedRoles().forEach(ra -> assertEquals(Status.DELETE_APPROVED, ra.getStatus()));
    }

    @Test
    @DisplayName("Reject the delete case-allocator with wrong Category.")
    void shouldRejectDeleteCaseAllocatorRoles_withWrongCategory() {
        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.ADMIN, "case-allocator",
                                                             SPECIFIC, "caseId",
                                                             "1234567890123456", DELETE_REQUESTED);
        requestedRole1.getAttributes().putAll(Map.of("jurisdiction", convertValueJsonNode("IA"),
                                                     "caseType", convertValueJsonNode("Asylum"),
                                                     "caseId", convertValueJsonNode("1234567890123456")));
        assignmentRequest.setRequestedRoles(List.of(requestedRole1));
        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_1_1.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        executeDroolRules(List.of(buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
                                                          "case-allocator",
                                                          RoleCategory.LEGAL_OPERATIONS)));

        //assertion
        assignmentRequest.getRequestedRoles().forEach(ra -> assertEquals(Status.DELETE_REJECTED, ra.getStatus()));
    }

    @Test
    @DisplayName("Reject delete the case-manager, case-allocator roles with disabled IAC_1_1 flag")
    void shouldRejectDeleteCaseRoles_V1_0_diabledFlag() {
        verifyDeleteCaseRole_V1_0("case-manager", "tribunal-caseworker");
        verifyDeleteCaseRole_V1_0("case-manager", "senior-tribunal-caseworker");
        verifyDeleteCaseRole_V1_0("case-allocator","case-allocator");
    }

    private void verifyDeleteCaseRole_V1_0(String roleName, String existingRole) {
        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.LEGAL_OPERATIONS, roleName,
                                                             SPECIFIC, "caseId",
                                                             "1234567890123456", DELETE_REQUESTED);
        requestedRole1.getAttributes().putAll(Map.of("jurisdiction", convertValueJsonNode("IA"),
                                                     "caseType", convertValueJsonNode("Asylum"),
                                                     "caseId", convertValueJsonNode("1234567890123456")));
        assignmentRequest.setRequestedRoles(List.of(requestedRole1));
        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        executeDroolRules(List.of(buildExistingRoleForIAC(requestedRole1.getActorId(),
                                                          existingRole,
                                                          RoleCategory.LEGAL_OPERATIONS),
                                  buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
                                                          "case-allocator",
                                                          RoleCategory.LEGAL_OPERATIONS)));

        //assertion
        assignmentRequest.getRequestedRoles().forEach(ra -> assertEquals(Status.DELETE_REJECTED, ra.getStatus()));
    }

    @Test
    @DisplayName("Reject deletion of the case-allocator role without existing Assigner. Expected is case-allocator")
    void shouldRejectDeleteCaseAllocatorRole_V1_1_noExistingAssigner() {
        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.LEGAL_OPERATIONS, "case-allocator",
                                                             SPECIFIC, "caseId",
                                                             "1234567890123456", DELETE_REQUESTED);
        requestedRole1.getAttributes().putAll(Map.of("jurisdiction", convertValueJsonNode("IA"),
                                                     "caseType", convertValueJsonNode("Asylum"),
                                                     "caseId", convertValueJsonNode("1234567890123456")));
        assignmentRequest.setRequestedRoles(List.of(requestedRole1));
        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_1_1.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        executeDroolRules(List.of());

        //assertion
        assignmentRequest.getRequestedRoles().forEach(ra -> assertEquals(Status.DELETE_REJECTED, ra.getStatus()));
    }

    @Test
    @DisplayName("Reject deletion of the case-allocator role with Assigner as TCW. Expected assigned is case-allocator")
    void shouldRejectDeleteCaseAllocatorRole_V1_1_assignerAsTCW() {
        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.LEGAL_OPERATIONS, "case-allocator",
                                                             SPECIFIC, "caseId",
                                                             "1234567890123456", DELETE_REQUESTED);
        requestedRole1.getAttributes().putAll(Map.of("jurisdiction", convertValueJsonNode("IA"),
                                                     "caseType", convertValueJsonNode("Asylum"),
                                                     "caseId", convertValueJsonNode("1234567890123456")));
        assignmentRequest.setRequestedRoles(List.of(requestedRole1));
        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_1_1.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        executeDroolRules(List.of(buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
                                                          "tribunal-caseworker",
                                                          RoleCategory.LEGAL_OPERATIONS)));

        //assertion
        assignmentRequest.getRequestedRoles().forEach(ra -> assertEquals(Status.DELETE_REJECTED, ra.getStatus()));
    }

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
