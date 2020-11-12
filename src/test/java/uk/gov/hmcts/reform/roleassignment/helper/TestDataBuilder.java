package uk.gov.hmcts.reform.roleassignment.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.roleassignment.data.ActorCacheEntity;
import uk.gov.hmcts.reform.roleassignment.data.HistoryEntity;
import uk.gov.hmcts.reform.roleassignment.data.RequestEntity;
import uk.gov.hmcts.reform.roleassignment.data.RoleAssignmentEntity;
import uk.gov.hmcts.reform.roleassignment.domain.model.ActorCache;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.Case;
import uk.gov.hmcts.reform.roleassignment.domain.model.ExistingRoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.QueryRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.Request;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleConfigRole;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.ActorIdType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Classification;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RequestType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.util.JacksonUtils;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.ACCESS_TOKEN;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.CREATE_REQUESTED;
import static uk.gov.hmcts.reform.roleassignment.util.Constants.ROLES_JSON;
import static uk.gov.hmcts.reform.roleassignment.util.JacksonUtils.convertValueJsonNode;

@Setter
public class TestDataBuilder {

    private TestDataBuilder() {
        //not meant to be instantiated.
    }

    public static AssignmentRequest buildAssignmentRequest(Status requestStatus, Status roleStatus,
                                                           Boolean replaceExisting) throws IOException {
        return new AssignmentRequest(buildRequest(requestStatus, replaceExisting),
                                     buildRequestedRoleCollection(roleStatus));
    }

    public static Request buildRequest(Status status, Boolean replaceExisting) {
        return Request.builder()
            .id(UUID.fromString("ab4e8c21-27a0-4abd-aed8-810fdce22adb"))
            .authenticatedUserId("4772dc44-268f-4d0c-8f83-f0fb662aac84")
            .correlationId("38a90097-434e-47ee-8ea1-9ea2a267f51d")
            .assignerId("123e4567-e89b-42d3-a456-556642445678")
            .requestType(RequestType.CREATE)
            .reference("p2")
            .process(("p2"))
            .replaceExisting(replaceExisting)
            .status(status)
            .created(LocalDateTime.now())
            .build();
    }

    public static RoleAssignment buildRoleAssignment(Status status) throws IOException {
        LocalDateTime timeStamp = LocalDateTime.now();
        return RoleAssignment.builder()
            .id(UUID.fromString("9785c98c-78f2-418b-ab74-a892c3ccca9f"))
            .actorId("21334a2b-79ce-44eb-9168-2d49a744be9c")
            .actorIdType(ActorIdType.IDAM)
            .roleType(RoleType.CASE)
            .roleName("judge")
            .classification(Classification.PUBLIC)
            .grantType(GrantType.STANDARD)
            .roleCategory(RoleCategory.JUDICIAL)
            .readOnly(true)
            .beginTime(timeStamp.plusDays(1))
            .endTime(timeStamp.plusMonths(1))
            .reference("reference")
            .process(("process"))
            .statusSequence(10)
            .status(status)
            .created(timeStamp)
            .attributes(JacksonUtils.convertValue(buildAttributesFromFile()))
            .notes(buildNotesFromFile())
            .authorisations(Collections.emptyList())
            .build();
    }

    public static RoleAssignment buildRoleAssignmentUpdated(Status status) throws IOException {
        LocalDateTime timeStamp = LocalDateTime.now();
        return RoleAssignment.builder()
            .id(UUID.fromString("9785c98c-78f2-418b-ab74-a892c3ccca9f"))
            .actorId("21334a2b-79ce-44eb-9168-2d49a744be9c")
            .actorIdType(ActorIdType.IDAM)
            .roleType(RoleType.CASE)
            .roleName("top dog")
            .classification(Classification.PUBLIC)
            .grantType(GrantType.STANDARD)
            .roleCategory(RoleCategory.JUDICIAL)
            .readOnly(true)
            .beginTime(timeStamp.plusDays(1))
            .endTime(timeStamp.plusMonths(1))
            .reference("new ref")
            .process(("new process"))
            .statusSequence(10)
            .status(status)
            .created(timeStamp)
            .attributes(JacksonUtils.convertValue(buildAttributesFromFile()))
            .notes(buildNotesFromFile())
            .build();
    }

