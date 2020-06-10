package uk.gov.hmcts.reform.roleassignment.helper;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Setter;
import uk.gov.hmcts.reform.roleassignment.domain.model.Request;
import uk.gov.hmcts.reform.roleassignment.domain.model.RequestedRole;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.ActorIdType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Classification;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RequestType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

@Setter
public class TestDataBuilder {

    private TestDataBuilder() {
        //not meant to be instantiated.
    }

    public static AssignmentRequest buildAssignmentRequest() {
        LocalDateTime timeStamp = LocalDateTime.now();
        return new AssignmentRequest(buildRequest(),buildRequestedRoleCollection());
    }

    public static Request buildRequest() {
        LocalDateTime timeStamp = LocalDateTime.now();
        return Request.builder().id(UUID.fromString("21334a2b-79ce-44eb-9168-2d49a744be9c")).correlationId(
            "correlationId").clientId("clientId").authenticatedUserId("userId").requestorId("requestorId").requestType(
            RequestType.CREATE).status(Status.APPROVED).process("process").reference("reference").replaceExisting(true).roleAssignmentId(
            "roleAssignmentId").timestamp(timeStamp).build();
    }

    public static Collection<RequestedRole> buildRequestedRoleCollection() {
        Collection<RequestedRole> requestedRoles = new ArrayList<>();
        requestedRoles.add(buildRequestedRole());
        requestedRoles.add(buildRequestedRole());
        return requestedRoles;
    }

    //TODO update this
    private static RequestedRole buildRequestedRole() {
        LocalDateTime timeStamp = LocalDateTime.now();

        ObjectNode node = JsonNodeFactory.instance.objectNode();

        //Map<String, JsonNode> attributes = new LinkedHashMap<>();
        //attributes.put("jurisdiction", "divorce");
        //attributes.put("region", "north-east");
        //attributes.put("contractType", "SALARIED");

        RoleAssignment roleAssignment = RoleAssignment.builder().actorId(UUID.fromString("21334a2b-79ce-44eb-9168-2d49a744be9c")).actorIdType(
            ActorIdType.IDAM).id(UUID.fromString("21334a2b-79ce-44eb-9168-2d49a744be9a")).roleType(RoleType.CASE).roleName(
                "judge").classification(Classification.PUBLIC).status(Status.APPROVED).readOnly(false).beginTime(
                    timeStamp.plusDays(1)).endTime(timeStamp.plusMonths(1)).created(timeStamp).build();

        RequestedRole requestedRole = new RequestedRole();
        requestedRole.setActorId(roleAssignment.actorId);
        requestedRole.setId(roleAssignment.id);
        requestedRole.setActorIdType(roleAssignment.actorIdType);
        requestedRole.setRoleType(roleAssignment.roleType);
        requestedRole.setRoleName(roleAssignment.roleName);
        requestedRole.setClassification(roleAssignment.classification);
        requestedRole.setStatus(roleAssignment.status);
        requestedRole.setReadOnly(roleAssignment.readOnly);
        requestedRole.setBeginTime(roleAssignment.beginTime);
        requestedRole.setCreated(roleAssignment.created);
        requestedRole.setEndTime(roleAssignment.endTime);
        //requestedRole.setAttributes(attributes);

        return requestedRole;
    }



}
