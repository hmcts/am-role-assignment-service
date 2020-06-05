package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.HistoryEntity;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.HistoryRepository;
import uk.gov.hmcts.reform.roleassignment.domain.model.RequestedRole;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.util.JacksonUtils;

import java.util.ArrayList;

@Service
public class PersistenceService {
    //1. StoreRequest which will insert records in request and history table with log,
    //2. Insert new Assignment record with updated Status in historyTable
    //3. Update Request Status
    //4. Store live record in the roleAssignmentTable
    private HistoryRepository roleAssignmentRepository;

    public PersistenceService(HistoryRepository roleAssignmentRepository) {
        this.roleAssignmentRepository = roleAssignmentRepository;
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void persistRequestAndRequestedRoles(RoleAssignmentRequest roleAssignmentRequest) {
        ArrayList<HistoryEntity> historyEntityList = new ArrayList<>();
        for (RequestedRole roleAssignment : roleAssignmentRequest.requestedRoles) {
            HistoryEntity historyEntity = convertModelToEntity(roleAssignment);
            historyEntityList.add(historyEntity);
        }
        roleAssignmentRepository.saveAll(historyEntityList);

    }

    private HistoryEntity convertModelToEntity(RequestedRole model) {
        HistoryEntity historyEntity = HistoryEntity.builder().actorId(model.getActorId())
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
        return historyEntity;
    }

}
