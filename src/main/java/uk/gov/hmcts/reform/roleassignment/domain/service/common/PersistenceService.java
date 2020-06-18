package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.HistoryEntity;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.HistoryRepository;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.RequestEntity;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.RequestRepository;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.RoleAssignmentEntity;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.RoleAssignmentRepository;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.ExistingRole;
import uk.gov.hmcts.reform.roleassignment.domain.model.Request;
import uk.gov.hmcts.reform.roleassignment.domain.model.RequestedRole;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.util.PersistenceUtil;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PersistenceService {
    //1. StoreRequest which will insert records in request and history table with log,
    //2. Insert new Assignment record with updated Status in historyTable
    //3. Update Request Status
    //4. Store live record in the roleAssignmentTable

    private HistoryRepository historyRepository;
    private RequestRepository requestRepository;
    private RoleAssignmentRepository roleAssignmentRepository;
    private PersistenceUtil persistenceUtil;

    public PersistenceService(HistoryRepository historyRepository, RequestRepository requestRepository,
                              RoleAssignmentRepository roleAssignmentRepository, PersistenceUtil persistenceUtil) {
        this.historyRepository = historyRepository;
        this.requestRepository = requestRepository;
        this.roleAssignmentRepository = roleAssignmentRepository;
        this.persistenceUtil = persistenceUtil;
    }


    public RequestEntity persistRequest(AssignmentRequest assignmentRequest) {

        //Prepare request entity
        RequestEntity requestEntity = persistenceUtil.convertRequestIntoEntity(assignmentRequest.getRequest());


        //Prepare History entity
        Set<HistoryEntity> historyEntities = assignmentRequest.getRequestedRoles().stream().map(roleAssignment -> persistenceUtil.convertHistoryToEntity(
            roleAssignment,
            requestEntity
        )).collect(Collectors.toSet());

        historyEntities.stream().forEach(entity -> entity.setId(UUID.fromString(generateUniqueId())));

        requestEntity.setHistoryEntities(historyEntities);

        //Persist the request entity
        return requestRepository.save(requestEntity);


    }

    public synchronized String generateUniqueId() {
        return UUID.randomUUID().toString();

    }


    public void persistHistory(RoleAssignment roleAssignment, Request request) {
        UUID roleAssignmentId = roleAssignment.getId();
        UUID requestId = request.getId();

        RequestEntity requestEntity = persistenceUtil.convertRequestIntoEntity(request);
        if (requestId != null) {
            requestEntity.setId(requestId);
        }

        HistoryEntity entity = persistenceUtil.convertHistoryToEntity(roleAssignment, requestEntity
        );

        if (roleAssignmentId != null) {
            entity.setId(roleAssignmentId);
        }
        //Persist the history entity
        historyRepository.save(entity);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void persistRoleAssignment(RoleAssignment roleAssignment, HistoryEntity historyEntity) {
        //Persist the role assignment entity
        RoleAssignmentEntity entity = persistenceUtil.convertRoleAssignmentToEntity(roleAssignment);
        entity.setId(historyEntity.getId());
        roleAssignmentRepository.save(entity);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateRequest(Request request) {

        //need to pass same request entity which we saved in first step with updated status
        //Update the status of request entity which already saved in db
        requestRepository.save(persistenceUtil.convertRequestIntoEntity(request));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Set<ExistingRole> getExistingRoleAssignment(UUID actorId) {

        Set<RoleAssignmentEntity> roleAssignmentEntities = roleAssignmentRepository.findByActorId(actorId);
        //convert into model class
        return roleAssignmentEntities.stream().map(role -> persistenceUtil.convertRoleAssignmentEntityInModel(role)).collect(
            Collectors.toSet());

    }

    public Set<RequestedRole> getExistingRoleByProcessAndReference(String process, String reference, String status) {
        Set<HistoryEntity> historyEntities = historyRepository.findByReference(process, reference, status);
        //convert into model class
        return historyEntities.stream().map(role -> persistenceUtil.convertHistoryEntityInModel(role)).collect(
            Collectors.toSet());
    }


}
