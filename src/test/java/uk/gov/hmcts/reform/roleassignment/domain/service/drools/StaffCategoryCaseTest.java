package uk.gov.hmcts.reform.roleassignment.domain.service.drools;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.roleassignment.domain.model.ExistingRoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType.CHALLENGED;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType.SPECIFIC;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.DELETE_REQUESTED;
import static uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder.buildExistingRoleForIAC;
import static uk.gov.hmcts.reform.roleassignment.util.JacksonUtils.convertValueJsonNode;

@RunWith(MockitoJUnitRunner.class)
class StaffCategoryCaseTest extends DroolBase {


    @Test
    void shouldApproveCaseRequestedRoles_RequesterOrgRoleTCW_S001() {
        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.STAFF, "tribunal-caseworker",
                                                             SPECIFIC
        );
        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);
        assignmentRequest.setRequestedRoles(requestedRoles);

        ExistingRoleAssignment existingActorAssignment1 = buildExistingRoleForIAC(
            requestedRole1.getActorId(),
            "senior-tribunal-caseworker"
        );
        ExistingRoleAssignment existingRequesterAssignment2 = buildExistingRoleForIAC(
            assignmentRequest.getRequest().getAssignerId(), "tribunal-caseworker");

        List<ExistingRoleAssignment> existingRoleAssignments = new ArrayList<>();
        existingRoleAssignments.add(existingActorAssignment1);
        existingRoleAssignments.add(existingRequesterAssignment2);

        // facts must contain the request
        facts.add(assignmentRequest.getRequest());

        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        // facts must contain all existing role assignments
        facts.addAll(existingRoleAssignments);

        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(ra -> {
            assertEquals(Status.APPROVED, ra.getStatus());
        });
    }

    @Test
    void shouldRejectCaseRequestedRole_MissingExistingRoleOfRequester_S017() {
        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.STAFF, "tribunal-caseworker",
                                                             SPECIFIC
        );
        requestedRole1.setStatus(DELETE_REQUESTED);
        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);
        assignmentRequest.setRequestedRoles(requestedRoles);

        // facts must contain the request
        facts.add(assignmentRequest.getRequest());

        // facts must contain all requested role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        //No existing record for requester, it is not added in fact.

        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(Status.DELETE_REJECTED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldDeleteApprovedRequestedRoleForCase_S022() {
        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.STAFF, "tribunal-caseworker",
                                                             SPECIFIC
        );
        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));
        requestedRole1.setStatus(Status.DELETE_REQUESTED);

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);
        assignmentRequest.setRequestedRoles(requestedRoles);

        // facts must contain the request
        facts.add(assignmentRequest.getRequest());

        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        //facts must contain existing role of requester
        facts.add(buildExistingRoleForIAC(
            assignmentRequest.getRequest().getAssignerId(),
            "senior-tribunal-caseworker"
        ));

        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(Status.DELETE_APPROVED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldRejectRequestedRoleForDelete_WrongExistingRoleID_S023() {
        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.STAFF, "tribunal-caseworker",
                                                             SPECIFIC
        );
        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));
        requestedRole1.setStatus(Status.DELETE_REQUESTED);

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);
        assignmentRequest.setRequestedRoles(requestedRoles);

        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        //facts must contain existing role of requester
        facts.add(buildExistingRoleForIAC(
            requestedRole1.getActorId() + "98",
            "senior-tribunal-caseworker"
        ));

        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(Status.DELETE_REJECTED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldRejectRequestedRoleForDelete_WrongExistingRoleName_S019() {
        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.STAFF, "tribunal-caseworker",
                                                             SPECIFIC
        );
        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));
        requestedRole1.setStatus(Status.DELETE_REQUESTED);

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);
        assignmentRequest.setRequestedRoles(requestedRoles);

        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        //facts must contain existing role of requester
        facts.add(buildExistingRoleForIAC(
            assignmentRequest.getRequest().getAssignerId(),
            "judge"
        ));

        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(Status.DELETE_REJECTED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldRejectRequestedRoleForDelete_WrongExistingAttributeValue_S020() {
        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.STAFF, "tribunal-caseworker",
                                                             SPECIFIC
        );
        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));
        requestedRole1.setStatus(Status.DELETE_REQUESTED);

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);
        assignmentRequest.setRequestedRoles(requestedRoles);

        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        ExistingRoleAssignment existingRequesterAssignment1 = buildExistingRoleForIAC(
            assignmentRequest.getRequest()
                .getAssignerId(),
            "senior-tribunal-caseworker"
        );
        existingRequesterAssignment1.getAttributes().put("jurisdiction", convertValueJsonNode("CMC"));
        //facts must contain existing role of requester
        facts.add(existingRequesterAssignment1);

        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(Status.DELETE_REJECTED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldApprovedCaseValidationForTCW_ForAssignee2STCW_RequesterTCW_S024() {
        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.STAFF, "tribunal-caseworker",
                                                             SPECIFIC
        );
        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

        RoleAssignment requestedRole2 = getRequestedCaseRole(RoleCategory.STAFF, "tribunal-caseworker",
                                                             SPECIFIC
        );
        requestedRole2.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);
        requestedRoles.add(requestedRole2);
        assignmentRequest.setRequestedRoles(requestedRoles);

        ExistingRoleAssignment existingRoleAssignment1 = buildExistingRoleForIAC(
            requestedRole1.getActorId(),
            "tribunal-caseworker"
        );
        ExistingRoleAssignment existingRoleAssignment2 = buildExistingRoleForIAC(
            requestedRole2.getActorId(),
            "senior-tribunal-caseworker"
        );
        ExistingRoleAssignment existingRoleAssignment3 = buildExistingRoleForIAC(
            assignmentRequest.getRequest().getAssignerId(), "tribunal-caseworker");

        List<ExistingRoleAssignment> existingRoleAssignments = new ArrayList<>();
        existingRoleAssignments.add(existingRoleAssignment1);
        existingRoleAssignments.add(existingRoleAssignment2);
        existingRoleAssignments.add(existingRoleAssignment3);
        executeDroolRules(existingRoleAssignments);

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(Status.APPROVED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldApprovedCaseValidationForTCW_RequesterSTCW_S002() {
        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.STAFF, "tribunal-caseworker",
                                                             SPECIFIC
        );
        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);
        assignmentRequest.setRequestedRoles(requestedRoles);

        ExistingRoleAssignment existingActorAssignment1 = buildExistingRoleForIAC(
            requestedRole1.getActorId(),
            "tribunal-caseworker"
        );

        ExistingRoleAssignment existingRequesterAssignment2 = buildExistingRoleForIAC(
            assignmentRequest.getRequest().getAssignerId(), "senior-tribunal-caseworker");

        List<ExistingRoleAssignment> existingRoleAssignments = new ArrayList<>();
        existingRoleAssignments.add(existingActorAssignment1);
        existingRoleAssignments.add(existingRequesterAssignment2);

        // facts must contain all affected role assignments
        executeDroolRules(existingRoleAssignments);

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(Status.APPROVED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldRejectCaseValidationForTCW_RequesterJudge_S004A() {
        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.STAFF, "tribunal-caseworker",
                                                             SPECIFIC
        );
        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);
        assignmentRequest.setRequestedRoles(requestedRoles);

        ExistingRoleAssignment existingActorAssignment1 = buildExistingRoleForIAC(
            requestedRole1.getActorId(),
            "tribunal-caseworker"
        );

        ExistingRoleAssignment existingRequesterAssignment1 = buildExistingRoleForIAC(
            assignmentRequest.getRequest().getAssignerId(), "judge");

        List<ExistingRoleAssignment> existingRoleAssignments = new ArrayList<>();
        existingRoleAssignments.add(existingActorAssignment1);
        existingRoleAssignments.add(existingRequesterAssignment1);

        // facts must contain all affected role assignments
        executeDroolRules(existingRoleAssignments);

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(Status.REJECTED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldRejectCaseValidationForAssigneeJudge_RequesterSTCW_S004() {
        RoleAssignment requestedRole2 = getRequestedCaseRole(RoleCategory.STAFF, "tribunal-caseworker",
                                                             SPECIFIC
        );
        requestedRole2.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole2);
        assignmentRequest.setRequestedRoles(requestedRoles);

        ExistingRoleAssignment existingActorAssignment1 = buildExistingRoleForIAC(
            requestedRole2.getActorId(),
            "judge"
        );
        ExistingRoleAssignment existingRequesterAssignment1 = buildExistingRoleForIAC(
            assignmentRequest.getRequest().getAssignerId(), "senior-tribunal-caseworker");

        List<ExistingRoleAssignment> existingRoleAssignments = new ArrayList<>();
        existingRoleAssignments.add(existingActorAssignment1);
        existingRoleAssignments.add(existingRequesterAssignment1);

        // facts must contain all affected role assignments
        executeDroolRules(existingRoleAssignments);

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment ->
                                                          assertEquals(Status.REJECTED, roleAssignment.getStatus()));
    }

    @Test
    void shouldRejectCaseValidationForTCW_RequesterSCTW_WrongAssignerID_S005() {
        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.STAFF, "tribunal-caseworker",
                                                             SPECIFIC
        );
        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);
        assignmentRequest.setRequestedRoles(requestedRoles);

        ExistingRoleAssignment existingActorAssignment1 = buildExistingRoleForIAC(
            requestedRole1.getActorId(),
            "tribunal-caseworker"
        );

        ExistingRoleAssignment existingRequesterAssignment1 = buildExistingRoleForIAC(
            assignmentRequest.getRequest().getAssignerId(), "senior-tribunal-caseworker");

        List<ExistingRoleAssignment> existingRoleAssignments = new ArrayList<>();
        existingRoleAssignments.add(existingActorAssignment1);
        existingRoleAssignments.add(existingRequesterAssignment1);

        //assign incorrect assigner id
        assignmentRequest.getRequest().setAssignerId(requestedRole1.getRoleName() + requestedRole1.getActorId());

        // facts must contain all affected role assignments
        executeDroolRules(existingRoleAssignments);

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment ->
                                                          assertEquals(Status.REJECTED, roleAssignment.getStatus()));
    }

    @Test
    void shouldRejectCaseValidationForTCW_RequesterSCTW_WrongRoleCategory_S006() {
        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.JUDICIAL, "tribunal-caseworker",
                                                             SPECIFIC
        );
        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);
        assignmentRequest.setRequestedRoles(requestedRoles);

        ExistingRoleAssignment existingActorAssignment1 = buildExistingRoleForIAC(
            requestedRole1.getActorId(),
            "tribunal-caseworker"
        );

        ExistingRoleAssignment existingRequesterAssignment1 = buildExistingRoleForIAC(
            assignmentRequest.getRequest().getAssignerId(), "senior-tribunal-caseworker");

        List<ExistingRoleAssignment> existingRoleAssignments = new ArrayList<>();
        existingRoleAssignments.add(existingActorAssignment1);
        existingRoleAssignments.add(existingRequesterAssignment1);

        // facts must contain all affected role assignments
        executeDroolRules(existingRoleAssignments);

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment ->
                                                          assertEquals(Status.REJECTED, roleAssignment.getStatus()));
    }

    @Test
    void shouldRejectCaseValidationForTCW_RequesterSCTW_MissingCaseID_S007() {
        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.STAFF, "tribunal-caseworker",
                                                             SPECIFIC
        );

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);
        assignmentRequest.setRequestedRoles(requestedRoles);

        ExistingRoleAssignment existingActorAssignment1 = buildExistingRoleForIAC(
            requestedRole1.getActorId(),
            "tribunal-caseworker"
        );

        ExistingRoleAssignment existingRequesterAssignment1 = buildExistingRoleForIAC(
            assignmentRequest.getRequest().getAssignerId(), "senior-tribunal-caseworker");

        List<ExistingRoleAssignment> existingRoleAssignments = new ArrayList<>();
        existingRoleAssignments.add(existingActorAssignment1);
        existingRoleAssignments.add(existingRequesterAssignment1);

        // facts must contain all affected role assignments
        executeDroolRules(existingRoleAssignments);

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment ->
                                                          assertEquals(Status.REJECTED, roleAssignment.getStatus()));
    }

    @Test
    void shouldRejectCaseValidationForTCW_RequesterSCTW_WrongCaseType_S009() {
        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.STAFF, "tribunal-caseworker",
                                                             SPECIFIC
        );
        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123457"));

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);
        assignmentRequest.setRequestedRoles(requestedRoles);

        ExistingRoleAssignment existingActorAssignment1 = buildExistingRoleForIAC(
            requestedRole1.getActorId(),
            "tribunal-caseworker"
        );

        ExistingRoleAssignment existingRequesterAssignment1 = buildExistingRoleForIAC(
            assignmentRequest.getRequest().getAssignerId(), "senior-tribunal-caseworker");

        List<ExistingRoleAssignment> existingRoleAssignments = new ArrayList<>();
        existingRoleAssignments.add(existingActorAssignment1);
        existingRoleAssignments.add(existingRequesterAssignment1);

        // facts must contain all affected role assignments
        executeDroolRules(existingRoleAssignments);

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment ->
                                                          assertEquals(Status.REJECTED, roleAssignment.getStatus()));
    }

    @Test
    void shouldRejectCaseValidationForTCW_RequesterSCTW_WrongAssigneeJurisdiction_S008() {
        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.STAFF, "tribunal-caseworker",
                                                             SPECIFIC
        );
        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123458"));

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);
        assignmentRequest.setRequestedRoles(requestedRoles);

        ExistingRoleAssignment existingActorAssignment1 = buildExistingRoleForIAC(
            requestedRole1.getActorId(),
            "tribunal-caseworker"
        );

        ExistingRoleAssignment existingRequesterAssignment1 = buildExistingRoleForIAC(
            assignmentRequest.getRequest().getAssignerId(), "senior-tribunal-caseworker");

        List<ExistingRoleAssignment> existingRoleAssignments = new ArrayList<>();
        existingRoleAssignments.add(existingActorAssignment1);
        existingRoleAssignments.add(existingRequesterAssignment1);

        // facts must contain all affected role assignments
        executeDroolRules(existingRoleAssignments);

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment ->
                                                          assertEquals(Status.REJECTED, roleAssignment.getStatus()));
    }

    @Test
    void shouldRejectCaseValidationForTCW_RequesterSCTW_WithBeginEndTimeForAssignee_S027() {
        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.STAFF, "tribunal-caseworker",
                                                             SPECIFIC
        );
        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));
        ZonedDateTime timeStamp = ZonedDateTime.now(ZoneOffset.UTC);
        requestedRole1.setBeginTime(timeStamp.plusDays(1));
        requestedRole1.setEndTime(timeStamp.plusDays(100));

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);
        assignmentRequest.setRequestedRoles(requestedRoles);

        //Testing begin time by not adding existing role assignment for assigner 1
        ExistingRoleAssignment existingActorAssignment1 = buildExistingRoleForIAC(
            requestedRole1.getActorId(),
            "tribunal-caseworker"
        );

        ExistingRoleAssignment existingRequesterAssignment1 = buildExistingRoleForIAC(
            assignmentRequest.getRequest().getAssignerId(), "senior-tribunal-caseworker");

        List<ExistingRoleAssignment> existingRoleAssignments = new ArrayList<>();
        existingRoleAssignments.add(existingActorAssignment1);
        existingRoleAssignments.add(existingRequesterAssignment1);

        // facts must contain all affected role assignments
        executeDroolRules(existingRoleAssignments);

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment ->
                                                          assertEquals(Status.APPROVED, roleAssignment.getStatus()));
    }

    @Test
    void shouldAcceptedCaseValidationForTCW_RequesterSCTW_NoBeginEndTimeForAssignee_S026() {

        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.STAFF, "tribunal-caseworker",
                                                             SPECIFIC
        );
        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);
        assignmentRequest.setRequestedRoles(requestedRoles);

        //Testing begin time by not adding existing role assignment for assignee1
        ExistingRoleAssignment existingActorAssignment1 = buildExistingRoleForIAC(
            requestedRole1.getActorId(),
            "tribunal-caseworker"
        );

        ExistingRoleAssignment existingRequesterAssignment1 = buildExistingRoleForIAC(
            assignmentRequest.getRequest().getAssignerId(), "senior-tribunal-caseworker");

        List<ExistingRoleAssignment> existingRoleAssignments = new ArrayList<>();
        existingRoleAssignments.add(existingActorAssignment1);
        existingRoleAssignments.add(existingRequesterAssignment1);

        // facts must contain all affected role assignments
        executeDroolRules(existingRoleAssignments);

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment ->
                                                          assertEquals(Status.APPROVED, roleAssignment.getStatus()));
    }

    @Test
    void shouldRejectCaseValidationForTCW_WrongGrantType_S013() {
        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.STAFF, "tribunal-caseworker",
                                                             CHALLENGED
        );
        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);
        assignmentRequest.setRequestedRoles(requestedRoles);

        ExistingRoleAssignment existingActorAssignment1 = buildExistingRoleForIAC(
            requestedRole1.getActorId(),
            "tribunal-caseworker"
        );

        ExistingRoleAssignment existingRequesterAssignment1 = buildExistingRoleForIAC(
            assignmentRequest.getRequest().getAssignerId(), "senior-tribunal-caseworker");

        List<ExistingRoleAssignment> existingRoleAssignments = new ArrayList<>();
        existingRoleAssignments.add(existingActorAssignment1);
        existingRoleAssignments.add(existingRequesterAssignment1);

        // facts must contain all affected role assignments
        executeDroolRules(existingRoleAssignments);

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment ->
                                                          assertEquals(Status.REJECTED, roleAssignment.getStatus()));
    }

    @Test
    void shouldRejectCaseValidationForTCW_RequesterSCTW_MissingExistingRA_S025() {
        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.STAFF, "tribunal-caseworker",
                                                             SPECIFIC
        );
        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);
        assignmentRequest.setRequestedRoles(requestedRoles);

        ExistingRoleAssignment existingRequesterAssignment1 = buildExistingRoleForIAC(
            assignmentRequest.getRequest().getAssignerId(), "senior-tribunal-caseworker");

        List<ExistingRoleAssignment> existingRoleAssignments = new ArrayList<>();
        existingRoleAssignments.add(existingRequesterAssignment1);

        // facts must contain all affected role assignments
        executeDroolRules(existingRoleAssignments);

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment ->
                                                          assertEquals(Status.REJECTED, roleAssignment.getStatus()));
    }

    @Test
    void shouldRejectCaseDeleteRequestedRole_WrongAssignerId_S018() {
        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.STAFF, "tribunal-caseworker",
                                                             SPECIFIC
        );
        requestedRole1.setStatus(DELETE_REQUESTED);
        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);
        assignmentRequest.setRequestedRoles(requestedRoles);

        // facts must contain the request
        facts.add(assignmentRequest.getRequest());

        // facts must contain all requested role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        //assign incorrect assigner id
        assignmentRequest.getRequest().setAssignerId(requestedRole1.getRoleName() + requestedRole1.getActorId());
        //facts must contain existing role of requester
        facts.add(assignmentRequest);

        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(Status.DELETE_REJECTED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldRejectCaseRequestedRole_ForRequester_WrongRequesterJurisdiction_S0021() {
        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.STAFF, "tribunal-caseworker",
                                                             SPECIFIC
        );
        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);
        assignmentRequest.setRequestedRoles(requestedRoles);

        ExistingRoleAssignment existingActorAssignment1 = buildExistingRoleForIAC(
            requestedRole1.getActorId(),
            "tribunal-caseworker"
        );

        ExistingRoleAssignment existingRequesterAssignment1 = buildExistingRoleForIAC(
            assignmentRequest.getRequest().getAssignerId(), "tribunal-caseworker");

        existingRequesterAssignment1.getAttributes().put("jurisdiction", convertValueJsonNode("CMC"));

        List<ExistingRoleAssignment> existingRoleAssignments = new ArrayList<>();
        existingRoleAssignments.add(existingActorAssignment1);
        existingRoleAssignments.add(existingRequesterAssignment1);

        // facts must contain the request
        facts.add(assignmentRequest.getRequest());

        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        // facts must contain all existing role assignments
        facts.addAll(existingRoleAssignments);

        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(Status.REJECTED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldRejectCaseRequestedRole_ForRequester_WrongRoleName_S003() {
        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.STAFF, "tribunal-caseworker",
                                                             SPECIFIC
        );
        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);
        assignmentRequest.setRequestedRoles(requestedRoles);

        ExistingRoleAssignment existingActorAssignment1 = buildExistingRoleForIAC(
            requestedRole1.getActorId(),
            "tribunal-caseworker"
        );

        ExistingRoleAssignment existingRequesterAssignment1 = buildExistingRoleForIAC(
            assignmentRequest.getRequest().getAssignerId(), "judge");

        List<ExistingRoleAssignment> existingRoleAssignments = new ArrayList<>();
        existingRoleAssignments.add(existingActorAssignment1);
        existingRoleAssignments.add(existingRequesterAssignment1);

        // facts must contain the request
        facts.add(assignmentRequest.getRequest());

        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        // facts must contain all existing role assignments
        facts.addAll(existingRoleAssignments);

        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(Status.REJECTED, roleAssignment.getStatus());
        });
    }

    private void executeDroolRules(List<ExistingRoleAssignment> existingRoleAssignments) {
        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        // facts must contain all existing role assignments
        facts.addAll(existingRoleAssignments);

        // facts must contain the request
        facts.add(assignmentRequest.getRequest());

        // Run the rules
        kieSession.execute(facts);
    }
}
