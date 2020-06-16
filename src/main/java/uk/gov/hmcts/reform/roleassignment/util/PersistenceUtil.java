package uk.gov.hmcts.reform.roleassignment.util;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.HistoryEntity;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.RequestEntity;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.RoleAssignmentEntity;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.RoleAssignmentIdentity;
import uk.gov.hmcts.reform.roleassignment.domain.model.ExistingRole;
import uk.gov.hmcts.reform.roleassignment.domain.model.Request;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.ActorIdType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Classification;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;

@Service
public class PersistenceUtil {

    public HistoryEntity convertHistoryToEntity(RoleAssignment model, RequestEntity requestEntity) {
        RoleAssignmentIdentity roleAssignmentId = RoleAssignmentIdentity.builder().status(model.getStatus().toString()).build();
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
            .roleAssignmentIdentity(roleAssignmentId)
            .requestEntity(requestEntity)
            .build();


    }

    public RequestEntity convertRequestIntoEntity(Request request) {
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
            .log(request.getLog())
            .build();

    }

    public RoleAssignmentEntity convertRoleAssignmentToEntity(RoleAssignment model, HistoryEntity historyEntity) {
        return RoleAssignmentEntity.builder().actorId(model.getActorId())
            .actorIdType(model.getActorIdType().toString())
            .attributes(JacksonUtils.convertValueJsonNode(model.getAttributes()))
            .beginTime(model.getBeginTime())
            .classification(model.getClassification().toString())
            .endTime(model.getEndTime())
            .grantType(model.getGrantType().toString())
            .roleName(model.getRoleName())
            .roleType(model.getRoleType().toString())
            .readOnly(model.readOnly)
            .build();


    }

    public ExistingRole convertRoleAssignmentEntityInModel(RoleAssignmentEntity roleAssignmentEntity) {

        ExistingRole existingRole = new ExistingRole();
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
}
