package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.roleassignment.domain.model.ActorCache;
import uk.gov.hmcts.reform.roleassignment.domain.model.Request;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.util.PersistenceUtil;
import uk.gov.hmcts.reform.roleassignment.data.ActorCacheEntity;
import uk.gov.hmcts.reform.roleassignment.data.ActorCacheRepository;
import uk.gov.hmcts.reform.roleassignment.data.HistoryEntity;
import uk.gov.hmcts.reform.roleassignment.data.HistoryRepository;
import uk.gov.hmcts.reform.roleassignment.data.RequestEntity;
import uk.gov.hmcts.reform.roleassignment.data.RequestRepository;
import uk.gov.hmcts.reform.roleassignment.data.RoleAssignmentEntity;
import uk.gov.hmcts.reform.roleassignment.data.RoleAssignmentRepository;

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
        RequestEntity requestEntity = persistenceUtil.convertRequestToEntity(request);


        //Persist the request entity
        return requestRepository.save(requestEntity);


    }

    public void updateRequest(RequestEntity requestEntity) {
        //Persist the request entity
        requestRepository.save(requestEntity);


    }


    public HistoryEntity persistHistory(RoleAssignment roleAssignment, Request request) {
        UUID roleAssignmentId = roleAssignment.getId();
        UUID requestId = request.getId();

        RequestEntity requestEntity = persistenceUtil.convertRequestToEntity(request);
        if (requestId != null) {
            requestEntity.setId(requestId);
        }

        HistoryEntity historyEntity = persistenceUtil.convertRoleAssignmentToHistoryEntity(roleAssignment,
                                                                                           requestEntity);
        if (roleAssignmentId != null) {
            historyEntity.setId(roleAssignmentId);
        } else {
            historyEntity.setId(UUID.randomUUID());
        }
        //Persist the history entity
        return historyRepository.save(historyEntity);


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
        ActorCacheEntity existingActorCache = actorCacheRepository.findByActorId(roleAssignment.getActorId());

        if (existingActorCache != null) {
            entity.setEtag(existingActorCache.getEtag());
        }
        return actorCacheRepository.save(entity);
    }

    @NotNull
    private ActorCache prepareActorCache(RoleAssignment roleAssignment) {
        ActorCache actorCache = new ActorCache();
        actorCache.setActorId(roleAssignment.getActorId());
        Set<RoleAssignmentEntity> roleAssignmentEntities =
            roleAssignmentRepository.findByActorId(roleAssignment.getActorId());
        actorCache.setRoleAssignments(roleAssignmentEntities);
        return actorCache;
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ActorCacheEntity getActorCacheEntity(UUID actorId) {

        return actorCacheRepository.findByActorId(actorId);
    }

    public List<RoleAssignment> getAssignmentsByProcess(String process, String reference, String status) {
        Set<HistoryEntity> historyEntities = historyRepository.findByReference(process, reference, status);
        //convert into model class
        return historyEntities.stream().map(historyEntity -> persistenceUtil
            .convertHistoryEntityToRoleAssignment(historyEntity)).collect(
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
    public List<RoleAssignment> getAssignmentsByActor(UUID actorId) {

        Set<RoleAssignmentEntity> roleAssignmentEntities = roleAssignmentRepository.findByActorId(actorId);
        //convert into model class
        return roleAssignmentEntities.stream().map(role -> persistenceUtil.convertEntityToRoleAssignment(role))
            .collect(Collectors.toList());

    }

    public List<RoleAssignment> getAssignmentById(UUID assignmentId) {
        Optional<RoleAssignmentEntity> roleAssignmentEntityOptional = roleAssignmentRepository.findById(assignmentId);
        if (roleAssignmentEntityOptional.isPresent()) {
            return roleAssignmentEntityOptional.stream()
                                               .map(role -> persistenceUtil.convertEntityToRoleAssignment(role))
                                               .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

}
