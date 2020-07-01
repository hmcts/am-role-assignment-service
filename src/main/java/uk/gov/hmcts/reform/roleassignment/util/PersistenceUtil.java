package uk.gov.hmcts.reform.roleassignment.util;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignment.data.cachecontrol.ActorCacheEntity;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.HistoryEntity;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.RequestEntity;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.RoleAssignmentEntity;
import uk.gov.hmcts.reform.roleassignment.domain.model.ActorCache;
import uk.gov.hmcts.reform.roleassignment.domain.model.Request;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.ActorIdType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Classification;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;

@Service
public class PersistenceUtil {

    public HistoryEntity convertRoleAssignmentToHistoryEntity(RoleAssignment roleAssignment,
                                                              RequestEntity requestEntity) {
        return HistoryEntity.builder()
            .actorId(roleAssignment.getActorId())
            .actorIdType(roleAssignment.getActorIdType().toString())
            .attributes(JacksonUtils.convertValueJsonNode(roleAssignment.getAttributes()))
            .beginTime(roleAssignment.getBeginTime())
            .classification(roleAssignment.getClassification().toString())
            .endTime(roleAssignment.getEndTime())
            .grantType(roleAssignment.getGrantType().toString())
            .roleName(roleAssignment.getRoleName())
            .roleType(roleAssignment.getRoleType().toString())
            .readOnly(roleAssignment.readOnly)
            .status(roleAssignment.getStatus().toString())
            .requestEntity(requestEntity)
            .process(roleAssignment.getProcess())
            .reference(roleAssignment.getReference())
            .created(roleAssignment.getCreated())
            .notes(roleAssignment.getNotes())
            .build();


    }

    public RequestEntity convertRequestToEntity(Request request) {
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

    public RoleAssignmentEntity convertRoleAssignmentToEntity(RoleAssignment model) {
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

    public ActorCacheEntity convertActorCacheToEntity(ActorCache actorCache) {
        return ActorCacheEntity.builder()
            .actorId(actorCache.getActorId())
            .etag(actorCache.getEtag())
            .roleAssignmentResponse(JacksonUtils.convertValueJsonNode(actorCache.roleAssignments))
            .build();

    }

    public RoleAssignment convertHistoryEntityToRoleAssignment(HistoryEntity historyEntity) {
        return RoleAssignment.builder()
            .id(historyEntity.getId())
            .actorIdType(ActorIdType.valueOf(historyEntity.getActorIdType()))
            .actorId(historyEntity.getActorId())
            .classification(Classification.valueOf(historyEntity.getClassification()))
            .grantType(GrantType.valueOf(historyEntity.getGrantType()))
            .readOnly(historyEntity.isReadOnly())
            .roleName(historyEntity.getRoleName())
            .roleType(RoleType.valueOf(historyEntity.getRoleType()))
            .roleCategory(RoleCategory.valueOf(historyEntity.getRoleCategory()))
            .status(Status.valueOf(historyEntity.getStatus()))
            .statusSequence(historyEntity.getSequence())
            .process(historyEntity.getProcess())
            .reference(historyEntity.getReference())
            .beginTime(historyEntity.getBeginTime())
            .endTime(historyEntity.getEndTime())
            .created(historyEntity.getCreated())
            .log(historyEntity.getLog())
            .attributes(JacksonUtils.convertValue(historyEntity.getAttributes()))
            .notes(historyEntity.getNotes())
            .build();
    }

    public RoleAssignment convertEntityToRoleAssignment(RoleAssignmentEntity roleAssignmentEntity) {

        RoleAssignment requestedRole = new RoleAssignment();
        requestedRole.setId(roleAssignmentEntity.getId());
        requestedRole.setActorId(roleAssignmentEntity.getActorId());
        requestedRole.setActorIdType(ActorIdType.valueOf(roleAssignmentEntity.getActorIdType()));
        requestedRole.setAttributes(JacksonUtils.convertValue(roleAssignmentEntity.getAttributes()));
        requestedRole.setBeginTime(roleAssignmentEntity.getBeginTime());
        requestedRole.setEndTime(roleAssignmentEntity.getEndTime());
        requestedRole.setCreated(roleAssignmentEntity.getCreated());
        requestedRole.setClassification(Classification.valueOf(roleAssignmentEntity.getClassification()));
        requestedRole.setGrantType(GrantType.valueOf(roleAssignmentEntity.getGrantType()));
        requestedRole.setReadOnly(roleAssignmentEntity.isReadOnly());
        requestedRole.setRoleName(roleAssignmentEntity.getRoleName());
        requestedRole.setRoleType(RoleType.valueOf(roleAssignmentEntity.getRoleType()));
        return requestedRole;
    }


}
