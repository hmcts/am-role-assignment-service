package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.roleassignment.data.ActorCacheEntity;
import uk.gov.hmcts.reform.roleassignment.data.ActorCacheRepository;
import uk.gov.hmcts.reform.roleassignment.data.DatabaseChangelogLockEntity;
import uk.gov.hmcts.reform.roleassignment.data.DatabseChangelogLockRepository;
import uk.gov.hmcts.reform.roleassignment.data.HistoryEntity;
import uk.gov.hmcts.reform.roleassignment.data.HistoryRepository;
import uk.gov.hmcts.reform.roleassignment.data.RequestEntity;
import uk.gov.hmcts.reform.roleassignment.data.RequestRepository;
import uk.gov.hmcts.reform.roleassignment.data.RoleAssignmentEntity;
import uk.gov.hmcts.reform.roleassignment.data.RoleAssignmentRepository;
import uk.gov.hmcts.reform.roleassignment.domain.model.ActorCache;
import uk.gov.hmcts.reform.roleassignment.domain.model.QueryRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.Request;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.util.PersistenceUtil;
import uk.gov.hmcts.reform.roleassignment.v1.V1;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.data.jpa.domain.Specification.where;
import static uk.gov.hmcts.reform.roleassignment.data.RoleAssignmentEntitySpecifications.searchByActorIds;
import static uk.gov.hmcts.reform.roleassignment.data.RoleAssignmentEntitySpecifications.searchByAttributes;
import static uk.gov.hmcts.reform.roleassignment.data.RoleAssignmentEntitySpecifications.searchByClassification;
import static uk.gov.hmcts.reform.roleassignment.data.RoleAssignmentEntitySpecifications.searchByGrantType;
import static uk.gov.hmcts.reform.roleassignment.data.RoleAssignmentEntitySpecifications.searchByRoleCategories;
import static uk.gov.hmcts.reform.roleassignment.data.RoleAssignmentEntitySpecifications.searchByRoleName;
import static uk.gov.hmcts.reform.roleassignment.data.RoleAssignmentEntitySpecifications.searchByRoleType;
import static uk.gov.hmcts.reform.roleassignment.data.RoleAssignmentEntitySpecifications.searchByValidDate;

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
    private DatabseChangelogLockRepository databseChangelogLockRepository;

    public PersistenceService(HistoryRepository historyRepository, RequestRepository requestRepository,
                              RoleAssignmentRepository roleAssignmentRepository, PersistenceUtil persistenceUtil,
                              ActorCacheRepository actorCacheRepository,
                              DatabseChangelogLockRepository databseChangelogLockRepository) {
        this.historyRepository = historyRepository;
        this.requestRepository = requestRepository;
        this.roleAssignmentRepository = roleAssignmentRepository;
        this.persistenceUtil = persistenceUtil;
        this.actorCacheRepository = actorCacheRepository;
        this.databseChangelogLockRepository = databseChangelogLockRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
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
        historyEntity.setId(Objects.requireNonNullElseGet(roleAssignmentId, UUID::randomUUID));
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

    @Transactional
    public DatabaseChangelogLockEntity releaseDatabaseLock(int id) {
        databseChangelogLockRepository.releaseLock(id);
        return databseChangelogLockRepository.getById(id);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<RoleAssignment> getAssignmentsByActor(UUID actorId) {

        Set<RoleAssignmentEntity> roleAssignmentEntities = roleAssignmentRepository.findByActorId(actorId);
        //convert into model class
        return roleAssignmentEntities.stream().map(role -> persistenceUtil.convertEntityToRoleAssignment(role))
            .collect(Collectors.toList());

    }

    public List<RoleAssignment> getAssignmentsByActorAndCaseId(String actorId, String caseId, String roleType) {
        Set<RoleAssignmentEntity> roleAssignmentEntities = null;

        if (StringUtils.isNotEmpty(actorId) && StringUtils.isNotEmpty(caseId)) {
            roleAssignmentEntities = roleAssignmentRepository.findByActorIdAndCaseId(actorId, caseId, roleType);
        } else if (StringUtils.isNotEmpty(actorId)) {
            roleAssignmentEntities =
                roleAssignmentRepository.findByActorIdAndRoleTypeIgnoreCase(UUID.fromString(actorId), roleType);
        } else if (StringUtils.isNotEmpty(caseId)) {
            roleAssignmentEntities = roleAssignmentRepository.getAssignmentByCaseId(caseId, roleType);
        }

        if (roleAssignmentEntities == null || roleAssignmentEntities.isEmpty()) {
            throw new ResourceNotFoundException(V1.Error.ASSIGNMENT_RECORDS_NOT_FOUND);
        }

        return roleAssignmentEntities.stream()
                                     .map(role -> persistenceUtil.convertEntityToRoleAssignment(role))
                                     .collect(Collectors.toList());
    }

    public List<RoleAssignment> retrieveRoleAssignmentsByQueryRequest(QueryRequest searchRequest)  {

        Page<RoleAssignmentEntity> roleAssignmentEntities = roleAssignmentRepository.findAll(where((searchRequest.getActorId()==null || searchRequest.getActorId().isEmpty()) ? null : searchByActorIds(
            searchRequest.getActorId())) .and((searchRequest.getGrantType()==null ||searchRequest.getGrantType().isEmpty()) ? null : searchByGrantType(searchRequest.getGrantType()))
                                         .and(searchRequest.getVaildAt() == null ? null : searchByValidDate(searchRequest.getVaildAt()))
                                         .and((searchRequest.getAttributes()==null||searchRequest.getAttributes().isEmpty()) ? null : searchByAttributes(searchRequest.getAttributes()))
                                         .and((searchRequest.getRoleType()==null||searchRequest.getRoleType().isEmpty())?null:searchByRoleType(searchRequest.getRoleType()))
                                         .and((searchRequest.getRoleName()==null||searchRequest.getRoleName().isEmpty())?null:searchByRoleName(searchRequest.getRoleName()))
                                         .and((searchRequest.getClassification()==null||searchRequest.getClassification().isEmpty())?null:searchByClassification(searchRequest.getClassification()))
                                         .and((searchRequest.getRoleCategorie()==null||searchRequest.getRoleCategorie().isEmpty())?null:searchByRoleCategories(searchRequest.getRoleCategorie()))
                                         ,PageRequest.of(0, 10, Sort.by(Sort.DEFAULT_DIRECTION, "id")));



        return roleAssignmentEntities.stream()
            .map(role -> persistenceUtil.convertEntityToRoleAssignment(role))
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
