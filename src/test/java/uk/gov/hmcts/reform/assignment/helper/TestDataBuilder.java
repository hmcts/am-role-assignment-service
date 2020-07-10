package uk.gov.hmcts.reform.assignment.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.assignment.data.cachecontrol.ActorCacheEntity;
import uk.gov.hmcts.reform.assignment.data.roleassignment.HistoryEntity;
import uk.gov.hmcts.reform.assignment.data.roleassignment.RequestEntity;
import uk.gov.hmcts.reform.assignment.data.roleassignment.RoleAssignmentEntity;
import uk.gov.hmcts.reform.assignment.domain.model.ActorCache;
import uk.gov.hmcts.reform.assignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.assignment.domain.model.Request;
import uk.gov.hmcts.reform.assignment.domain.model.Role;
import uk.gov.hmcts.reform.assignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.assignment.domain.model.RoleAssignmentRequestResource;
import uk.gov.hmcts.reform.assignment.domain.model.RoleAssignmentResource;
import uk.gov.hmcts.reform.assignment.domain.model.enums.ActorIdType;
import uk.gov.hmcts.reform.assignment.domain.model.enums.Classification;
import uk.gov.hmcts.reform.assignment.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.assignment.domain.model.enums.RequestType;
import uk.gov.hmcts.reform.assignment.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.assignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.assignment.util.JacksonUtils;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static uk.gov.hmcts.reform.assignment.apihelper.Constants.ROLES_JSON;

@Setter
public class TestDataBuilder {

    private TestDataBuilder() {
        //not meant to be instantiated.
    }

    public static AssignmentRequest buildAssignmentRequest() throws IOException {
        return new AssignmentRequest(buildRequest(), buildRequestedRoleCollection());
    }

    private static Request buildRequest() {
        LocalDateTime timeStamp = LocalDateTime.now();
        return Request.builder()
            .id(UUID.fromString("21334a2b-79ce-44eb-9168-2d49a744be9c"))
            .correlationId("correlationId")
            .clientId("clientId")
            .authenticatedUserId(UUID.fromString("21334a2b-79ce-44eb-9168-2d49a744be9c"))
            .assignerId(UUID.fromString("21334a2b-79ce-44eb-9168-2d49a744be9c"))
            .requestType(RequestType.CREATE)
            .reference("reference")
            .process(("process"))
            .status(Status.CREATED)
            .replaceExisting(true)
            .roleAssignmentId(UUID.fromString("21334a2b-79ce-44eb-9168-2d49a744be9c"))
            .created(timeStamp).build();
    }

    public static Collection<RoleAssignment> buildRequestedRoleCollection() throws IOException {
        Collection<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(buildRequestedRole());
        requestedRoles.add(buildRequestedRole());
        return requestedRoles;
    }

    public static RoleAssignment buildRequestedRole() throws IOException {

        LocalDateTime timeStamp = LocalDateTime.now();

        HashMap<String, JsonNode> attributes = buildAttributesFromFile();

        RoleAssignment requestedRole = new RoleAssignment();
        requestedRole.setActorId(UUID.fromString("21334a2b-79ce-44eb-9168-2d49a744be9c"));
        requestedRole.setId(UUID.fromString("21334a2b-79ce-44eb-9168-2d49a744be9a"));
        requestedRole.setActorIdType(ActorIdType.IDAM);
        requestedRole.setRoleType(RoleType.CASE);
        requestedRole.setRoleName("RoleName");
        requestedRole.setClassification(Classification.PUBLIC);
        requestedRole.setGrantType(GrantType.STANDARD);
        requestedRole.setReadOnly(false);
        requestedRole.setBeginTime(timeStamp.plusDays(1));
        requestedRole.setCreated(timeStamp);
        requestedRole.setEndTime(timeStamp.plusMonths(1));
        requestedRole.setAttributes(attributes);
        requestedRole.setStatus(Status.CREATED);

        return requestedRole;
    }

    private static HashMap<String, JsonNode> buildAttributesFromFile() throws IOException {
        InputStream inputStream =
            TestDataBuilder.class.getClassLoader().getResourceAsStream("attributes.json");
        assert inputStream != null;
        return new ObjectMapper().readValue(inputStream, new TypeReference<HashMap<String, JsonNode>>() {
        });
    }

