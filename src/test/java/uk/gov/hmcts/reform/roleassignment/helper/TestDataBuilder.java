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
import uk.gov.hmcts.reform.roleassignment.domain.model.Assignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.Case;
import uk.gov.hmcts.reform.roleassignment.domain.model.ExistingRoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.QueryRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.Request;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentRequestResource;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentResource;
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
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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

import static java.time.LocalDateTime.now;
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

    public static AssignmentRequest buildEmptyAssignmentRequest(Status roleStatus) throws IOException {
        return new AssignmentRequest(Request.builder().build(),  buildRequestedRoleCollection(roleStatus));
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
            .created(ZonedDateTime.now())
            .build();
    }

    public static RoleAssignment buildRoleAssignment_CustomActorId(Status status, String actorId, String path,
                                                                   RoleType roleType, String roleName)
        throws IOException {
        ZonedDateTime timeStamp = ZonedDateTime.now();
        return RoleAssignment.builder()
            .id(UUID.fromString("3ed4f960-e50b-4127-af30-47821d5799f7"))
            .actorId(actorId)
            .actorIdType(ActorIdType.IDAM)
            .roleType(roleType)
            .roleName(roleName)
            .classification(Classification.PRIVATE)
            .grantType(GrantType.STANDARD)
            .roleCategory(RoleCategory.LEGAL_OPERATIONS)
            .readOnly(false)
            .beginTime(timeStamp.plusDays(1))
            .created(timeStamp)
            .endTime(timeStamp.plusDays(3))
            .reference("reference")
            .process(("process"))
            .statusSequence(10)
            .status(status)
            .attributes(JacksonUtils.convertValue(buildAttributesFromFile(path)))
            .authorisations(Collections.emptyList())
            .build();
    }

    public static List<RoleAssignment> buildRoleAssignmentList_Custom(Status status, String actorId, String path,
                                                                      RoleType roleType, String roleName)
        throws IOException {
        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(buildRoleAssignment_CustomActorId(status, actorId, path, roleType, roleName));
        return requestedRoles;
    }

    public static List<Assignment> buildAssignmentList(Status status, String actorId, String path, RoleType roleType,
                                                       String roleName)
        throws IOException {
        List<Assignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(buildRoleAssignment_CustomActorId(status, actorId, path, roleType, roleName));
        return requestedRoles;
    }

    public static List<Assignment> buildMultiAssignmentList(Status status, String actorId, String path,
                                                            RoleType roleType, String roleName)
        throws IOException {
        List<Assignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(buildRoleAssignment_CustomActorId(status, actorId, path, roleType, roleName));
        requestedRoles.add(buildRoleAssignment_CustomActorId(status, actorId, path, roleType, roleName));
        return requestedRoles;
    }

    public static RoleAssignment buildRoleAssignment(Status status) throws IOException {
        ZonedDateTime timeStamp = ZonedDateTime.now(ZoneOffset.UTC);
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
            .created(ZonedDateTime.now())
            .attributes(JacksonUtils.convertValue(buildAttributesFromFile("attributes.json")))
            .notes(buildNotesFromFile())
            .authorisations(Collections.emptyList())
            .build();
    }

    public static RoleAssignment buildRoleAssignmentUpdated(Status status) throws IOException {
        ZonedDateTime timeStamp = ZonedDateTime.now(ZoneOffset.UTC);
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
            .created(ZonedDateTime.now())
            .attributes(JacksonUtils.convertValue(buildAttributesFromFile("attributes.json")))
            .notes(buildNotesFromFile())
            .build();
    }

    public static ResponseEntity<Object> buildRoleAssignmentResponse(Status requestStatus,
                                                                     Status roleStatus,
                                                                     Boolean replaceExisting) throws Exception {
        return ResponseEntity.status(HttpStatus.OK)
            .body(buildAssignmentRequest(requestStatus, roleStatus, replaceExisting));
    }

    public static ResponseEntity<RoleAssignmentRequestResource> buildAssignmentRequestResource(Status requestStatus,
                                                                                               Status roleStatus,
                                                                                               Boolean replaceExisting)
        throws Exception {
        return ResponseEntity.status(HttpStatus.OK)
            .body(new RoleAssignmentRequestResource(buildAssignmentRequest(requestStatus,
                                                                           roleStatus,
                                                                           replaceExisting)));
    }

    public static ResponseEntity<RoleAssignmentResource> buildResourceRoleAssignmentResponse(
        Status roleStatus) throws Exception {
        return ResponseEntity.status(HttpStatus.OK)
            .body(new RoleAssignmentResource(Arrays.asList(buildRoleAssignment(roleStatus)),""));
    }

    public static ResponseEntity<RoleAssignmentResource> buildResourceRoleAssignmentResponse_Custom(
        Status roleStatus, String actorId) throws Exception {
        return ResponseEntity.status(HttpStatus.OK)
            .body(new RoleAssignmentResource(Arrays.asList(buildRoleAssignment_CustomActorId(
                roleStatus, actorId,"attributes.json", RoleType.ORGANISATION,
                "senior-tribunal-caseworker")), actorId));
    }

    public static Collection<RoleAssignment> buildRequestedRoleCollection(Status status) throws IOException {
        Collection<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(buildRoleAssignment(status));
        requestedRoles.add(buildRoleAssignment(status));
        return requestedRoles;
    }

    public static Collection<RoleAssignment> buildRoleAssignmentCollection_Pact(Status status) throws IOException {
        Collection<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(buildRoleAssignment(status));
        return requestedRoles;
    }

    public static Collection<RoleAssignment> buildRequestedRoleCollection_Updated(Status status) throws IOException {
        Collection<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(buildRoleAssignmentUpdated(status));
        requestedRoles.add(buildRoleAssignmentUpdated(status));
        return requestedRoles;
    }

    public static JsonNode buildAttributesFromFile(String path) {
        try (InputStream inputStream =
                 TestDataBuilder.class.getClassLoader().getResourceAsStream(path)) {
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
            .created(request.getCreated().toLocalDateTime())
            .log(request.getLog())
            .build();
    }

    public static HistoryEntity buildHistoryIntoEntity(RoleAssignment model, RequestEntity requestEntity) {
        return HistoryEntity.builder().id(model.getId()).actorId(model.getActorId())
            .actorIdType(model.getActorIdType().toString())
            .attributes(JacksonUtils.convertValueJsonNode(model.getAttributes()))
            .beginTime(model.getBeginTime().toLocalDateTime())
            .classification(model.getClassification().toString())
            .endTime(model.getEndTime().toLocalDateTime())
            .grantType(model.getGrantType().toString())
            .roleName(model.getRoleName())
            .roleType(model.getRoleType().toString())
            .readOnly(model.isReadOnly())
            .status(model.getStatus().toString())
            .requestEntity(requestEntity)
            .process(model.getProcess())
            .reference(model.getReference())
            .created(model.getCreated().toLocalDateTime())
            .notes(model.getNotes())
            .build();
    }

    public static RoleAssignmentEntity convertRoleAssignmentToEntity(RoleAssignment model) {
        return RoleAssignmentEntity.builder()
            .id(model.getId())
            .actorId(model.getActorId())
            .actorIdType(model.getActorIdType().toString())
            .attributes(JacksonUtils.convertValueJsonNode(model.getAttributes()))
            .beginTime(model.getBeginTime().toLocalDateTime())
            .classification(model.getClassification().toString())
            .endTime(model.getEndTime().toLocalDateTime())
            .created(model.getCreated().toLocalDateTime())
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
        requestedrole.setBeginTime(historyEntity.getBeginTime().atZone(ZoneId.of("UTC")));
        requestedrole.setEndTime(historyEntity.getEndTime().atZone(ZoneId.of("UTC")));
        requestedrole.setCreated(historyEntity.getCreated().atZone(ZoneId.of("UTC")));
        requestedrole.setClassification(Classification.valueOf(historyEntity.getClassification()));
        requestedrole.setGrantType(GrantType.valueOf(historyEntity.getGrantType()));
        requestedrole.setReadOnly(historyEntity.isReadOnly());
        requestedrole.setRoleName(historyEntity.getRoleName());
        requestedrole.setRoleType(RoleType.valueOf(historyEntity.getRoleType()));
        requestedrole.setStatus(Status.valueOf(historyEntity.getStatus()));
        return requestedrole;

    }

    public static ActorCacheEntity buildActorCacheEntity() throws IOException {
        JsonNode attributes = buildAttributesFromFile("attributes.json");
        return ActorCacheEntity.builder()
            .actorId("21334a2b-79ce-44eb-9168-2d49a744be9c")
            .etag(1)
            .roleAssignmentResponse(JacksonUtils.convertValueJsonNode(attributes))
            .build();
    }

    public static ActorCache buildActorCache() throws IOException {
        return ActorCache.builder()
            .actorId("21334a2b-79ce-44eb-9168-2d49a744be9c")
            .etag(1)
            .build();
    }

    private static RoleAssignmentEntity buildRoleAssignmentEntitySet() throws IOException {
        return RoleAssignmentEntity.builder()
            .actorId("21334a2b-79ce-44eb-9168-2d49a744be9c")
            .actorIdType(ActorIdType.IDAM.name())
            .roleType(RoleType.CASE.name())
            .roleName("judge")
            .classification(Classification.PUBLIC.name())
            .grantType(GrantType.STANDARD.name())
            .roleCategory(RoleCategory.JUDICIAL.name())
            .readOnly(true)
            .beginTime(now().plusDays(1))
            .endTime(now().plusMonths(1))
            .created(now())
            .attributes(buildAttributesFromFile("attributes.json"))
            .build();
    }


    @NotNull
    public static ActorCache prepareActorCache(RoleAssignment roleAssignment) {
        ActorCache actorCache = new ActorCache();
        actorCache.setActorId(roleAssignment.getActorId());
        Set<RoleAssignmentEntity> roleAssignmentEntities = new HashSet<>();
        roleAssignmentEntities.add(convertRoleAssignmentToEntity(roleAssignment));
        return actorCache;
    }


    public static HistoryEntity buildHistoryEntity(RoleAssignment roleAssignment, RequestEntity requestEntity) {
        String[] auths = {"dev","test"};
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
            .created(roleAssignment.getCreated().toLocalDateTime())
            .beginTime(roleAssignment.getBeginTime().toLocalDateTime())
            .endTime(roleAssignment.getEndTime().toLocalDateTime())
            .attributes(JacksonUtils.convertValueJsonNode(roleAssignment.getAttributes()))
            .notes(roleAssignment.getNotes())
            .sequence(roleAssignment.getStatusSequence())
            .log(roleAssignment.getLog())
            .authorisations(auths)
            .build();
    }

    public static RoleAssignmentEntity buildRoleAssignmentEntity(RoleAssignment roleAssignment) {
        return RoleAssignmentEntity.builder()
            .id(roleAssignment.getId())
            .actorId(roleAssignment.getActorId())
            .actorIdType(roleAssignment.getActorIdType().toString())
            .attributes(JacksonUtils.convertValueJsonNode(roleAssignment.getAttributes()))
            .beginTime(roleAssignment.getBeginTime().toLocalDateTime())
            .classification(roleAssignment.getClassification().toString())
            .endTime(roleAssignment.getEndTime().toLocalDateTime())
            .created(roleAssignment.getCreated().toLocalDateTime())
            .grantType(roleAssignment.getGrantType().toString())
            .roleName(roleAssignment.getRoleName())
            .roleType(roleAssignment.getRoleType().toString())
            .readOnly(roleAssignment.isReadOnly())
            .roleCategory(roleAssignment.getRoleCategory().toString())
            .authorisations(roleAssignment.getAuthorisations().toArray(new String[0]))
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
        ZonedDateTime timeStamp = ZonedDateTime.now(ZoneOffset.UTC);
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
            .attributes(JacksonUtils.convertValue(buildAttributesFromFile("attributes.json")))
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
            .validAt(now())
            .attributes(attributes)
            .authorisations(Arrays.asList("dev"))
            .build();


    }

    public static ExistingRoleAssignment buildExistingRoleForIAC(String actorId, String roleName,
                                                                 RoleCategory roleCategory) {
        Map<String,JsonNode> attributes = new HashMap<>();
        attributes.put("jurisdiction",convertValueJsonNode("IA"));
        return ExistingRoleAssignment.builder()
            .actorId(actorId)
            .roleType(RoleType.ORGANISATION)
            .roleCategory(roleCategory)
            .roleName(roleName)
            .attributes(attributes)
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
