package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.HistoryEntity;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.HistoryRepository;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.HistoryStatusEntity;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.HistoryStatusRepository;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.RequestRepository;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.RequestStatusRepository;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.RoleAssignmentRepository;
import uk.gov.hmcts.reform.roleassignment.domain.model.RequestedRole;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.util.JacksonUtils;

import java.util.ArrayList;
import java.util.HashSet;

@Service
public class PersistenceService {
    //1. StoreRequest,
    //2. StoreAssignment
    //3. Update Request Status
    //4. Update Assignment Status
    //5. Make Assignment to Live
    private HistoryRepository historyRepository;
    private HistoryStatusRepository historyStatusRepository;
    private RequestRepository requestRepository;
    private RequestStatusRepository requestStatusRepository;
    private RoleAssignmentRepository roleAssignmentRepository;

    public PersistenceService(HistoryRepository historyRepository, HistoryStatusRepository historyStatusRepository,
                              RequestRepository requestRepository, RequestStatusRepository requestStatusRepository,
                              RoleAssignmentRepository roleAssignmentRepository) {
        this.historyRepository = historyRepository;
        this.historyStatusRepository = historyStatusRepository;
        this.requestRepository = requestRepository;
        this.roleAssignmentRepository = roleAssignmentRepository;
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void persistRequestAndRequestedRoles(RoleAssignmentRequest roleAssignmentRequest) {
        ArrayList<HistoryEntity> historyEntityList = new ArrayList<>();
        for (RequestedRole roleAssignment : roleAssignmentRequest.requestedRoles) {
            HistoryEntity historyEntity = convertModelToEntity(roleAssignment);
            buildRoleAssignmentHistoryStatus(historyEntity);
            historyEntityList.add(historyEntity);
        }
        historyRepository.saveAll(historyEntityList);

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
        historyEntity.setRoleAssignmentHistoryStatusEntities(new HashSet<HistoryStatusEntity>());
        return historyEntity;
    }

    private void buildRoleAssignmentHistoryStatus(HistoryEntity historyEntity) {
        HistoryStatusEntity historyStatusEntity = HistoryStatusEntity.builder().historyEntity(
            historyEntity)
            .log("professional drools rule")
            .status(Status.CREATED.toString())
            .sequence(102)
            .build();
        historyEntity.getRoleAssignmentHistoryStatusEntities().add(historyStatusEntity);
    }

}
