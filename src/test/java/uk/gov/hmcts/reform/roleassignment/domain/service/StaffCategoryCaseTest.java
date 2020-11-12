package uk.gov.hmcts.reform.roleassignment.domain.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.roleassignment.domain.model.ExistingRoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.ActorIdType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Classification;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType.SPECIFIC;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType.STANDARD;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.CREATE_REQUESTED;
import static uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder.buildExistingRoleForIAC;
import static uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder.getRequestedCaseRole;
import static uk.gov.hmcts.reform.roleassignment.util.JacksonUtils.convertValueJsonNode;

@RunWith(MockitoJUnitRunner.class)
class StaffCategoryCaseTest extends DroolBase {


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
    void shouldRejectCaseRequestedRole_MissingExistingRoleOfAssignee() {

        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.STAFF);
            roleAssignment.setRoleType(RoleType.CASE);
            roleAssignment.setRoleName("tribunal-caseworker");
            roleAssignment.setGrantType(SPECIFIC);
            roleAssignment.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

        });

        // facts must contain the request
        facts.add(assignmentRequest.getRequest());

        // facts must contain all requested role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        //facts must contain existing role of assigner
        facts.add(buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
             "senior-tribunal-caseworker"));

        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            assertEquals(Status.REJECTED, roleAssignment.getStatus());

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

        // facts must contain the request
        facts.add(assignmentRequest.getRequest());

        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        //facts must contain existing role of assigner
        facts.add(buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
            "senior-tribunal-caseworker"));

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


        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            assertEquals(Status.DELETE_REJECTED, roleAssignment.getStatus());
        });


    }


    @Test
    void shouldApprovedCaseValidationForTCW_ForAssignee2STCW_RequesterTCW() {

        List<RoleAssignment> roleAssignmentList = new ArrayList<>();
        Map<String, JsonNode> attributesCase = new HashMap<String, JsonNode>();
        attributesCase.put("caseId", convertValueJsonNode("1234567890123456"));

        Map<String,JsonNode> attributesOrg = new HashMap<String, JsonNode>();
        attributesOrg.put("jurisdiction", convertValueJsonNode("IA"));
        attributesOrg.put("primaryLocation", convertValueJsonNode("abc"));

        RoleAssignment roleAssignment1Org = RoleAssignment.builder()
            .id(UUID.fromString("9785c98c-78f2-418b-ab74-a892c3ccca9e"))
            .actorId("4772dc44-268f-4d0c-8f83-f0fb662aac82")
            .actorIdType(ActorIdType.IDAM)
            .classification(Classification.PUBLIC)
            .readOnly(true)
            .status(CREATE_REQUESTED)
            .roleCategory(RoleCategory.STAFF)
            .roleType(RoleType.ORGANISATION)
            .roleName("senior-tribunal-caseworker")
            .grantType(STANDARD)
            .attributes(attributesOrg)
            .build();

        roleAssignmentList.add(roleAssignment1Org);

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

        RoleAssignment roleAssignmentCase = RoleAssignment.builder()
            .id(UUID.fromString("9785c98c-78f2-418b-ab74-a892c3ccca9f"))
            .actorId("4772dc44-268f-4d0c-8f83-f0fb662aac83")
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
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName("tribunal-caseworker");
            roleAssignment.setGrantType(STANDARD);
            roleAssignment.setAttributes(attributesOrg);

            roleAssignmentList.add(roleAssignment);
        });

        assignmentRequest.setRequestedRoles(roleAssignmentList);

        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        //facts must contain existing role of assigner
        facts.add(buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
                                          "tribunal-caseworker"));

        //facts must contain existing role of assignees
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment ->
                                                                   facts.add(buildExistingRoleForIAC(roleAssignment.getActorId(),
                                                                                                     roleAssignment.getRoleName())));
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

        Map<String,JsonNode> attributesOrg = new HashMap<String, JsonNode>();
        attributesOrg.put("jurisdiction", convertValueJsonNode("IA"));
        attributesOrg.put("primaryLocation", convertValueJsonNode("abc"));

        RoleAssignment roleAssignment1Org = RoleAssignment.builder()
            .id(UUID.fromString("9785c98c-78f2-418b-ab74-a892c3ccca9e"))
            .actorId("4772dc44-268f-4d0c-8f83-f0fb662aac82")
            .actorIdType(ActorIdType.IDAM)
            .classification(Classification.PUBLIC)
            .readOnly(true)
            .status(CREATE_REQUESTED)
            .roleCategory(RoleCategory.STAFF)
            .roleType(RoleType.ORGANISATION)
            .roleName("senior-tribunal-caseworker")
            .grantType(STANDARD)
            .attributes(attributesOrg)
            .build();

        roleAssignmentList.add(roleAssignment1Org);

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

        RoleAssignment roleAssignmentCase = RoleAssignment.builder()
            .id(UUID.fromString("9785c98c-78f2-418b-ab74-a892c3ccca9f"))
            .actorId("4772dc44-268f-4d0c-8f83-f0fb662aac83")
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
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName("tribunal-caseworker");
            roleAssignment.setGrantType(STANDARD);
            roleAssignment.setAttributes(attributesOrg);

            roleAssignmentList.add(roleAssignment);
        });

        assignmentRequest.setRequestedRoles(roleAssignmentList);

        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        //facts must contain existing role of assigner
        facts.add(buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
                                          "senior-tribunal-caseworker"));

        //facts must contain existing role of assignees
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment ->
                                                                   facts.add(buildExistingRoleForIAC(roleAssignment.getActorId(),
                                                                                                     roleAssignment.getRoleName())));

        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            System.out.println(roleAssignment);
            assertEquals(Status.APPROVED, roleAssignment.getStatus());
        });
    }

    @Test
    void shouldRejectCaseValidationForTCW_ForAssignee2STCW_RequesterJudge() {

        List<RoleAssignment> roleAssignmentList = new ArrayList<>();
        Map<String,JsonNode> attributesCase = new HashMap<String, JsonNode>();
        attributesCase.put("caseId", convertValueJsonNode("1234567890123456"));

        Map<String,JsonNode> attributesOrg = new HashMap<String, JsonNode>();
        attributesOrg.put("jurisdiction", convertValueJsonNode("IA"));
        attributesOrg.put("primaryLocation", convertValueJsonNode("abc"));

        RoleAssignment roleAssignment1Org = RoleAssignment.builder()
            .id(UUID.fromString("9785c98c-78f2-418b-ab74-a892c3ccca9e"))
            .actorId("4772dc44-268f-4d0c-8f83-f0fb662aac82")
            .actorIdType(ActorIdType.IDAM)
            .classification(Classification.PUBLIC)
            .readOnly(true)
            .status(CREATE_REQUESTED)
            .roleCategory(RoleCategory.STAFF)
            .roleType(RoleType.ORGANISATION)
            .roleName("senior-tribunal-caseworker")
            .grantType(STANDARD)
            .attributes(attributesOrg)
            .build();

        roleAssignmentList.add(roleAssignment1Org);

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

        RoleAssignment roleAssignmentCase = RoleAssignment.builder()
            .id(UUID.fromString("9785c98c-78f2-418b-ab74-a892c3ccca9f"))
            .actorId("4772dc44-268f-4d0c-8f83-f0fb662aac83")
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
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName("tribunal-caseworker");
            roleAssignment.setGrantType(STANDARD);
            roleAssignment.setAttributes(attributesOrg);

            roleAssignmentList.add(roleAssignment);
        });

        assignmentRequest.setRequestedRoles(roleAssignmentList);

        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        //facts must contain existing role of assigner
        facts.add(buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
                                          "judge"));

        //facts must contain existing role of assignees
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment ->
                                                                   facts.add(buildExistingRoleForIAC(roleAssignment.getActorId(),
                                                                                                     roleAssignment.getRoleName())));

        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            System.out.println(roleAssignment);
            if(roleAssignment.getRoleType().equals(RoleType.CASE)){
                assertEquals(Status.REJECTED, roleAssignment.getStatus());
            }
        });
    }

    @Test
    void shouldRejectCaseValidationForTCW_ForAssignee2Judge_RequesterSTCW() {

        List<RoleAssignment> roleAssignmentList = new ArrayList<>();
        Map<String,JsonNode> attributesCase = new HashMap<String, JsonNode>();
        attributesCase.put("caseId", convertValueJsonNode("1234567890123456"));

        Map<String,JsonNode> attributesOrg = new HashMap<String, JsonNode>();
        attributesOrg.put("jurisdiction", convertValueJsonNode("IA"));
        attributesOrg.put("primaryLocation", convertValueJsonNode("abc"));

        Map<String,JsonNode> attributesJudge = new HashMap<String, JsonNode>();
        attributesJudge.put("jurisdiction", convertValueJsonNode("IA"));
        attributesJudge.put("region", convertValueJsonNode("north-east"));

        RoleAssignment roleAssignment1Org = RoleAssignment.builder()
            .id(UUID.fromString("9785c98c-78f2-418b-ab74-a892c3ccca9e"))
            .actorId("4772dc44-268f-4d0c-8f83-f0fb662aac82")
            .actorIdType(ActorIdType.IDAM)
            .classification(Classification.PUBLIC)
            .readOnly(true)
            .status(CREATE_REQUESTED)
            .roleCategory(RoleCategory.STAFF)
            .roleType(RoleType.ORGANISATION)
            .roleName("judge")
            .grantType(STANDARD)
            .attributes(attributesJudge)
            .build();

        roleAssignmentList.add(roleAssignment1Org);

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

        RoleAssignment roleAssignmentCase = RoleAssignment.builder()
            .id(UUID.fromString("9785c98c-78f2-418b-ab74-a892c3ccca9f"))
            .actorId("4772dc44-268f-4d0c-8f83-f0fb662aac83")
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
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName("tribunal-caseworker");
            roleAssignment.setGrantType(STANDARD);
            roleAssignment.setAttributes(attributesOrg);

            roleAssignmentList.add(roleAssignment);
        });

        assignmentRequest.setRequestedRoles(roleAssignmentList);

        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        //facts must contain existing role of assigner
        facts.add(buildExistingRoleForIAC(assignmentRequest.getRequest().getAssignerId(),
                                          "senior-tribunal-caseworker"));

        //facts must contain existing role of assignees
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment ->
                                                                   facts.add(buildExistingRoleForIAC(roleAssignment.getActorId(),
                                                                                                     roleAssignment.getRoleName())));

        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            if(roleAssignment.getRoleName().equals("judge")){
                System.out.println(roleAssignment);

                assertNotEquals(Status.APPROVED, roleAssignment.getStatus());
            }
        });
    }

    @Test
    void shouldRejectCaseValidationForTCW_ForAssignee2STCW_RequesterSCTW_WrongAssignerID() {

        List<RoleAssignment> roleAssignmentList = new ArrayList<>();
        Map<String,JsonNode> attributesCase = new HashMap<String, JsonNode>();
        attributesCase.put("caseId", convertValueJsonNode("1234567890123456"));

        Map<String,JsonNode> attributesOrg = new HashMap<String, JsonNode>();
        attributesOrg.put("jurisdiction", convertValueJsonNode("IA"));
        attributesOrg.put("primaryLocation", convertValueJsonNode("abc"));



//        RoleAssignment roleAssignment1Case = RoleAssignment.builder()
//            .id(UUID.fromString("9785c98c-78f2-418b-ab74-a892c3ccca9e"))
//            .actorId("4772dc44-268f-4d0c-8f83-f0fb662aac82")
//            .actorIdType(ActorIdType.IDAM)
//            .classification(Classification.PUBLIC)
//            .readOnly(true)
//            .status(CREATE_REQUESTED)
//            .roleCategory(RoleCategory.STAFF)
//            .roleType(RoleType.CASE)
//            .roleName("tribunal-caseworker")
//            .grantType(SPECIFIC)
//            .attributes(attributesCase)
//            .build();
//
//        roleAssignmentList.add(roleAssignment1Case);

        RoleAssignment roleAssignmentCase = RoleAssignment.builder()
            .id(UUID.fromString("9785c98c-78f2-418b-ab74-a892c3ccca9f"))
            .actorId("4772dc44-268f-4d0c-8f83-f0fb662aac84")
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
            roleAssignment.setClassification(Classification.PUBLIC);
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
            if (roleAssignment.getActorId().equals("4772dc44-268f-4d0c-8f83-f0fb662aac83")&& roleAssignment.getRoleType().equals(RoleType.CASE)){
               assignmentRequest.getRequest().setAssignerId("4772dc44-268f-4d0c-8f83-f0fb662aac8");
            }
            else{
                assignmentRequest.getRequest().setAssignerId("4772dc44-268f-4d0c-8f83-f0fb662aac84");

            }
            facts.add(assignmentRequest.getRequest());

            System.out.println(assignmentRequest.getRequest());
            facts.add(buildExistingRoleForIAC(roleAssignment.getActorId(),
                                              roleAssignment.getRoleName()));
        });

        // Run the rules
        kieSession.execute(facts);

        //assertion
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            System.out.println(roleAssignment);
            if(roleAssignment.getActorId().equals("4772dc44-268f-4d0c-8f83-f0fb662aac83") && roleAssignment.getRoleType().equals(RoleType.CASE)){
                assertEquals(Status.REJECTED, roleAssignment.getStatus());
            }
        });
    }

}
