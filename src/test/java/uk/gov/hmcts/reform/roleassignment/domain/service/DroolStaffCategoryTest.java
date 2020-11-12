package uk.gov.hmcts.reform.roleassignment.domain.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.Case;
import uk.gov.hmcts.reform.roleassignment.domain.model.Request;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.*;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.RetrieveDataService;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType.SPECIFIC;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType.STANDARD;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.CREATE_REQUESTED;
import static uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder.buildExistingRoleForIAC;
import static uk.gov.hmcts.reform.roleassignment.util.JacksonUtils.convertValueJsonNode;

@RunWith(MockitoJUnitRunner.class)
class DroolStaffCategoryTest extends DroolBase {

    @Mock
    private final RetrieveDataService retrieveDataService = mock(RetrieveDataService.class);

    @Test
    void shouldApprovedOrgRequestedRoleForTCW() {

//        clientId check not implemented yet
//        assignmentRequest.getRequest().setClientId("orm");

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

        // facts must contain the request
        facts.add(assignmentRequest.getRequest());

        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            assertEquals(Status.APPROVED, roleAssignment.getStatus());
        });


    }

    @Test
    void shouldRejectOrgRequestedRoleForTCW_PrimaryLocationMissing() {

//        clientId check not implemented yet
//        assignmentRequest.getRequest().setClientId("orm");

        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.STAFF);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName("tribunal-caseworker");
            roleAssignment.setGrantType(STANDARD);
            roleAssignment.getAttributes().put("jurisdiction", convertValueJsonNode("IA"));

        });

        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        // facts must contain the request
        facts.add(assignmentRequest.getRequest());

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

        // facts must contain the request
        facts.add(assignmentRequest.getRequest());

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

        // facts must contain the request
        facts.add(assignmentRequest.getRequest());

        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            assertEquals(Status.REJECTED, roleAssignment.getStatus());

        });


    }

    @Test
    void shouldApprovedOrgRequestedRoleForSTCW() {

//        clientId check not implemented yet
//        assignmentRequest.getRequest().setClientId("orm");

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

        // facts must contain the request
        facts.add(assignmentRequest.getRequest());

        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            assertEquals(Status.APPROVED, roleAssignment.getStatus());
        });


    }

    @Test
    void shouldRejectOrgValidationForSTCW_MissingAttributeJurisdiction() {

//        clientId check not implemented yet
//        assignmentRequest.getRequest().setClientId("orm");

        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.STAFF);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName("senior-tribunal-caseworker");
            roleAssignment.setGrantType(STANDARD);
            roleAssignment.getAttributes().put("primaryLocation", convertValueJsonNode("abc"));
        });


        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        // facts must contain the request
        facts.add(assignmentRequest.getRequest());

        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            assertNotEquals(Status.APPROVED, roleAssignment.getStatus());
        });


    }


    @Test
    void shouldPassDeleteRequestedRoleForOrg() {

//        clientId check not implemented yet
//        assignmentRequest.getRequest().setClientId("orm");

        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setStatus(Status.DELETE_REQUESTED);
            roleAssignment.setRoleCategory(RoleCategory.STAFF);
            roleAssignment.setRoleType(RoleType.ORGANISATION);

        });


        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        // facts must contain the request
        facts.add(assignmentRequest.getRequest());

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

        // facts must contain the request
        facts.add(assignmentRequest.getRequest());

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

        // facts must contain the request
        facts.add(assignmentRequest.getRequest());

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

        // facts must contain the request
        facts.add(assignmentRequest.getRequest());

        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            assertEquals(Status.DELETE_REJECTED, roleAssignment.getStatus());
        });


    }

    @Test
    void shouldRejectOrgValidationForTCW_WrongValueForAttributeJurisdiction() {

//        clientId check not implemented yet
//        assignmentRequest.getRequest().setClientId("orm");

        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.STAFF);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName("tribunal-caseworker");
            roleAssignment.setGrantType(STANDARD);
            roleAssignment.getAttributes().put("jurisdiction", convertValueJsonNode("CMC"));
            roleAssignment.getAttributes().put("primaryLocation", convertValueJsonNode("abc"));
        });

        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        // facts must contain the request
        facts.add(assignmentRequest.getRequest());

        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            assertNotEquals(Status.APPROVED, roleAssignment.getStatus());
        });


    }

    @Test
    void shouldRejectOrgValidationForTCW_WrongGrantType() {

//        clientId check not implemented yet
//        assignmentRequest.getRequest().setClientId("orm");

        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.STAFF);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName("tribunal-caseworker");
            roleAssignment.setGrantType(SPECIFIC);
            roleAssignment.getAttributes().put("jurisdiction", convertValueJsonNode("IA"));
            roleAssignment.getAttributes().put("primaryLocation", convertValueJsonNode("abc"));
        });

        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        // facts must contain the request
        facts.add(assignmentRequest.getRequest());

        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            assertNotEquals(Status.APPROVED, roleAssignment.getStatus());
        });


    }

    @Test
    void shouldRejectOrgValidationForTCW_WrongRoleCategory() {

//        clientId check not implemented yet
//        assignmentRequest.getRequest().setClientId("orm");

        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.JUDICIAL);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName("tribunal-caseworker");
            roleAssignment.setGrantType(STANDARD);
            roleAssignment.getAttributes().put("jurisdiction", convertValueJsonNode("IA"));
            roleAssignment.getAttributes().put("primaryLocation", convertValueJsonNode("abc"));
        });

        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        // facts must contain the request
        facts.add(assignmentRequest.getRequest());

        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            assertNotEquals(Status.APPROVED, roleAssignment.getStatus());
        });


    }


    @Test
    void shouldRejectOrgValidationForTCW_WrongClassification() {

//        clientId check not implemented yet
//        assignmentRequest.getRequest().setClientId("orm");

        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.STAFF);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setClassification(Classification.RESTRICTED);
            roleAssignment.setRoleName("tribunal-caseworker");
            roleAssignment.setGrantType(STANDARD);
            roleAssignment.getAttributes().put("jurisdiction", convertValueJsonNode("IA"));
            roleAssignment.getAttributes().put("primaryLocation", convertValueJsonNode("abc"));
        });

        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        // facts must contain the request
        facts.add(assignmentRequest.getRequest());
        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            assertNotEquals(Status.APPROVED, roleAssignment.getStatus());
        });


    }

    @Test
    void shouldRejectOrgValidationForTCW_MissingAttributeJurisdiction() {

//        clientId check not implemented yet
//        assignmentRequest.getRequest().setClientId("orm");

        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.STAFF);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName("tribunal-caseworker");
            roleAssignment.setGrantType(STANDARD);
            roleAssignment.getAttributes().put("primaryLocation", convertValueJsonNode("abc"));
        });


        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());
        // facts must contain the request
        facts.add(assignmentRequest.getRequest());

        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            assertNotEquals(Status.APPROVED, roleAssignment.getStatus());
        });
    }
