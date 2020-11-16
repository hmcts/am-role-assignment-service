package uk.gov.hmcts.reform.roleassignment.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.roleassignment.domain.model.Case;
import uk.gov.hmcts.reform.roleassignment.domain.model.ExistingRoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.RetrieveDataService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType.CHALLENGED;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType.SPECIFIC;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.DELETE_REQUESTED;
import static uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder.buildExistingRoleForIAC;
import static uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder.getRequestedCaseRole;
import static uk.gov.hmcts.reform.roleassignment.util.JacksonUtils.convertValueJsonNode;

@RunWith(MockitoJUnitRunner.class)
class StaffCategoryCaseTest extends DroolBase {

    @Mock
    private final RetrieveDataService retrieveDataService = mock(RetrieveDataService.class);

    @Test
    void shouldApproveCaseRequestedRoles_RequesterOrgRoleTCW() {

        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.STAFF,"tribunal-caseworker",
                                                              SPECIFIC);
        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

        RoleAssignment requestedRole2 = getRequestedCaseRole(RoleCategory.STAFF,"tribunal-caseworker",
                                                              SPECIFIC);
        requestedRole2.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);
        requestedRoles.add(requestedRole2);
        assignmentRequest.setRequestedRoles(requestedRoles);

        ExistingRoleAssignment existingRoleAssignment1 = buildExistingRoleForIAC(requestedRole1.getActorId(),
                                                                                 "tribunal-caseworker");
        ExistingRoleAssignment existingRoleAssignment2 = buildExistingRoleForIAC(requestedRole2.getActorId(),
                                                                                 "tribunal-caseworker");
        ExistingRoleAssignment existingRoleAssignment3 = buildExistingRoleForIAC(
            assignmentRequest.getRequest().getAssignerId(),"senior-tribunal-caseworker");

        List<ExistingRoleAssignment> existingRoleAssignments = new ArrayList<>();
        existingRoleAssignments.add(existingRoleAssignment1);
        existingRoleAssignments.add(existingRoleAssignment2);
        existingRoleAssignments.add(existingRoleAssignment3);

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
    void shouldRejectCaseRequestedRole_MissingExistingRoleOfRequester() {

        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.STAFF,"tribunal-caseworker",
                                                             SPECIFIC);
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
    void shouldDeleteApprovedRequestedRoleForCase() {

        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.STAFF,"tribunal-caseworker",
                                                             SPECIFIC);
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
        facts.add(buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
            "senior-tribunal-caseworker"));

        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(Status.DELETE_APPROVED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldRejectRequestedRoleForDelete_WrongExistingRoleID() {

        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.STAFF,"tribunal-caseworker",
                                                             SPECIFIC);
        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));
        requestedRole1.setStatus(Status.DELETE_REQUESTED);

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);
        assignmentRequest.setRequestedRoles(requestedRoles);

        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        //facts must contain existing role of requester
        facts.add(buildExistingRoleForIAC(requestedRole1.getActorId() + "98",
                                          "senior-tribunal-caseworker"));

        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(Status.DELETE_REJECTED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldRejectRequestedRoleForDelete_WrongExistingRoleName() {

        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.STAFF,"tribunal-caseworker",
                                                             SPECIFIC);
        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));
        requestedRole1.setStatus(Status.DELETE_REQUESTED);

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);
        assignmentRequest.setRequestedRoles(requestedRoles);

        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        //facts must contain existing role of requester
        facts.add(buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
                                          "judge"));

        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(Status.DELETE_REJECTED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldRejectRequestedRoleForDelete_WrongExistingAttributeValue() {

        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.STAFF,"tribunal-caseworker",
                                                             SPECIFIC);
        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));
        requestedRole1.setStatus(Status.DELETE_REQUESTED);

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);
        assignmentRequest.setRequestedRoles(requestedRoles);

        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        ExistingRoleAssignment existingRoleAssignment1 = buildExistingRoleForIAC(assignmentRequest.getRequest()
                                                                                     .getAssignerId(),
                                "senior-tribunal-caseworker");
        existingRoleAssignment1.getAttributes().put("jurisdiction",convertValueJsonNode("CMC"));
        //facts must contain existing role of requester
        facts.add(existingRoleAssignment1);

        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(Status.DELETE_REJECTED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldApprovedCaseValidationForTCW_ForAssignee2STCW_RequesterTCW() {

        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.STAFF,"tribunal-caseworker",
                                                             SPECIFIC);
        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

        RoleAssignment requestedRole2 = getRequestedCaseRole(RoleCategory.STAFF,"tribunal-caseworker",
                                                             SPECIFIC);
        requestedRole2.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);
        requestedRoles.add(requestedRole2);
        assignmentRequest.setRequestedRoles(requestedRoles);

        ExistingRoleAssignment existingRoleAssignment1 = buildExistingRoleForIAC(requestedRole1.getActorId(),
                                                                                 "tribunal-caseworker");
        ExistingRoleAssignment existingRoleAssignment2 = buildExistingRoleForIAC(requestedRole2.getActorId(),
                                                                                 "senior-tribunal-caseworker");
        ExistingRoleAssignment existingRoleAssignment3 = buildExistingRoleForIAC(
            assignmentRequest.getRequest().getAssignerId(),"tribunal-caseworker");

        List<ExistingRoleAssignment> existingRoleAssignments = new ArrayList<>();
        existingRoleAssignments.add(existingRoleAssignment1);
        existingRoleAssignments.add(existingRoleAssignment2);
        existingRoleAssignments.add(existingRoleAssignment3);


        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        // facts must contain all existing role assignments
        facts.addAll(existingRoleAssignments);

        // facts must contain the request
        facts.add(assignmentRequest.getRequest());

        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(Status.APPROVED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldApprovedCaseValidationForTCW_ForAssignee2STCW_RequesterSTCW() {

        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.STAFF,"tribunal-caseworker",
                                                             SPECIFIC);
        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

        RoleAssignment requestedRole2 = getRequestedCaseRole(RoleCategory.STAFF,"tribunal-caseworker",
                                                             SPECIFIC);
        requestedRole2.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);
        requestedRoles.add(requestedRole2);
        assignmentRequest.setRequestedRoles(requestedRoles);

        ExistingRoleAssignment existingRoleAssignment1 = buildExistingRoleForIAC(requestedRole1.getActorId(),
                                                                                 "tribunal-caseworker");
        ExistingRoleAssignment existingRoleAssignment2 = buildExistingRoleForIAC(requestedRole2.getActorId(),
                                                                                 "senior-tribunal-caseworker");
        ExistingRoleAssignment existingRoleAssignment3 = buildExistingRoleForIAC(
            assignmentRequest.getRequest().getAssignerId(),"senior-tribunal-caseworker");

        List<ExistingRoleAssignment> existingRoleAssignments = new ArrayList<>();
        existingRoleAssignments.add(existingRoleAssignment1);
        existingRoleAssignments.add(existingRoleAssignment2);
        existingRoleAssignments.add(existingRoleAssignment3);


        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        // facts must contain all existing role assignments
        facts.addAll(existingRoleAssignments);

        // facts must contain the request
        facts.add(assignmentRequest.getRequest());


        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(Status.APPROVED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldRejectCaseValidationForTCW_ForAssignee2STCW_RequesterJudge() {

        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.STAFF,"tribunal-caseworker",
                                                             SPECIFIC);
        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

        RoleAssignment requestedRole2 = getRequestedCaseRole(RoleCategory.STAFF,"tribunal-caseworker",
                                                             SPECIFIC);
        requestedRole2.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);
        requestedRoles.add(requestedRole2);
        assignmentRequest.setRequestedRoles(requestedRoles);

        ExistingRoleAssignment existingRoleAssignment1 = buildExistingRoleForIAC(requestedRole1.getActorId(),
                                                                                 "tribunal-caseworker");
        ExistingRoleAssignment existingRoleAssignment2 = buildExistingRoleForIAC(requestedRole2.getActorId(),
                                                                                 "senior-tribunal-caseworker");
        ExistingRoleAssignment existingRoleAssignment3 = buildExistingRoleForIAC(
            assignmentRequest.getRequest().getAssignerId(),"judge");

        List<ExistingRoleAssignment> existingRoleAssignments = new ArrayList<>();
        existingRoleAssignments.add(existingRoleAssignment1);
        existingRoleAssignments.add(existingRoleAssignment2);
        existingRoleAssignments.add(existingRoleAssignment3);


        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        // facts must contain all existing role assignments
        facts.addAll(existingRoleAssignments);

        // facts must contain the request
        facts.add(assignmentRequest.getRequest());

        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(Status.REJECTED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldRejectCaseValidationForTCW_ForAssignee2Judge_RequesterSTCW() {

        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.STAFF,"tribunal-caseworker",
                                                             SPECIFIC);
        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

        RoleAssignment requestedRole2 = getRequestedCaseRole(RoleCategory.STAFF,"tribunal-caseworker",
                                                             SPECIFIC);
        requestedRole2.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);
        requestedRoles.add(requestedRole2);
        assignmentRequest.setRequestedRoles(requestedRoles);

        ExistingRoleAssignment existingRoleAssignment1 = buildExistingRoleForIAC(requestedRole1.getActorId(),
                                                                                 "tribunal-caseworker");
        ExistingRoleAssignment existingRoleAssignment2 = buildExistingRoleForIAC(requestedRole2.getActorId(),
                                                                                 "judge");
        ExistingRoleAssignment existingRoleAssignment3 = buildExistingRoleForIAC(
            assignmentRequest.getRequest().getAssignerId(),"senior-tribunal-caseworker");

        List<ExistingRoleAssignment> existingRoleAssignments = new ArrayList<>();
        existingRoleAssignments.add(existingRoleAssignment1);
        existingRoleAssignments.add(existingRoleAssignment2);
        existingRoleAssignments.add(existingRoleAssignment3);


        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        // facts must contain all existing role assignments
        facts.addAll(existingRoleAssignments);

        // facts must contain the request
        facts.add(assignmentRequest.getRequest());

        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().stream()
            .filter(roleAssignment -> roleAssignment.getActorId().equals(requestedRole2.getActorId()))
            .forEach(roleAssignment -> {
                assertNotEquals(Status.APPROVED, roleAssignment.getStatus());
            });
    }

    @Test
    void shouldRejectCaseValidationForTCW_ForAssignee2STCW_RequesterSCTW_WrongAssignerID() {

        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.STAFF,"tribunal-caseworker",
                                                             SPECIFIC);
        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

        RoleAssignment requestedRole2 = getRequestedCaseRole(RoleCategory.STAFF,"tribunal-caseworker",
                                                             SPECIFIC);
        requestedRole2.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);
        requestedRoles.add(requestedRole2);
        assignmentRequest.setRequestedRoles(requestedRoles);

        ExistingRoleAssignment existingRoleAssignment1 = buildExistingRoleForIAC(requestedRole1.getActorId(),
                                                                                 "tribunal-caseworker");
        ExistingRoleAssignment existingRoleAssignment2 = buildExistingRoleForIAC(requestedRole2.getActorId(),
                                                                                 "senior-tribunal-caseworker");
        ExistingRoleAssignment existingRoleAssignment3 = buildExistingRoleForIAC(
            assignmentRequest.getRequest().getAssignerId(),"senior-tribunal-caseworker");

        List<ExistingRoleAssignment> existingRoleAssignments = new ArrayList<>();
        existingRoleAssignments.add(existingRoleAssignment1);
        existingRoleAssignments.add(existingRoleAssignment2);
        existingRoleAssignments.add(existingRoleAssignment3);

        //assign incorrect assigner id
        assignmentRequest.getRequest().setAssignerId(requestedRole1.getRoleName() + requestedRole2.getActorId());

        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        // facts must contain all existing role assignments
        facts.addAll(existingRoleAssignments);

        // facts must contain the request
        facts.add(assignmentRequest.getRequest());

        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().stream()
            .filter(roleAssignment -> roleAssignment.getActorId().equals(requestedRole1.getActorId()))
            .forEach(roleAssignment -> {
                assertNotEquals(Status.APPROVED, roleAssignment.getStatus());
            });

    }

    @Test
    void shouldRejectCaseValidationForTCW_ForAssignee2STCW_RequesterSCTW_WrongRoleCategory() {

        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.JUDICIAL,"tribunal-caseworker",
                                                             SPECIFIC);
        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

        RoleAssignment requestedRole2 = getRequestedCaseRole(RoleCategory.STAFF,"tribunal-caseworker",
                                                             SPECIFIC);
        requestedRole2.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);
        requestedRoles.add(requestedRole2);
        assignmentRequest.setRequestedRoles(requestedRoles);

        ExistingRoleAssignment existingRoleAssignment1 = buildExistingRoleForIAC(requestedRole1.getActorId(),
                                                                                 "tribunal-caseworker");
        ExistingRoleAssignment existingRoleAssignment2 = buildExistingRoleForIAC(requestedRole2.getActorId(),
                                                                                 "senior-tribunal-caseworker");
        ExistingRoleAssignment existingRoleAssignment3 = buildExistingRoleForIAC(
            assignmentRequest.getRequest().getAssignerId(),"senior-tribunal-caseworker");

        List<ExistingRoleAssignment> existingRoleAssignments = new ArrayList<>();
        existingRoleAssignments.add(existingRoleAssignment1);
        existingRoleAssignments.add(existingRoleAssignment2);
        existingRoleAssignments.add(existingRoleAssignment3);

        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        // facts must contain all existing role assignments
        facts.addAll(existingRoleAssignments);

        // facts must contain the request
        facts.add(assignmentRequest.getRequest());

        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().stream()
            .filter(roleAssignment -> roleAssignment.getActorId().equals(requestedRole1.getActorId()))
            .forEach(roleAssignment -> {
                assertNotEquals(Status.APPROVED, roleAssignment.getStatus());
            });
    }

    @Test
    void shouldRejectCaseValidationForTCW_ForAssignee2STCW_RequesterSCTW_MissingCaseID() {

        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.STAFF,"tribunal-caseworker",
                                                             SPECIFIC);

        RoleAssignment requestedRole2 = getRequestedCaseRole(RoleCategory.STAFF,"tribunal-caseworker",
                                                             SPECIFIC);
        requestedRole2.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);
        requestedRoles.add(requestedRole2);
        assignmentRequest.setRequestedRoles(requestedRoles);

        ExistingRoleAssignment existingRoleAssignment1 = buildExistingRoleForIAC(requestedRole1.getActorId(),
                                                                                 "tribunal-caseworker");
        ExistingRoleAssignment existingRoleAssignment2 = buildExistingRoleForIAC(requestedRole2.getActorId(),
                                                                                 "senior-tribunal-caseworker");
        ExistingRoleAssignment existingRoleAssignment3 = buildExistingRoleForIAC(
            assignmentRequest.getRequest().getAssignerId(),"senior-tribunal-caseworker");

        List<ExistingRoleAssignment> existingRoleAssignments = new ArrayList<>();
        existingRoleAssignments.add(existingRoleAssignment1);
        existingRoleAssignments.add(existingRoleAssignment2);
        existingRoleAssignments.add(existingRoleAssignment3);

        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        // facts must contain all existing role assignments
        facts.addAll(existingRoleAssignments);

        // facts must contain the request
        facts.add(assignmentRequest.getRequest());

        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().stream()
            .filter(roleAssignment -> roleAssignment.getActorId().equals(requestedRole1.getActorId()))
            .forEach(roleAssignment -> {
                assertNotEquals(Status.APPROVED, roleAssignment.getStatus());
            });
    }

    @Test
    void shouldRejectCaseValidationForTCW_ForAssignee2STCW_RequesterSCTW_WrongCaseType() {

        //mock the retrieveDataService to fetch the Case Object with incorrect type ID
        Case caseObj1 = Case.builder().id("1234567890123457")
            .caseTypeId("Not Asylum")
            .jurisdiction("IA")
            .build();
        doReturn(caseObj1).when(retrieveDataService).getCaseById("1234567890123457");

        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.STAFF,"tribunal-caseworker",
                                                             SPECIFIC);
        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123457"));

        RoleAssignment requestedRole2 = getRequestedCaseRole(RoleCategory.STAFF,"tribunal-caseworker",
                                                             SPECIFIC);
        requestedRole2.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);
        requestedRoles.add(requestedRole2);
        assignmentRequest.setRequestedRoles(requestedRoles);

        ExistingRoleAssignment existingRoleAssignment1 = buildExistingRoleForIAC(requestedRole1.getActorId(),
                                                                                 "tribunal-caseworker");
        ExistingRoleAssignment existingRoleAssignment2 = buildExistingRoleForIAC(requestedRole2.getActorId(),
                                                                                 "senior-tribunal-caseworker");
        ExistingRoleAssignment existingRoleAssignment3 = buildExistingRoleForIAC(
            assignmentRequest.getRequest().getAssignerId(),"senior-tribunal-caseworker");

        List<ExistingRoleAssignment> existingRoleAssignments = new ArrayList<>();
        existingRoleAssignments.add(existingRoleAssignment1);
        existingRoleAssignments.add(existingRoleAssignment2);
        existingRoleAssignments.add(existingRoleAssignment3);

        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        // facts must contain all existing role assignments
        facts.addAll(existingRoleAssignments);

        // facts must contain the request
        facts.add(assignmentRequest.getRequest());

        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().stream()
            .filter(roleAssignment -> roleAssignment.getActorId().equals(requestedRole1.getActorId()))
            .forEach(roleAssignment -> {
                assertNotEquals(Status.APPROVED, roleAssignment.getStatus());
            });
    }

    @Test
    void shouldRejectCaseValidationForTCW_ForAssignee2STCW_RequesterSCTW_WrongCaseJurisdiction() {

        //mock the retrieveDataService to fetch the Case Object with incorrect jurisdiction
        Case caseObj1 = Case.builder().id("1234567890123457")
            .caseTypeId("Asylum")
            .jurisdiction("CMC")
            .build();
        doReturn(caseObj1).when(retrieveDataService).getCaseById("1234567890123457");

        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.STAFF,"tribunal-caseworker",
                                                             SPECIFIC);
        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123457"));

        RoleAssignment requestedRole2 = getRequestedCaseRole(RoleCategory.STAFF,"tribunal-caseworker",
                                                             SPECIFIC);
        requestedRole2.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);
        requestedRoles.add(requestedRole2);
        assignmentRequest.setRequestedRoles(requestedRoles);

        ExistingRoleAssignment existingRoleAssignment1 = buildExistingRoleForIAC(requestedRole1.getActorId(),
                                                                                 "tribunal-caseworker");
        ExistingRoleAssignment existingRoleAssignment2 = buildExistingRoleForIAC(requestedRole2.getActorId(),
                                                                                 "senior-tribunal-caseworker");
        ExistingRoleAssignment existingRoleAssignment3 = buildExistingRoleForIAC(
            assignmentRequest.getRequest().getAssignerId(),"senior-tribunal-caseworker");

        List<ExistingRoleAssignment> existingRoleAssignments = new ArrayList<>();
        existingRoleAssignments.add(existingRoleAssignment1);
        existingRoleAssignments.add(existingRoleAssignment2);
        existingRoleAssignments.add(existingRoleAssignment3);

        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        // facts must contain all existing role assignments
        facts.addAll(existingRoleAssignments);

        // facts must contain the request
        facts.add(assignmentRequest.getRequest());

        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().stream()
            .filter(roleAssignment -> roleAssignment.getActorId().equals(requestedRole1.getActorId()))
            .forEach(roleAssignment -> {
                assertNotEquals(Status.APPROVED, roleAssignment.getStatus());
            });
    }

    @Test
    void shouldRejectCaseValidationForTCW_RequesterSCTW_NoBeginTimeForAssignee2() {


        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.STAFF,"tribunal-caseworker",
                                                             SPECIFIC);
        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

        RoleAssignment requestedRole2 = getRequestedCaseRole(RoleCategory.STAFF,"tribunal-caseworker",
                                                             SPECIFIC);
        requestedRole2.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);
        requestedRoles.add(requestedRole2);
        assignmentRequest.setRequestedRoles(requestedRoles);

        //Testing begin time by not adding existing role assignment for assigner 2
        ExistingRoleAssignment existingRoleAssignment1 = buildExistingRoleForIAC(requestedRole1.getActorId(),
                                                                                 "tribunal-caseworker");

        ExistingRoleAssignment existingRoleAssignment3 = buildExistingRoleForIAC(
            assignmentRequest.getRequest().getAssignerId(),"senior-tribunal-caseworker");

        List<ExistingRoleAssignment> existingRoleAssignments = new ArrayList<>();
        existingRoleAssignments.add(existingRoleAssignment1);
        existingRoleAssignments.add(existingRoleAssignment3);

        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        // facts must contain all existing role assignments
        facts.addAll(existingRoleAssignments);

        // facts must contain the request
        facts.add(assignmentRequest.getRequest());

        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().stream()
            .filter(roleAssignment -> roleAssignment.getActorId().equals(requestedRole2.getActorId()))
            .forEach(roleAssignment -> {
                assertNotEquals(Status.APPROVED, roleAssignment.getStatus());
            });
    }

    @Test
    void shouldRejectCaseValidationForTCW_RequesterSCTW_NoEndTimeForAssignee1() {

        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.STAFF,"tribunal-caseworker",
                                                             SPECIFIC);
        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

        RoleAssignment requestedRole2 = getRequestedCaseRole(RoleCategory.STAFF,"tribunal-caseworker",
                                                             SPECIFIC);
        requestedRole2.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);
        requestedRoles.add(requestedRole2);
        assignmentRequest.setRequestedRoles(requestedRoles);

        //Testing begin time by not adding existing role assignment for assignee1
        ExistingRoleAssignment existingRoleAssignment2 = buildExistingRoleForIAC(requestedRole2.getActorId(),
                                                                                 "tribunal-caseworker");

        ExistingRoleAssignment existingRoleAssignment3 = buildExistingRoleForIAC(
            assignmentRequest.getRequest().getAssignerId(),"senior-tribunal-caseworker");

        List<ExistingRoleAssignment> existingRoleAssignments = new ArrayList<>();
        existingRoleAssignments.add(existingRoleAssignment2);
        existingRoleAssignments.add(existingRoleAssignment3);

        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        // facts must contain all existing role assignments
        facts.addAll(existingRoleAssignments);

        // facts must contain the request
        facts.add(assignmentRequest.getRequest());

        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().stream()
            .filter(roleAssignment -> roleAssignment.getActorId().equals(requestedRole1.getActorId()))
            .forEach(roleAssignment -> {
                assertNotEquals(Status.APPROVED, roleAssignment.getStatus());
            });
    }

    @Test
    void shouldRejectCaseValidationForTCW_ForAssignee2STCW_RequesterSCTW_WrongGrantType() {

        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.STAFF,"tribunal-caseworker",
                                                             CHALLENGED);
        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

        RoleAssignment requestedRole2 = getRequestedCaseRole(RoleCategory.STAFF,"tribunal-caseworker",
                                                             SPECIFIC);
        requestedRole2.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);
        requestedRoles.add(requestedRole2);
        assignmentRequest.setRequestedRoles(requestedRoles);

        ExistingRoleAssignment existingRoleAssignment1 = buildExistingRoleForIAC(requestedRole1.getActorId(),
                                                                                 "tribunal-caseworker");
        ExistingRoleAssignment existingRoleAssignment2 = buildExistingRoleForIAC(requestedRole2.getActorId(),
                                                                                 "senior-tribunal-caseworker");
        ExistingRoleAssignment existingRoleAssignment3 = buildExistingRoleForIAC(
            assignmentRequest.getRequest().getAssignerId(),"senior-tribunal-caseworker");

        List<ExistingRoleAssignment> existingRoleAssignments = new ArrayList<>();
        existingRoleAssignments.add(existingRoleAssignment1);
        existingRoleAssignments.add(existingRoleAssignment2);
        existingRoleAssignments.add(existingRoleAssignment3);

        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        // facts must contain all existing role assignments
        facts.addAll(existingRoleAssignments);

        // facts must contain the request
        facts.add(assignmentRequest.getRequest());

        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().stream()
            .filter(roleAssignment -> roleAssignment.getActorId().equals(requestedRole1.getActorId()))
            .forEach(roleAssignment -> {
                assertNotEquals(Status.APPROVED, roleAssignment.getStatus());
            });
    }


    @Test
    void shouldRejectCaseValidationForTCW_ForAssignee3STCW_RequesterSCTW_MissingExistingRA() {

        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.STAFF,"tribunal-caseworker",
                                                             SPECIFIC);
        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

        RoleAssignment requestedRole2 = getRequestedCaseRole(RoleCategory.STAFF,"tribunal-caseworker",
                                                             SPECIFIC);
        requestedRole2.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

        RoleAssignment requestedRole3 = getRequestedCaseRole(RoleCategory.STAFF,"tribunal-caseworker",
                                                             SPECIFIC);
        requestedRole3.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);
        requestedRoles.add(requestedRole3);
        assignmentRequest.setRequestedRoles(requestedRoles);

        ExistingRoleAssignment existingRoleAssignment1 = buildExistingRoleForIAC(requestedRole1.getActorId(),
                                                                                 "tribunal-caseworker");
        ExistingRoleAssignment existingRoleAssignment2 = buildExistingRoleForIAC(requestedRole2.getActorId(),
                                                                                 "senior-tribunal-caseworker");
        ExistingRoleAssignment existingRoleAssignment3 = buildExistingRoleForIAC(
            assignmentRequest.getRequest().getAssignerId(),"senior-tribunal-caseworker");

        List<ExistingRoleAssignment> existingRoleAssignments = new ArrayList<>();
        existingRoleAssignments.add(existingRoleAssignment1);
        existingRoleAssignments.add(existingRoleAssignment2);
        existingRoleAssignments.add(existingRoleAssignment3);

        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        // facts must contain all existing role assignments
        facts.addAll(existingRoleAssignments);

        // facts must contain the request
        facts.add(assignmentRequest.getRequest());

        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().stream()
            .filter(roleAssignment -> roleAssignment.getActorId().equals(requestedRole3.getActorId()))
            .forEach(roleAssignment -> {
                assertNotEquals(Status.APPROVED, roleAssignment.getStatus());
            });
    }

}