    public static List<Role> buildRolesFromFile() throws IOException {
        try (InputStream input = TestDataBuilder.class.getClassLoader().getResourceAsStream(ROLES_JSON)) {
            CollectionType listType = new ObjectMapper().getTypeFactory().constructCollectionType(
                ArrayList.class,
                Role.class
            );
            assert input != null;
            List<Role> allRoles = new ObjectMapper().readValue(input, listType);
            return allRoles;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static RequestEntity buildRequestEntity(Request request) {
        return RequestEntity.builder()
            .correlationId(request.getCorrelationId())
            .status(request.getStatus().toString())
            .process(request.getProcess())
            .reference(request.getProcess())
            .authenticatedUserId(request.getAuthenticatedUserId())
            .clientId(request.getClientId())
            .assignerId(request.getAssignerId())
            .replaceExisting(request.replaceExisting)
            .requestType(request.getRequestType().toString())
            .created(request.getCreated())
            .log(request.getLog())
            .build();
    }

    public static HistoryEntity buildHistoryIntoEntity(RoleAssignment model, RequestEntity requestEntity) {
        return HistoryEntity.builder().actorId(model.getActorId())
            .actorIdType(model.getActorIdType().toString())
            .attributes(JacksonUtils.convertValueJsonNode(model.getAttributes()))
            .beginTime(model.getBeginTime())
            .classification(model.getClassification().toString())
            .endTime(model.getEndTime())
            .grantType(model.getGrantType().toString())
            .roleName(model.getRoleName())
            .roleType(model.getRoleType().toString())
            .readOnly(model.readOnly)
            .status(model.getStatus().toString())
            .requestEntity(requestEntity)
            .process(model.getProcess())
            .reference(model.getReference())
            .created(model.getCreated())
            .notes(model.getNotes())
            .build();
    }

    public static RoleAssignmentEntity convertRoleAssignmentToEntity(RoleAssignment model) {
        return RoleAssignmentEntity.builder()
            .id(model.getId())
            .actorId(model.getActorId())
            .actorIdType(model.getActorIdType().toString())
            .attributes(JacksonUtils.convertValueJsonNode(model.getAttributes()))
            .beginTime(model.getBeginTime())
            .classification(model.getClassification().toString())
            .endTime(model.getEndTime())
            .created(model.getCreated())
            .grantType(model.getGrantType().toString())
            .roleName(model.getRoleName())
            .roleType(model.getRoleType().toString())
            .readOnly(model.readOnly)
            .build();
    }

    public static RoleAssignment convertRoleAssignmentEntityInModel(RoleAssignmentEntity roleAssignmentEntity) {

        RoleAssignment existingRole = new RoleAssignment();
        existingRole.setId(roleAssignmentEntity.getId());
        existingRole.setActorId(roleAssignmentEntity.getActorId());
        existingRole.setActorIdType(ActorIdType.valueOf(roleAssignmentEntity.getActorIdType()));
        existingRole.setAttributes(JacksonUtils.convertValue(roleAssignmentEntity.getAttributes()));
        existingRole.setBeginTime(roleAssignmentEntity.getBeginTime());
        existingRole.setEndTime(roleAssignmentEntity.getEndTime());
        existingRole.setCreated(roleAssignmentEntity.getCreated());
        existingRole.setClassification(Classification.valueOf(roleAssignmentEntity.getClassification()));
        existingRole.setGrantType(GrantType.valueOf(roleAssignmentEntity.getGrantType()));
        existingRole.setReadOnly(roleAssignmentEntity.isReadOnly());
        existingRole.setRoleName(roleAssignmentEntity.getRoleName());
        existingRole.setRoleType(RoleType.valueOf(roleAssignmentEntity.getRoleType()));
        return existingRole;
    }

    public static RoleAssignment convertHistoryEntityInModel(HistoryEntity historyEntity) {

        RoleAssignment requestedrole = new RoleAssignment();
        requestedrole.setId(historyEntity.getId());
        requestedrole.setActorId(historyEntity.getActorId());
        requestedrole.setActorIdType(ActorIdType.valueOf(historyEntity.getActorIdType()));
        requestedrole.setAttributes(JacksonUtils.convertValue(historyEntity.getAttributes()));
        requestedrole.setBeginTime(historyEntity.getBeginTime());
        requestedrole.setEndTime(historyEntity.getEndTime());
        requestedrole.setCreated(historyEntity.getCreated());
        requestedrole.setClassification(Classification.valueOf(historyEntity.getClassification()));
        requestedrole.setGrantType(GrantType.valueOf(historyEntity.getGrantType()));
        requestedrole.setReadOnly(historyEntity.isReadOnly());
        requestedrole.setRoleName(historyEntity.getRoleName());
        requestedrole.setRoleType(RoleType.valueOf(historyEntity.getRoleType()));
        requestedrole.setStatus(Status.valueOf(historyEntity.getStatus()));
        return requestedrole;

    }

    public static ActorCacheEntity buildActorCacheEntity() throws IOException {
        HashMap<String, JsonNode> attributes = buildAttributesFromFile();
        return ActorCacheEntity.builder()
            .actorId(UUID.fromString("21334a2b-79ce-44eb-9168-2d49a744be9c"))
            .etag(1)
            .roleAssignmentResponse(JacksonUtils.convertValueJsonNode(attributes))
            .build();

    }

    public static ResponseEntity<Object> buildRoleAssignmentResponse() throws Exception {
        List<RoleAssignment> roleAssignments = (List<RoleAssignment>) buildRequestedRoleCollection();
        return ResponseEntity.status(HttpStatus.OK).body(new RoleAssignmentResource(
            roleAssignments,
            UUID.randomUUID()
        ));
    }

    public ActorCacheEntity convertActorCacheToEntity(ActorCache actorCache) {
        return ActorCacheEntity.builder()
            .actorId(actorCache.getActorId())
            .etag(actorCache.getEtag())
            .roleAssignmentResponse(JacksonUtils.convertValueJsonNode(actorCache.roleAssignments))
            .build();

    }

    @NotNull
    public static ActorCache prepareActorCache(RoleAssignment roleAssignment) {
        ActorCache actorCache = new ActorCache();
        actorCache.setActorId(roleAssignment.actorId);
        Set<RoleAssignmentEntity> roleAssignmentEntities = new HashSet<>();
        roleAssignmentEntities.add(convertRoleAssignmentToEntity(roleAssignment));
        actorCache.setRoleAssignments(roleAssignmentEntities);
        return actorCache;
    }

    //update this
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