//    @Test
//    void shouldRejectOrgRequestedRoleForTCW_WrongClientID() {
//
//        assignmentRequest.getRequest().setClientId("ccd-gw");
//
//        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
//            roleAssignment.setRoleCategory(RoleCategory.STAFF);
//            roleAssignment.setRoleType(RoleType.ORGANISATION);
//            roleAssignment.setRoleName("tribunal-caseworker");
//            roleAssignment.setGrantType(STANDARD);
//            roleAssignment.getAttributes().put("jurisdiction", convertValueJsonNode("IA"));
//            roleAssignment.getAttributes().put("primaryLocation", convertValueJsonNode("abc"));
//        });
//
//        // facts must contain all affected role assignments
//        facts.addAll(assignmentRequest.getRequestedRoles());
//
//        // facts must contain the request
//        facts.add(assignmentRequest.getRequest());
//        // Run the rules
//        kieSession.execute(facts);
//
//        //assertion
//        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
//            assertNotEquals(Status.APPROVED, roleAssignment.getStatus());
//        });
//
//
//    }

//    @Test
//    void shouldRejectDeleteRequestedRoleForOrgWrongClientId() {
//
//        assignmentRequest.getRequest().setClientId("ccd-gm");
//
//        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
//            roleAssignment.setStatus(Status.DELETE_REQUESTED);
//            roleAssignment.setRoleCategory(RoleCategory.STAFF);
//            roleAssignment.setRoleType(RoleType.ORGANISATION);
//
//        });
//
//
//        // facts must contain all affected role assignments
//        facts.addAll(assignmentRequest.getRequestedRoles());
//
//    // facts must contain the request
//        facts.add(assignmentRequest.getRequest());
//        // Run the rules
//        kieSession.execute(facts);
//
//        //assertion
//        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
//            assertEquals(Status.DELETE_REJECTED, roleAssignment.getStatus());
//        });
//
//
//    }

    @Test
    void shouldApprovedCaseValidationForTCW_ForAssignee2STCW_RequesterTCW() {

        List<RoleAssignment> roleAssignmentList = new ArrayList<>();
        Map<String,JsonNode> attributesCase = new HashMap<String, JsonNode>();
        attributesCase.put("caseId", convertValueJsonNode("1234567890123456"));

        RoleAssignment roleAssignment1Case = RoleAssignment.builder()
            .id(UUID.fromString("9785c98c-78f2-418b-ab74-a892c3ccca9e"))
            .actorId("4772dc44-268f-4d0c-8f83-f0fb662aac82")
            .actorIdType(ActorIdType.IDAM)
            .classification(Classification.PUBLIC)
            .readOnly(true)
            .status(CREATE_REQUESTED)
            .roleCategory(RoleCategory.STAFF)
            .roleType(RoleType.CASE)
            .roleName("tribunal-caseworker")
            .grantType(SPECIFIC)
            .attributes(attributesCase)
            .build();

        roleAssignmentList.add(roleAssignment1Case);

        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {

            roleAssignment.setRoleCategory(RoleCategory.STAFF);
            roleAssignment.setRoleType(RoleType.CASE);
            roleAssignment.setRoleName("tribunal-caseworker");
            roleAssignment.setGrantType(SPECIFIC);
            roleAssignment.setAttributes(attributesCase);

            roleAssignmentList.add(roleAssignment);
        });

        assignmentRequest.setRequestedRoles(roleAssignmentList);

        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        //facts must contain existing role of assigner
        facts.add(buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
                                          "tribunal-caseworker"));


        //facts must contain existing role of assignees
        assignmentRequest.getRequestedRoles().forEach(roleAssignment ->{
            if (roleAssignment.getActorId().equals("4772dc44-268f-4d0c-8f83-f0fb662aac82")){
                facts.add(buildExistingRoleForIAC(roleAssignment.getActorId(),
                                                  "senior-tribunal-caseworker"));
            }
            else {
                facts.add(buildExistingRoleForIAC(roleAssignment.getActorId(),
                                                  roleAssignment.getRoleName()));
            }
        });

        // facts must contain the request
        facts.add(assignmentRequest.getRequest());

        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            System.out.println(roleAssignment);
             assertEquals(Status.APPROVED, roleAssignment.getStatus());
        });


    }

    @Test
    void shouldApprovedCaseValidationForTCW_ForAssignee2STCW_RequesterSTCW() {

        List<RoleAssignment> roleAssignmentList = new ArrayList<>();
        Map<String,JsonNode> attributesCase = new HashMap<String, JsonNode>();
        attributesCase.put("caseId", convertValueJsonNode("1234567890123456"));

        RoleAssignment roleAssignment1Case = RoleAssignment.builder()
            .id(UUID.fromString("9785c98c-78f2-418b-ab74-a892c3ccca9e"))
            .actorId("4772dc44-268f-4d0c-8f83-f0fb662aac82")
            .actorIdType(ActorIdType.IDAM)
            .classification(Classification.PUBLIC)
            .readOnly(true)
            .status(CREATE_REQUESTED)
            .roleCategory(RoleCategory.STAFF)
            .roleType(RoleType.CASE)
            .roleName("tribunal-caseworker")
            .grantType(SPECIFIC)
            .attributes(attributesCase)
            .build();

        roleAssignmentList.add(roleAssignment1Case);


        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {

            roleAssignment.setRoleCategory(RoleCategory.STAFF);
            roleAssignment.setRoleType(RoleType.CASE);
            roleAssignment.setRoleName("tribunal-caseworker");
            roleAssignment.setGrantType(SPECIFIC);
            roleAssignment.setAttributes(attributesCase);

            roleAssignmentList.add(roleAssignment);
        });

        assignmentRequest.setRequestedRoles(roleAssignmentList);

        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        //facts must contain existing role of assigner
        facts.add(buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
                                          "senior-tribunal-caseworker"));

        //facts must contain existing role of assignees
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment ->{
            if (roleAssignment.getActorId().equals("4772dc44-268f-4d0c-8f83-f0fb662aac82")){
                facts.add(buildExistingRoleForIAC(roleAssignment.getActorId(),
                                                  "senior-tribunal-caseworker"));
            }
            else {
                facts.add(buildExistingRoleForIAC(roleAssignment.getActorId(),
                                                  roleAssignment.getRoleName()));
            }
        });

        facts.add(assignmentRequest.getRequest());

        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            assertEquals(Status.APPROVED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldRejectCaseValidationForTCW_ForAssignee2STCW_RequesterJudge() {

        List<RoleAssignment> roleAssignmentList = new ArrayList<>();
        Map<String,JsonNode> attributesCase = new HashMap<String, JsonNode>();
        attributesCase.put("caseId", convertValueJsonNode("1234567890123456"));


        RoleAssignment roleAssignment1Case = RoleAssignment.builder()
            .id(UUID.fromString("9785c98c-78f2-418b-ab74-a892c3ccca9e"))
            .actorId("4772dc44-268f-4d0c-8f83-f0fb662aac82")
            .actorIdType(ActorIdType.IDAM)
            .classification(Classification.PUBLIC)
            .readOnly(true)
            .status(CREATE_REQUESTED)
            .roleCategory(RoleCategory.STAFF)
            .roleType(RoleType.CASE)
            .roleName("tribunal-caseworker")
            .grantType(SPECIFIC)
            .attributes(attributesCase)
            .build();

        roleAssignmentList.add(roleAssignment1Case);


        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {

            roleAssignment.setRoleCategory(RoleCategory.STAFF);
            roleAssignment.setRoleType(RoleType.CASE);
            roleAssignment.setRoleName("tribunal-caseworker");
            roleAssignment.setGrantType(SPECIFIC);
            roleAssignment.setAttributes(attributesCase);

            roleAssignmentList.add(roleAssignment);
        });

        assignmentRequest.setRequestedRoles(roleAssignmentList);

        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        //facts must contain existing role of assigner
        facts.add(buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
                                          "judge"));

        //facts must contain existing role of assignees
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment ->{
            if (roleAssignment.getActorId().equals("4772dc44-268f-4d0c-8f83-f0fb662aac82")){
                facts.add(buildExistingRoleForIAC(roleAssignment.getActorId(),
                                                  "senior-tribunal-caseworker"));
            }
            else {

                facts.add(buildExistingRoleForIAC(roleAssignment.getActorId(),
                                                  roleAssignment.getRoleName()));
            }
        });

        facts.add(assignmentRequest.getRequest());
        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            System.out.println(roleAssignment);
                assertEquals(Status.REJECTED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldRejectCaseValidationForTCW_ForAssignee2Judge_RequesterSTCW() {

        List<RoleAssignment> roleAssignmentList = new ArrayList<>();
        Map<String,JsonNode> attributesCase = new HashMap<String, JsonNode>();
        attributesCase.put("caseId", convertValueJsonNode("1234567890123456"));

        RoleAssignment roleAssignment1Case = RoleAssignment.builder()
            .id(UUID.fromString("9785c98c-78f2-418b-ab74-a892c3ccca9e"))
            .actorId("4772dc44-268f-4d0c-8f83-f0fb662aac82")
            .actorIdType(ActorIdType.IDAM)
            .classification(Classification.PUBLIC)
            .readOnly(true)
            .status(CREATE_REQUESTED)
            .roleCategory(RoleCategory.STAFF)
            .roleType(RoleType.CASE)
            .roleName("tribunal-caseworker")
            .grantType(SPECIFIC)
            .attributes(attributesCase)
            .build();

        roleAssignmentList.add(roleAssignment1Case);

        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {

            roleAssignment.setRoleCategory(RoleCategory.STAFF);
            roleAssignment.setRoleType(RoleType.CASE);
            roleAssignment.setRoleName("tribunal-caseworker");
            roleAssignment.setGrantType(SPECIFIC);
            roleAssignment.setAttributes(attributesCase);

            roleAssignmentList.add(roleAssignment);
        });

        assignmentRequest.setRequestedRoles(roleAssignmentList);

        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        //facts must contain existing role of assigner
        facts.add(buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
                                          "senior-tribunal-caseworker"));

        //facts must contain existing role of assignees
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment ->{
            if (roleAssignment.getActorId().equals("4772dc44-268f-4d0c-8f83-f0fb662aac82")){

                facts.add(buildExistingRoleForIAC(roleAssignment.getActorId(),
                                                  "judge"));
            }
            else {

                facts.add(buildExistingRoleForIAC(roleAssignment.getActorId(),
                                                  roleAssignment.getRoleName()));
            }
        });

        facts.add(assignmentRequest.getRequest());

        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            System.out.println(roleAssignment);

            if (roleAssignment.getActorId().equals("4772dc44-268f-4d0c-8f83-f0fb662aac82")){
                assertNotEquals(Status.APPROVED, roleAssignment.getStatus());
            }
        });
    }

    @Test
    void shouldRejectCaseValidationForTCW_ForAssignee2STCW_RequesterSCTW_WrongAssignerID() {

        List<RoleAssignment> roleAssignmentList = new ArrayList<>();
        Map<String,JsonNode> attributesCase = new HashMap<String, JsonNode>();
        attributesCase.put("caseId", convertValueJsonNode("1234567890123456"));

        RoleAssignment roleAssignmentCase = RoleAssignment.builder()
            .id(UUID.fromString("9785c98c-78f2-418b-ab74-a892c3ccca9e"))
            .actorId("4772dc44-268f-4d0c-8f83-f0fb662aac82")
            .actorIdType(ActorIdType.IDAM)
            .classification(Classification.PUBLIC)
            .readOnly(true)
            .status(CREATE_REQUESTED)
            .roleCategory(RoleCategory.STAFF)
            .roleType(RoleType.CASE)
            .roleName("tribunal-caseworker")
            .grantType(SPECIFIC)
            .attributes(attributesCase)
            .build();

        roleAssignmentList.add(roleAssignmentCase);

        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {

            roleAssignment.setRoleCategory(RoleCategory.STAFF);
            roleAssignment.setRoleType(RoleType.CASE);
            roleAssignment.setRoleName("tribunal-caseworker");
            roleAssignment.setGrantType(SPECIFIC);
            roleAssignment.setAttributes(attributesCase);

            roleAssignmentList.add(roleAssignment);
        });

        assignmentRequest.setRequestedRoles(roleAssignmentList);
        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        //facts must contain existing role of assigner
        facts.add(buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
                                          "senior-tribunal-caseworker"));


        //facts must contain existing role of assignees
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment ->{
            if (roleAssignment.getActorId().equals("4772dc44-268f-4d0c-8f83-f0fb662aac83")){
                Request newAssignmentRequest =  Request.builder()
                    .id(UUID.fromString("ab4e8c21-27a0-4abd-aed8-810fdce22adb"))
                    .authenticatedUserId("4772dc44-268f-4d0c-8f83-f0fb662aac84")
                    .correlationId("38a90097-434e-47ee-8ea1-9ea2a267f51d")
                    .assignerId("4772dc44-268f-4d0c-8f83-f0fb662aac8")
                    .requestType(RequestType.CREATE)
                    .reference("4772dc44-268f-4d0c-8f83-f0fb662aac84")
                    .process(("p2"))
                    .replaceExisting(true)
                    .status(Status.CREATED)
                    .created(LocalDateTime.now())
                    .build();
                facts.add(buildExistingRoleForIAC(roleAssignment.getActorId(),
                                                  roleAssignment.getRoleName()));
                facts.add(newAssignmentRequest);

            }
            else {
//                facts.add(assignmentRequest.getRequest());

                facts.add(buildExistingRoleForIAC(roleAssignment.getActorId(),
                                                  "senior-tribunal-caseworker"));
            }

        });

        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            System.out.println(roleAssignment);
            if(roleAssignment.getActorId().equals("4772dc44-268f-4d0c-8f83-f0fb662aac83")){
                assertEquals(Status.REJECTED, roleAssignment.getStatus());
            }
        });
    }

    @Test
    void shouldRejectCaseValidationForTCW_ForAssignee2STCW_RequesterSCTW_WrongRoleCategory() {

        List<RoleAssignment> roleAssignmentList = new ArrayList<>();
        Map<String,JsonNode> attributesCase = new HashMap<String, JsonNode>();
        attributesCase.put("caseId", convertValueJsonNode("1234567890123456"));

        RoleAssignment roleAssignmentCase = RoleAssignment.builder()
            .id(UUID.fromString("9785c98c-78f2-418b-ab74-a892c3ccca9e"))
            .actorId("4772dc44-268f-4d0c-8f83-f0fb662aac82")
            .actorIdType(ActorIdType.IDAM)
            .classification(Classification.PUBLIC)
            .readOnly(true)
            .status(CREATE_REQUESTED)
            .roleCategory(RoleCategory.STAFF)
            .roleType(RoleType.CASE)
            .roleName("tribunal-caseworker")
            .grantType(SPECIFIC)
            .attributes(attributesCase)
            .build();

        roleAssignmentList.add(roleAssignmentCase);

        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {

            roleAssignment.setRoleCategory(RoleCategory.JUDICIAL);
            roleAssignment.setRoleType(RoleType.CASE);
            roleAssignment.setRoleName("tribunal-caseworker");
            roleAssignment.setGrantType(SPECIFIC);
            roleAssignment.setAttributes(attributesCase);

            roleAssignmentList.add(roleAssignment);
        });

        assignmentRequest.setRequestedRoles(roleAssignmentList);
        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        //facts must contain existing role of assigner
        facts.add(buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
                                          "senior-tribunal-caseworker"));


        //facts must contain existing role of assignees
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment ->{
            if (roleAssignment.getActorId().equals("4772dc44-268f-4d0c-8f83-f0fb662aac83")){

                facts.add(buildExistingRoleForIAC(roleAssignment.getActorId(),
                                                  roleAssignment.getRoleName()));
            }
            else {

                facts.add(buildExistingRoleForIAC(roleAssignment.getActorId(),
                                                  "senior-tribunal-caseworker"));
            }

        });

        //fact must contain request
        facts.add(assignmentRequest.getRequest());

        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            System.out.println(roleAssignment);
            if(roleAssignment.getActorId().equals("4772dc44-268f-4d0c-8f83-f0fb662aac83")){
                assertNotEquals(Status.APPROVED, roleAssignment.getStatus());
            }
        });
    }

    @Test
    void shouldRejectCaseValidationForTCW_ForAssignee2STCW_RequesterSCTW_MissingCaseID() {

        List<RoleAssignment> roleAssignmentList = new ArrayList<>();
        Map<String,JsonNode> attributesCase = new HashMap<String, JsonNode>();
        attributesCase.put("caseId", convertValueJsonNode("1234567890123456"));

        RoleAssignment roleAssignmentCase = RoleAssignment.builder()
            .id(UUID.fromString("9785c98c-78f2-418b-ab74-a892c3ccca9e"))
            .actorId("4772dc44-268f-4d0c-8f83-f0fb662aac82")
            .actorIdType(ActorIdType.IDAM)
            .classification(Classification.PUBLIC)
            .readOnly(true)
            .status(CREATE_REQUESTED)
            .roleCategory(RoleCategory.STAFF)
            .roleType(RoleType.CASE)
            .roleName("tribunal-caseworker")
            .grantType(SPECIFIC)
            .attributes(attributesCase)
            .build();

        roleAssignmentList.add(roleAssignmentCase);

        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {

            roleAssignment.setRoleCategory(RoleCategory.STAFF);
            roleAssignment.setRoleType(RoleType.CASE);
            roleAssignment.setRoleName("tribunal-caseworker");
            roleAssignment.setGrantType(SPECIFIC);
//            roleAssignment.setAttributes(attributesCase);

            roleAssignmentList.add(roleAssignment);
        });

        assignmentRequest.setRequestedRoles(roleAssignmentList);
        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        //facts must contain existing role of assigner
        facts.add(buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
                                          "senior-tribunal-caseworker"));


        //facts must contain existing role of assignees
        assignmentRequest.getRequestedRoles().forEach(roleAssignment ->{
            if (roleAssignment.getActorId().equals("4772dc44-268f-4d0c-8f83-f0fb662aac83")){
                facts.add(buildExistingRoleForIAC(roleAssignment.getActorId(),
                                                  roleAssignment.getRoleName()));
            }
            else {
                facts.add(buildExistingRoleForIAC(roleAssignment.getActorId(),
                                                  "senior-tribunal-caseworker"));
            }

        });

        //fact must contain request
        facts.add(assignmentRequest.getRequest());

        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            System.out.println(roleAssignment);
            if(roleAssignment.getActorId().equals("4772dc44-268f-4d0c-8f83-f0fb662aac83")){
                assertNotEquals(Status.APPROVED, roleAssignment.getStatus());
            }
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

        List<RoleAssignment> roleAssignmentList = new ArrayList<>();
        Map<String,JsonNode> attributesCase = new HashMap<String, JsonNode>();
        attributesCase.put("caseId", convertValueJsonNode("1234567890123456"));

        Map<String,JsonNode> attributesCase1 = new HashMap<String, JsonNode>();
        attributesCase1.put("caseId", convertValueJsonNode("1234567890123457"));

        RoleAssignment roleAssignmentCase = RoleAssignment.builder()
            .id(UUID.fromString("9785c98c-78f2-418b-ab74-a892c3ccca9e"))
            .actorId("4772dc44-268f-4d0c-8f83-f0fb662aac82")
            .actorIdType(ActorIdType.IDAM)
            .classification(Classification.PUBLIC)
            .readOnly(true)
            .status(CREATE_REQUESTED)
            .roleCategory(RoleCategory.STAFF)
            .roleType(RoleType.CASE)
            .roleName("tribunal-caseworker")
            .grantType(SPECIFIC)
            .attributes(attributesCase)
            .build();

        roleAssignmentList.add(roleAssignmentCase);

        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {

            roleAssignment.setRoleCategory(RoleCategory.STAFF);
            roleAssignment.setRoleType(RoleType.CASE);
            roleAssignment.setRoleName("tribunal-caseworker");
            roleAssignment.setGrantType(SPECIFIC);
            roleAssignment.setAttributes(attributesCase1);

            roleAssignmentList.add(roleAssignment);
        });

        assignmentRequest.setRequestedRoles(roleAssignmentList);
        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        //facts must contain existing role of assigner
        facts.add(buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
                                          "senior-tribunal-caseworker"));


        //facts must contain existing role of assignees
        assignmentRequest.getRequestedRoles().forEach(roleAssignment ->{
            if (roleAssignment.getActorId().equals("4772dc44-268f-4d0c-8f83-f0fb662aac83")){
                facts.add(buildExistingRoleForIAC(roleAssignment.getActorId(),
                                                  roleAssignment.getRoleName()));
            }
            else {
                facts.add(buildExistingRoleForIAC(roleAssignment.getActorId(),
                                                  "senior-tribunal-caseworker"));
            }
        });

        //fact must contain request
        facts.add(assignmentRequest.getRequest());

        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            System.out.println(roleAssignment);
            if(roleAssignment.getActorId().equals("4772dc44-268f-4d0c-8f83-f0fb662aac83")){
                assertNotEquals(Status.APPROVED, roleAssignment.getStatus());
            }
        });
    }

    @Test
    void shouldRejectCaseValidationForTCW_ForAssignee2STCW_RequesterSCTW_WrongCaseJurisdiction() {


        //mock the retrieveDataService to fetch the Case Object with incorrect type ID
        Case caseObj1 = Case.builder().id("1234567890123457")
            .caseTypeId("Asylum")
            .jurisdiction("Not IA")
            .build();
        doReturn(caseObj1).when(retrieveDataService).getCaseById("1234567890123457");

        List<RoleAssignment> roleAssignmentList = new ArrayList<>();
        Map<String,JsonNode> attributesCase = new HashMap<String, JsonNode>();
        attributesCase.put("caseId", convertValueJsonNode("1234567890123456"));

        Map<String,JsonNode> attributesCase1 = new HashMap<String, JsonNode>();
        attributesCase1.put("caseId", convertValueJsonNode("1234567890123457"));

        RoleAssignment roleAssignmentCase = RoleAssignment.builder()
            .id(UUID.fromString("9785c98c-78f2-418b-ab74-a892c3ccca9e"))
            .actorId("4772dc44-268f-4d0c-8f83-f0fb662aac82")
            .actorIdType(ActorIdType.IDAM)
            .classification(Classification.PUBLIC)
            .readOnly(true)
            .status(CREATE_REQUESTED)
            .roleCategory(RoleCategory.STAFF)
            .roleType(RoleType.CASE)
            .roleName("tribunal-caseworker")
            .grantType(SPECIFIC)
            .attributes(attributesCase)
            .build();

        roleAssignmentList.add(roleAssignmentCase);

        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {

            roleAssignment.setRoleCategory(RoleCategory.STAFF);
            roleAssignment.setRoleType(RoleType.CASE);
            roleAssignment.setRoleName("tribunal-caseworker");
            roleAssignment.setGrantType(SPECIFIC);
            roleAssignment.setAttributes(attributesCase1);

            roleAssignmentList.add(roleAssignment);
        });

        assignmentRequest.setRequestedRoles(roleAssignmentList);
        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        //facts must contain existing role of assigner
        facts.add(buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
                                          "senior-tribunal-caseworker"));


        //facts must contain existing role of assignees
        assignmentRequest.getRequestedRoles().forEach(roleAssignment ->{
            if (roleAssignment.getActorId().equals("4772dc44-268f-4d0c-8f83-f0fb662aac83")){
                facts.add(buildExistingRoleForIAC(roleAssignment.getActorId(),
                                                  roleAssignment.getRoleName()));
            }
            else {
                facts.add(buildExistingRoleForIAC(roleAssignment.getActorId(),
                                                  "senior-tribunal-caseworker"));
            }
        });

        //fact must contain request
        facts.add(assignmentRequest.getRequest());

        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            System.out.println(roleAssignment);
            if(roleAssignment.getActorId().equals("4772dc44-268f-4d0c-8f83-f0fb662aac83")){
                assertNotEquals(Status.APPROVED, roleAssignment.getStatus());
            }
        });
    }


}
