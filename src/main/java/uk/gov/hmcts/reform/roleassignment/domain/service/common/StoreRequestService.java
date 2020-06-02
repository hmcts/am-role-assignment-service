package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.RoleAssignmentHistoryEntity;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.RoleAssignmentHistoryRepository;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.RoleAssignmentHistoryStatusEntity;
import uk.gov.hmcts.reform.roleassignment.domain.model.RequestedRole;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.util.JacksonUtils;

import java.util.ArrayList;
import java.util.HashSet;

@Service
public class StoreRequestService {

    private RoleAssignmentHistoryRepository roleAssignmentRepository;

    public StoreRequestService(RoleAssignmentHistoryRepository roleAssignmentRepository) {
        this.roleAssignmentRepository = roleAssignmentRepository;
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void persistRequestAndRequestedRoles(RoleAssignmentRequest roleAssignmentRequest) {
        ArrayList<RoleAssignmentHistoryEntity> roleAssignmentHistoryEntityList = new ArrayList<>();
        for (RequestedRole roleAssignment : roleAssignmentRequest.requestedRoles) {
            RoleAssignmentHistoryEntity roleAssignmentHistoryEntity = convertModelToEntity(roleAssignment);
            buildRoleAssignmentHistoryStatus(roleAssignmentHistoryEntity);
            roleAssignmentHistoryEntityList.add(roleAssignmentHistoryEntity);
        }
        roleAssignmentRepository.saveAll(roleAssignmentHistoryEntityList);

    }

    private RoleAssignmentHistoryEntity convertModelToEntity(RequestedRole model) {
        RoleAssignmentHistoryEntity roleAssignmentHistoryEntity = RoleAssignmentHistoryEntity.builder().actorId(model.getActorId())
            .actorIdType(model.getActorIdType().toString())
            .attributes(JacksonUtils.convertValueJsonNode(model.getAttributes()))
            .beginTime(model.getBeginTime())
            .classification(model.getClassification().toString())
            .endTime(model.getEndTime())
            .grantType(model.getGrantType().toString())
            .roleName(model.getRoleName())
            .roleType(model.getRoleType().toString())
            .status(model.getStatus().toString())
            .readOnly(Boolean.TRUE)
            .build();
        roleAssignmentHistoryEntity.setRoleAssignmentHistoryStatusEntities(new HashSet<RoleAssignmentHistoryStatusEntity>());
        return roleAssignmentHistoryEntity;
    }

    private void buildRoleAssignmentHistoryStatus(RoleAssignmentHistoryEntity roleAssignmentHistoryEntity) {
        RoleAssignmentHistoryStatusEntity roleAssignmentHistoryStatusEntity = RoleAssignmentHistoryStatusEntity.builder().roleAssignmentHistoryEntity(
            roleAssignmentHistoryEntity)
            .log("professional drools rule")
            .status(Status.CREATED.toString())
            .sequence(102)
            .build();
        roleAssignmentHistoryEntity.getRoleAssignmentHistoryStatusEntities().add(roleAssignmentHistoryStatusEntity);
    }

}
