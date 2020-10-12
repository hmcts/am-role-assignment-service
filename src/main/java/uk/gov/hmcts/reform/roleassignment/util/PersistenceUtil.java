package uk.gov.hmcts.reform.roleassignment.util;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignment.data.ActorCacheEntity;
import uk.gov.hmcts.reform.roleassignment.data.HistoryEntity;
import uk.gov.hmcts.reform.roleassignment.data.RequestEntity;
import uk.gov.hmcts.reform.roleassignment.data.RoleAssignmentEntity;
import uk.gov.hmcts.reform.roleassignment.domain.model.ActorCache;
import uk.gov.hmcts.reform.roleassignment.domain.model.ExistingRoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.Request;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.ActorIdType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Classification;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

@Service
public class PersistenceUtil {

    public HistoryEntity convertRoleAssignmentToHistoryEntity(RoleAssignment roleAssignment,
                                                              RequestEntity requestEntity) {
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
            .authorisations(String.join(",",
             (roleAssignment.getAuthorisations() != null && !roleAssignment.getAuthorisations().isEmpty()
                 ? roleAssignment.getAuthorisations() : Collections.emptyList())))
            .build();
    }

    public RequestEntity convertRequestToEntity(Request request) {
        return RequestEntity.builder()
            .correlationId(request.getCorrelationId())
            .status(request.getStatus().toString())
            .process(request.getProcess())
            .reference(request.getReference())
            .authenticatedUserId(request.getAuthenticatedUserId())
            .clientId(request.getClientId())
            .assignerId(request.getAssignerId())
            .replaceExisting(request.isReplaceExisting())
            .requestType(request.getRequestType().toString())
            .created(request.getCreated())
            .log(request.getLog())
            .roleAssignmentId(request.getRoleAssignmentId())
            .build();

    }

    public RoleAssignmentEntity convertRoleAssignmentToEntity(RoleAssignment roleAssignment) {
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
            .authorisations(roleAssignment.getAuthorisations() != null && !roleAssignment.getAuthorisations().isEmpty()
                                ? roleAssignment.getAuthorisations() : Collections.emptyList())
            .build();
    }

    public ActorCacheEntity convertActorCacheToEntity(ActorCache actorCache) {
        return ActorCacheEntity.builder()
            .actorId(actorCache.getActorId())
            .etag(actorCache.getEtag())
            .roleAssignmentResponse(JacksonUtils.convertValueJsonNode(actorCache.getRoleAssignments()))
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
            .authorisations(StringUtils.isNotEmpty(historyEntity.getAuthorisations()) ? (Arrays.stream(historyEntity
                                                                         .getAuthorisations().split(
                ",")).collect(Collectors.toList())) : Collections.emptyList())
            .build();
    }

    public RoleAssignment convertEntityToRoleAssignment(RoleAssignmentEntity roleAssignmentEntity) {

        return RoleAssignment.builder()
            .id(roleAssignmentEntity.getId())
            .actorIdType(ActorIdType.valueOf(roleAssignmentEntity.getActorIdType()))
            .actorId(roleAssignmentEntity.getActorId())
            .classification(Classification.valueOf(roleAssignmentEntity.getClassification()))
            .grantType(GrantType.valueOf(roleAssignmentEntity.getGrantType()))
            .readOnly(roleAssignmentEntity.isReadOnly())
            .roleName(roleAssignmentEntity.getRoleName())
            .roleType(RoleType.valueOf(roleAssignmentEntity.getRoleType()))
            .roleCategory(roleAssignmentEntity.getRoleCategory() != null ? RoleCategory.valueOf(roleAssignmentEntity
                                                                                  .getRoleCategory()) : null)
            .beginTime(roleAssignmentEntity.getBeginTime())
            .endTime(roleAssignmentEntity.getEndTime())
            .created(roleAssignmentEntity.getCreated())
            .attributes(JacksonUtils.convertValue(roleAssignmentEntity.getAttributes()))
            .authorisations(roleAssignmentEntity.getAuthorisations() != null && !roleAssignmentEntity
                .getAuthorisations().isEmpty() ? roleAssignmentEntity.getAuthorisations() : Collections.emptyList())
            .build();
    }

    public ExistingRoleAssignment convertEntityToExistingRoleAssignment(RoleAssignmentEntity roleAssignmentEntity) {

        return ExistingRoleAssignment.builder()
            .id(roleAssignmentEntity.getId())
            .actorIdType(ActorIdType.valueOf(roleAssignmentEntity.getActorIdType()))
            .actorId(roleAssignmentEntity.getActorId())
            .classification(Classification.valueOf(roleAssignmentEntity.getClassification()))
            .grantType(GrantType.valueOf(roleAssignmentEntity.getGrantType()))
            .readOnly(roleAssignmentEntity.isReadOnly())
            .roleName(roleAssignmentEntity.getRoleName())
            .roleType(RoleType.valueOf(roleAssignmentEntity.getRoleType()))
            .roleCategory(roleAssignmentEntity.getRoleCategory() != null ? RoleCategory.valueOf(roleAssignmentEntity
                                                                                     .getRoleCategory()) : null)
            .beginTime(roleAssignmentEntity.getBeginTime())
            .endTime(roleAssignmentEntity.getEndTime())
            .created(roleAssignmentEntity.getCreated())
            .attributes(JacksonUtils.convertValue(roleAssignmentEntity.getAttributes()))
            .authorisations(roleAssignmentEntity.getAuthorisations() != null && !roleAssignmentEntity
                .getAuthorisations().isEmpty() ? roleAssignmentEntity.getAuthorisations() : Collections.emptyList())
            .build();
    }


}
