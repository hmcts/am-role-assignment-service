package uk.gov.hmcts.reform.roleassignment.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.roleassignment.domain.model.Request;
import uk.gov.hmcts.reform.roleassignment.domain.model.RequestedRole;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentRequestResource;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.ActorIdType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Classification;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RequestType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

@Setter
public class TestDataBuilder {

    private TestDataBuilder() {
        //not meant to be instantiated.
    }

    public static AssignmentRequest buildAssignmentRequest() throws IOException {
        return new AssignmentRequest(buildRequest(), buildRequestedRoleCollection());
    }

    public static Request buildRequest() {
        LocalDateTime timeStamp = LocalDateTime.now();
        return Request.builder().id(UUID.fromString("21334a2b-79ce-44eb-9168-2d49a744be9c")).correlationId(
            "correlationId").clientId("clientId").authenticatedUserId(
                UUID.fromString("21334a2b-79ce-44eb-9168-2d49a744be9c")).assignerId(
                    UUID.fromString("21334a2b-79ce-44eb-9168-2d49a744be9c")).requestType(
                        RequestType.CREATE).status(Status.APPROVED).process("process").reference(
                            "reference").replaceExisting(true).roleAssignmentId(
                                "roleAssignmentId").created(timeStamp).build();
    }

    public static Collection<RequestedRole> buildRequestedRoleCollection() throws IOException {
        Collection<RequestedRole> requestedRoles = new ArrayList<>();
        requestedRoles.add(buildRequestedRole());
        requestedRoles.add(buildRequestedRole());
        return requestedRoles;
    }

    //TODO update these, will build all from JSON files instead
    private static RequestedRole buildRequestedRole() throws IOException {

        LocalDateTime timeStamp = LocalDateTime.now();

        HashMap<String, JsonNode> attributes = buildAttributesFromFile();

        RoleAssignment roleAssignment = RoleAssignment.builder().actorId(UUID.fromString("21334a2b-79ce-44eb-9168-2d49a744be9c")).actorIdType(
            ActorIdType.IDAM).id(UUID.fromString("21334a2b-79ce-44eb-9168-2d49a744be9a")).roleType(RoleType.CASE).roleName(
                "judge").classification(Classification.PUBLIC).grantType(GrantType.STANDARD).status(Status.APPROVED).readOnly(false).beginTime(
                    timeStamp.plusDays(1)).endTime(timeStamp.plusMonths(1)).created(timeStamp).build();

        RequestedRole requestedRole = new RequestedRole();
        requestedRole.setActorId(roleAssignment.actorId);
        requestedRole.setId(roleAssignment.id);
        requestedRole.setActorIdType(roleAssignment.actorIdType);
        requestedRole.setRoleType(roleAssignment.roleType);
        requestedRole.setRoleName(roleAssignment.roleName);
        requestedRole.setClassification(roleAssignment.classification);
        requestedRole.setGrantType(roleAssignment.grantType);
        requestedRole.setStatus(roleAssignment.status);
        requestedRole.setReadOnly(roleAssignment.readOnly);
        requestedRole.setBeginTime(roleAssignment.beginTime);
        requestedRole.setCreated(roleAssignment.created);
        requestedRole.setEndTime(roleAssignment.endTime);
        requestedRole.setAttributes(attributes);

        return requestedRole;
    }

    private static HashMap<String, JsonNode> buildAttributesFromFile() throws IOException {
        InputStream inputStream =
            TestDataBuilder.class.getClassLoader().getResourceAsStream("attributes.json");
        assert inputStream != null;
        return new ObjectMapper().readValue(inputStream, new TypeReference<HashMap<String, JsonNode>>() {
        });
    }

    //this maybe not 100% accurate
    public static ResponseEntity<Object> buildResponseEntity(AssignmentRequest roleAssignmentRequest) {
        return ResponseEntity.status(HttpStatus.OK).body(new RoleAssignmentRequestResource(roleAssignmentRequest));
    }

    public static InputStream buildRequestBodyFromFile() throws IOException {
        InputStream inputStream =
            TestDataBuilder.class.getClassLoader().getResourceAsStream("assignmentRequest.json");
        return inputStream;
    }

    public static AssignmentRequest buildAssignmentRequestFromFile() throws IOException {
        InputStream inputStream =
            TestDataBuilder.class.getClassLoader().getResourceAsStream("assignmentRequest.json");
        assert inputStream != null;
        return new ObjectMapper().readValue(inputStream, AssignmentRequest.class);
    }


}
