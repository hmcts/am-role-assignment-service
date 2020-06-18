package uk.gov.hmcts.reform.roleassignment.util;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.HistoryEntity;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.RequestEntity;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.RoleAssignmentEntity;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.RoleAssignmentIdentity;
import uk.gov.hmcts.reform.roleassignment.domain.model.ExistingRole;
import uk.gov.hmcts.reform.roleassignment.domain.model.Request;
import uk.gov.hmcts.reform.roleassignment.domain.model.RequestedRole;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.ActorIdType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Classification;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;

@Service
public class PersistenceUtil {

    public HistoryEntity convertHistoryToEntity(RoleAssignment model, RequestEntity requestEntity) {
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
            .assignerId(request.getRequestorId())
            .replaceExisting(request.replaceExisting)
            .requestType(request.getRequestType().toString())
            .log(request.getLog())
            .build();

    }

    public RoleAssignmentEntity convertRoleAssignmentToEntity(RoleAssignment model) {
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

    public RequestedRole convertHistoryEntityInModel(HistoryEntity historyEntity) {

        RequestedRole requestedrole = new RequestedRole();
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

}