    public static ResponseEntity<Object> buildRoleAssignmentResponse(Status requestStatus,
                                                                     Status roleStatus,
                                                                     Boolean replaceExisting) throws Exception {
        return ResponseEntity.status(HttpStatus.OK)
            .body(buildAssignmentRequest(requestStatus, roleStatus, replaceExisting));
    }

    public static Collection<RoleAssignment> buildRequestedRoleCollection(Status status) throws IOException {
        Collection<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(buildRoleAssignment(status));
        requestedRoles.add(buildRoleAssignment(status));
        return requestedRoles;
    }

    public static Collection<RoleAssignment> buildRequestedRoleCollection_Updated(Status status) throws IOException {
        Collection<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(buildRoleAssignmentUpdated(status));
        requestedRoles.add(buildRoleAssignmentUpdated(status));
        return requestedRoles;
    }

    public static JsonNode buildAttributesFromFile() {
        try (InputStream inputStream =
            TestDataBuilder.class.getClassLoader().getResourceAsStream("attributes.json")) {
            assert inputStream != null;
            JsonNode result = new ObjectMapper().readValue(inputStream, new TypeReference<>() {});
            inputStream.close();
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonNode buildNotesFromFile() {
        try (InputStream inputStream =
            TestDataBuilder.class.getClassLoader().getResourceAsStream("notes.json")) {
            assert inputStream != null;
            JsonNode result = new ObjectMapper().readValue(inputStream, new TypeReference<>() {});
            inputStream.close();
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List<RoleConfigRole> buildRolesFromFile() throws IOException {
        try (InputStream input = TestDataBuilder.class.getClassLoader().getResourceAsStream(ROLES_JSON)) {
            CollectionType listType = new ObjectMapper().getTypeFactory().constructCollectionType(
                ArrayList.class,
                RoleConfigRole.class
            );
            assert input != null;
            return new ObjectMapper().readValue(input, listType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static RequestEntity buildRequestEntity(Request request) {
        return RequestEntity.builder()
            .id(request.getId())
            .assignerId(request.getAssignerId())
            .correlationId(request.getCorrelationId())
            .status(request.getStatus().toString())
            .process(request.getProcess())
            .reference(request.getProcess())
            .authenticatedUserId(request.getAuthenticatedUserId())
            .clientId(request.getClientId())
            .assignerId(request.getAssignerId())
            .replaceExisting(request.isReplaceExisting())
            .requestType(request.getRequestType().toString())
            .created(request.getCreated())
            .log(request.getLog())
            .build();
    }

    public static HistoryEntity buildHistoryIntoEntity(RoleAssignment model, RequestEntity requestEntity) {
        return HistoryEntity.builder().id(model.getId()).actorId(model.getActorId())
            .actorIdType(model.getActorIdType().toString())
            .attributes(JacksonUtils.convertValueJsonNode(model.getAttributes()))
            .beginTime(model.getBeginTime())
            .classification(model.getClassification().toString())
            .endTime(model.getEndTime())
            .grantType(model.getGrantType().toString())
            .roleName(model.getRoleName())
            .roleType(model.getRoleType().toString())
            .readOnly(model.isReadOnly())
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
            .readOnly(model.isReadOnly())
            .build();
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
        JsonNode attributes = buildAttributesFromFile();
        return ActorCacheEntity.builder()
            .actorId("21334a2b-79ce-44eb-9168-2d49a744be9c")
            .etag(1)
            .roleAssignmentResponse(JacksonUtils.convertValueJsonNode(attributes))
            .build();
    }

    public static ActorCache buildActorCache() throws IOException {
        HashSet<RoleAssignmentEntity> mySet = new HashSet<>();
        mySet.add(TestDataBuilder.buildRoleAssignmentEntitySet());
        return ActorCache.builder()
            .actorId("21334a2b-79ce-44eb-9168-2d49a744be9c")
            .etag(1)
            .roleAssignments(mySet)
            .build();
    }

    private static RoleAssignmentEntity buildRoleAssignmentEntitySet() throws IOException {
        LocalDateTime timeStamp = LocalDateTime.now();
        return RoleAssignmentEntity.builder()
            .actorId("21334a2b-79ce-44eb-9168-2d49a744be9c")
            .actorIdType(ActorIdType.IDAM.name())
            .roleType(RoleType.CASE.name())
            .roleName("judge")
            .classification(Classification.PUBLIC.name())
            .grantType(GrantType.STANDARD.name())
            .roleCategory(RoleCategory.JUDICIAL.name())
            .readOnly(true)
            .beginTime(timeStamp.plusDays(1))
            .endTime(timeStamp.plusMonths(1))
            .created(timeStamp)
            .attributes(buildAttributesFromFile())
            .build();
    }


    @NotNull
    public static ActorCache prepareActorCache(RoleAssignment roleAssignment) {
        ActorCache actorCache = new ActorCache();
        actorCache.setActorId(roleAssignment.getActorId());
        Set<RoleAssignmentEntity> roleAssignmentEntities = new HashSet<>();
        roleAssignmentEntities.add(convertRoleAssignmentToEntity(roleAssignment));
        actorCache.setRoleAssignments(roleAssignmentEntities);
        return actorCache;
    }


    public static HistoryEntity buildHistoryEntity(RoleAssignment roleAssignment, RequestEntity requestEntity) {
        return HistoryEntity.builder()
            .actorId(roleAssignment.getActorId())
            .actorIdType(roleAssignment.getActorIdType().toString())
            .classification(roleAssignment.getClassification().toString())
            .grantType(roleAssignment.getGrantType().toString())
            .roleName(roleAssignment.getRoleName())
            .roleType(roleAssignment.getRoleType().toString())
            .roleCategory(roleAssignment.getRoleCategory().toString())
            .readOnly(roleAssignment.isReadOnly())
            .status(roleAssignment.getStatus().toString())
            .requestEntity(requestEntity)
            .process(roleAssignment.getProcess())
            .reference(roleAssignment.getReference())
            .created(roleAssignment.getCreated())
            .beginTime(roleAssignment.getBeginTime())
            .endTime(roleAssignment.getEndTime())
            .attributes(JacksonUtils.convertValueJsonNode(roleAssignment.getAttributes()))
            .notes(roleAssignment.getNotes())
            .sequence(roleAssignment.getStatusSequence())
            .log(roleAssignment.getLog())
            .authorisations("dev,tester")
            .build();
    }

    public static RoleAssignmentEntity buildRoleAssignmentEntity(RoleAssignment roleAssignment) {
        return RoleAssignmentEntity.builder()
            .id(roleAssignment.getId())
            .actorId(roleAssignment.getActorId())
            .actorIdType(roleAssignment.getActorIdType().toString())
            .attributes(JacksonUtils.convertValueJsonNode(roleAssignment.getAttributes()))
            .beginTime(roleAssignment.getBeginTime())
            .classification(roleAssignment.getClassification().toString())
            .endTime(roleAssignment.getEndTime())
            .created(roleAssignment.getCreated())
            .grantType(roleAssignment.getGrantType().toString())
            .roleName(roleAssignment.getRoleName())
            .roleType(roleAssignment.getRoleType().toString())
            .readOnly(roleAssignment.isReadOnly())
            .roleCategory(roleAssignment.getRoleCategory().toString())
            .authorisations(String.join(";", roleAssignment.getAuthorisations()))
            .build();
    }

    public static UserInfo buildUserInfo(String uuid) throws IOException {
        List<String> list = new ArrayList<>();
        List<RoleConfigRole> roles = TestDataBuilder.buildRolesFromFile();
        for (RoleConfigRole role : roles) {
            list.add(role.toString());
        }
        return UserInfo.builder().sub("sub").uid(uuid)
            .name("James").givenName("007").familyName("Bond").roles(list).build();
    }

    public static Jwt buildJwt() {
        return Jwt.withTokenValue("token_value").header("head", "head")
            .claim("tokenName", ACCESS_TOKEN).build();
    }

    public static Case buildCase() {
        return Case.builder()
            .version(1)
            .reference(1L)
            .id("1234").build();
    }

    public static AssignmentRequest createRoleAssignmentRequest(
        boolean replaceExisting, boolean readOnly) throws IOException {
        return new AssignmentRequest(buildRequestForRoleAssignment(replaceExisting),
                                     buildRequestedRoles(readOnly));
    }

    public static Request buildRequestForRoleAssignment(boolean replaceExisting) {
        return Request.builder()
            .assignerId("123e4567-e89b-42d3-a456-556642445678")
            .reference("S-052")
            .process(("S-052"))
            .replaceExisting(replaceExisting)
            .build();
    }

    public static Collection<RoleAssignment> buildRequestedRoles(boolean readOnly) throws IOException {
        Collection<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(buildRoleAssignments(readOnly));
        return requestedRoles;
    }

    public static RoleAssignment buildRoleAssignments(boolean readOnly) throws IOException {
        LocalDateTime timeStamp = LocalDateTime.now();
        return RoleAssignment.builder()
            .actorId("123e4567-e89b-42d3-a456-556642445612")
            .actorIdType(ActorIdType.IDAM)
            .roleType(RoleType.CASE)
            .roleName("judge")
            .classification(Classification.PUBLIC)
            .grantType(GrantType.SPECIFIC)
            .roleCategory(RoleCategory.JUDICIAL)
            .readOnly(readOnly)
            .beginTime(timeStamp.plusDays(1))
            .endTime(timeStamp.plusMonths(1))
            .attributes(JacksonUtils.convertValue(buildAttributesFromFile()))
            .notes(buildNotesFromFile())
            .authorisations(Collections.emptyList())
            .build();
    }

    public static QueryRequest createQueryRequest() {
        Map<String, List<String>> attributes = new HashMap<>();
        List<String> regions = Arrays.asList("London", "JAPAN");
        List<String> contractTypes = Arrays.asList("SALARIED", "Non SALARIED");
        attributes.put("region", regions);
        attributes.put("contractType", contractTypes);

        return QueryRequest.builder()
            .actorId(Arrays.asList("123e4567-e89b-42d3-a456-556642445612"))

            .roleType(Arrays.asList(RoleType.CASE.toString()))
            .roleName(Arrays.asList("judge"))
            .classification(Arrays.asList(Classification.PUBLIC.toString()))
            .grantType(Arrays.asList(GrantType.SPECIFIC.toString()))
            .roleCategory(Arrays.asList(RoleCategory.JUDICIAL.toString()))
            .validAt(LocalDateTime.now())
            .attributes(attributes)
            .authorisations(Arrays.asList("dev"))
            .build();


    }

    public static ExistingRoleAssignment buildExistingRoleForIAC(String actorId, String roleName) {
        Map<String,JsonNode> attributes = new HashMap<>();
        attributes.put("jurisdiction",convertValueJsonNode("IA"));
        return ExistingRoleAssignment.builder()
            .actorId(actorId)
            .roleType(RoleType.ORGANISATION)
            .roleName(roleName)
            .attributes(attributes)
            .build();

    }

    public static RoleAssignment getRequestedCaseRole(RoleCategory roleCategory, String roleName, GrantType grantType) {
        return RoleAssignment.builder()
                                 .id(UUID.randomUUID())
                                 .actorId(UUID.randomUUID().toString())
                                 .actorIdType(ActorIdType.IDAM)
                                 .roleCategory(roleCategory)
                                 .roleType(RoleType.CASE)
                                 .roleName(roleName)
                                 .grantType(grantType)
                                 .classification(Classification.PUBLIC)
                                 .readOnly(true)
                                 .status(CREATE_REQUESTED)
                                 .attributes(new HashMap<String, JsonNode>())
                                 .build();
    }


    public static List<RoleAssignment> getRequestedOrgRole() {
        return Arrays.asList(RoleAssignment.builder()
                                 .id(UUID.fromString("9785c98c-78f2-418b-ab74-a892c3ccca9f"))
                                 .actorId("4772dc44-268f-4d0c-8f83-f0fb662aac83")
                                 .actorIdType(ActorIdType.IDAM)
                                 .classification(Classification.PUBLIC)
                                 .readOnly(true)
                                 .status(CREATE_REQUESTED)
                                 .attributes(new HashMap<String, JsonNode>())
                                 .build());
    }

}
