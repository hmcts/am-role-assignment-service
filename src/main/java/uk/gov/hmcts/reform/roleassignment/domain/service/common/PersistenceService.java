package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.roleassignment.data.cachecontrol.ActorCacheEntity;
import uk.gov.hmcts.reform.roleassignment.data.cachecontrol.ActorCacheRepository;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.HistoryEntity;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.HistoryRepository;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.RequestEntity;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.RequestRepository;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.RoleAssignmentEntity;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.RoleAssignmentRepository;
import uk.gov.hmcts.reform.roleassignment.domain.model.ExistingRole;
import uk.gov.hmcts.reform.roleassignment.domain.model.Request;
import uk.gov.hmcts.reform.roleassignment.domain.model.RequestedRole;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.ActorCache;
import uk.gov.hmcts.reform.roleassignment.util.PersistenceUtil;

import java.util.List;
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
    private ActorCacheRepository actorCacheRepository;

    public PersistenceService(HistoryRepository historyRepository, RequestRepository requestRepository,
                              RoleAssignmentRepository roleAssignmentRepository, PersistenceUtil persistenceUtil,
                              ActorCacheRepository actorCacheRepository) {
        this.historyRepository = historyRepository;
        this.requestRepository = requestRepository;
        this.roleAssignmentRepository = roleAssignmentRepository;
        this.persistenceUtil = persistenceUtil;
        this.actorCacheRepository = actorCacheRepository;
    }


    public RequestEntity persistRequest(Request request) {

        //Prepare request entity
        RequestEntity requestEntity = persistenceUtil.convertRequestIntoEntity(request);


        //Persist the request entity
        return requestRepository.save(requestEntity);


    }

    public void persistRequestToHistory(RequestEntity requestEntity) {
        //Persist the request entity
        requestRepository.save(requestEntity);


    }

    public synchronized UUID generateUniqueId() {
        return UUID.randomUUID();

    }


    public HistoryEntity persistHistory(RoleAssignment roleAssignment, Request request) {
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
        } else {
            entity.setId(generateUniqueId());
        }
        //Persist the history entity
        return historyRepository.save(entity);


    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void persistRoleAssignment(RoleAssignment roleAssignment) {
        //Persist the role assignment entity
        RoleAssignmentEntity entity = persistenceUtil.convertRoleAssignmentToEntity(roleAssignment);
        roleAssignmentRepository.save(entity);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ActorCacheEntity persistActorCache(RoleAssignment roleAssignment) {

        ActorCacheEntity entity = persistenceUtil.convertActorCacheToEntity(prepareActorCache(roleAssignment));
        ActorCacheEntity existingActorCache = actorCacheRepository.findByActorId(roleAssignment.actorId);

        if (existingActorCache != null) {
            entity.setEtag(existingActorCache.getEtag());
        }
        return actorCacheRepository.save(entity);
    }

    @NotNull
    private ActorCache prepareActorCache(RoleAssignment roleAssignment) {
        ActorCache actorCache = new ActorCache();
        actorCache.setActorId(roleAssignment.actorId);
        Set<RoleAssignmentEntity> roleAssignmentEntities =
            roleAssignmentRepository.findByActorId(roleAssignment.actorId);
        actorCache.setRoleAssignments(roleAssignmentEntities);
        return actorCache;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<ExistingRole> getExistingRoleAssignment(UUID actorId) {

        Set<RoleAssignmentEntity> roleAssignmentEntities = roleAssignmentRepository.findByActorId(actorId);
        //convert into model class
        return roleAssignmentEntities.stream().map(role -> persistenceUtil.convertRoleAssignmentEntityInModel(role))
                                     .collect(Collectors.toList());

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ActorCacheEntity getActorCacheEntity(UUID actorId) {

        return actorCacheRepository.findByActorId(actorId);
    }

    public List<RequestedRole> getExistingRoleByProcessAndReference(String process, String reference, String status) {
        Set<HistoryEntity> historyEntities = historyRepository.findByReference(process, reference, status);
        //convert into model class
        return historyEntities.stream().map(role -> persistenceUtil.convertHistoryEntityInModel(role)).collect(
            Collectors.toList());

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteRoleAssignment(RoleAssignment roleAssignment) {
        //Persist the role assignment entity
        RoleAssignmentEntity entity = persistenceUtil.convertRoleAssignmentToEntity(roleAssignment);
        roleAssignmentRepository.delete(entity);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteRoleAssignmentByActorId(UUID actorId) {

        roleAssignmentRepository.deleteByActorId(actorId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<RequestedRole> getRoleAssignment(UUID actorId) {

        Set<RoleAssignmentEntity> roleAssignmentEntities = roleAssignmentRepository.findByActorId(actorId);
        //convert into model class
        return roleAssignmentEntities.stream().map(role -> persistenceUtil.roleAssignmentEntityToRequestedRole(role))
            .collect(Collectors.toList());

    }

}
