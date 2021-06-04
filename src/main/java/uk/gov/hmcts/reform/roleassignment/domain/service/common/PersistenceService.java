package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import com.launchdarkly.shaded.org.jetbrains.annotations.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.annotation.RequestScope;
import uk.gov.hmcts.reform.roleassignment.data.ActorCacheEntity;
import uk.gov.hmcts.reform.roleassignment.data.ActorCacheRepository;
import uk.gov.hmcts.reform.roleassignment.data.DatabaseChangelogLockEntity;
import uk.gov.hmcts.reform.roleassignment.data.DatabseChangelogLockRepository;
import uk.gov.hmcts.reform.roleassignment.data.FlagConfig;
import uk.gov.hmcts.reform.roleassignment.data.FlagConfigRepository;
import uk.gov.hmcts.reform.roleassignment.data.HistoryEntity;
import uk.gov.hmcts.reform.roleassignment.data.HistoryRepository;
import uk.gov.hmcts.reform.roleassignment.data.RequestEntity;
import uk.gov.hmcts.reform.roleassignment.data.RequestRepository;
import uk.gov.hmcts.reform.roleassignment.data.RoleAssignmentEntity;
import uk.gov.hmcts.reform.roleassignment.data.RoleAssignmentRepository;
import uk.gov.hmcts.reform.roleassignment.domain.model.ActorCache;
import uk.gov.hmcts.reform.roleassignment.domain.model.Assignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.QueryRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.Request;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.util.PersistenceUtil;

import javax.persistence.EntityManager;
import java.util.Collection;
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
import static uk.gov.hmcts.reform.roleassignment.data.RoleAssignmentEntitySpecifications.searchByAuthorisations;
import static uk.gov.hmcts.reform.roleassignment.data.RoleAssignmentEntitySpecifications.searchByClassification;
import static uk.gov.hmcts.reform.roleassignment.data.RoleAssignmentEntitySpecifications.searchByGrantType;
import static uk.gov.hmcts.reform.roleassignment.data.RoleAssignmentEntitySpecifications.searchByRoleCategories;
import static uk.gov.hmcts.reform.roleassignment.data.RoleAssignmentEntitySpecifications.searchByRoleName;
import static uk.gov.hmcts.reform.roleassignment.data.RoleAssignmentEntitySpecifications.searchByRoleType;
import static uk.gov.hmcts.reform.roleassignment.data.RoleAssignmentEntitySpecifications.searchByValidDate;

@Service
@RequestScope
public class PersistenceService {

    private static final Logger logger = LoggerFactory.getLogger(PersistenceService.class);

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
    private Page<RoleAssignmentEntity> pageRoleAssignmentEntities;
    private FlagConfigRepository flagConfigRepository;

    @Value("${roleassignment.query.sortcolumn}")
    private String sortColumn;

    @Value("${roleassignment.query.size}")
    private Integer defaultSize;

    @Autowired
    EntityManager entityManager;

    public PersistenceService(HistoryRepository historyRepository, RequestRepository requestRepository,
                              RoleAssignmentRepository roleAssignmentRepository, PersistenceUtil persistenceUtil,
                              ActorCacheRepository actorCacheRepository,
                              DatabseChangelogLockRepository databseChangelogLockRepository,
                              FlagConfigRepository flagConfigRepository) {
        this.historyRepository = historyRepository;
        this.requestRepository = requestRepository;
        this.roleAssignmentRepository = roleAssignmentRepository;
        this.persistenceUtil = persistenceUtil;
        this.actorCacheRepository = actorCacheRepository;
        this.databseChangelogLockRepository = databseChangelogLockRepository;
        this.flagConfigRepository = flagConfigRepository;
    }

    @Transactional
    public RequestEntity persistRequest(Request request) {

        //Prepare request entity
        var requestEntity = persistenceUtil.convertRequestToEntity(request);


        //Persist the request entity
        return requestRepository.save(requestEntity);


    }

    @Transactional
    public void updateRequest(RequestEntity requestEntity) {
        //Persist the request entity
        requestRepository.save(requestEntity);
    }

    @Transactional
    public void persistHistoryEntities(Collection<HistoryEntity> historyEntityList) {
        historyEntityList.forEach(historyEntity -> entityManager.persist(historyEntity));
        entityManager.flush();
    }

    @Transactional
    public void persistRoleAssignments(Collection<RoleAssignment> roleAssignments) {
        //Persist the role assignment entity
        Set<RoleAssignmentEntity> roleAssignmentEntities = roleAssignments.stream().map(
            roleAssignment -> persistenceUtil.convertRoleAssignmentToEntity(roleAssignment, true)
        ).collect(Collectors.toSet());
        roleAssignmentEntities.forEach(roleAssignmentEntity -> entityManager.persist(roleAssignmentEntity));
        entityManager.flush();
    }

    @Transactional
    public void persistActorCache(Collection<RoleAssignment> roleAssignments) {
        roleAssignments.forEach(roleAssignment -> {
            var actorCacheEntity = persistenceUtil
                .convertActorCacheToEntity(prepareActorCache(roleAssignment));
            ActorCacheEntity existingActorCache = actorCacheRepository.findByActorId(roleAssignment.getActorId());
            if (existingActorCache != null) {
                actorCacheEntity.setEtag(existingActorCache.getEtag());
                entityManager.merge(actorCacheEntity);
            } else {
                entityManager.persist(actorCacheEntity);
            }
        });
        entityManager.flush();

    }

    @NotNull
    private ActorCache prepareActorCache(RoleAssignment roleAssignment) {
        var actorCache = new ActorCache();
        actorCache.setActorId(roleAssignment.getActorId());
        return actorCache;
    }

    @Transactional
    public ActorCacheEntity getActorCacheEntity(String actorId) {

        return actorCacheRepository.findByActorId(actorId);
    }

    public List<RoleAssignment> getAssignmentsByProcess(String process, String reference, String status) {
        long startTime = System.currentTimeMillis();

        Set<HistoryEntity> historyEntities = historyRepository.findByReference(process, reference, status);
        //convert into model class
        List<RoleAssignment> roleAssignmentList = historyEntities.stream().map(historyEntity -> persistenceUtil
            .convertHistoryEntityToRoleAssignment(historyEntity)).collect(
            Collectors.toList());
        logger.debug(
            " >> getAssignmentsByProcess execution finished at {} . Time taken = {} milliseconds",
            System.currentTimeMillis(),
            Math.subtractExact(System.currentTimeMillis(), startTime)
        );
        return roleAssignmentList;

    }

    @Transactional
    public void deleteRoleAssignment(RoleAssignment roleAssignment) {
        //Persist the role assignment entity
        RoleAssignmentEntity entity = persistenceUtil.convertRoleAssignmentToEntity(roleAssignment, false);
        roleAssignmentRepository.delete(entity);
    }

    @Transactional
    public void deleteRoleAssignmentByActorId(String actorId) {
        roleAssignmentRepository.deleteByActorId(actorId);
    }

    @Transactional
    public DatabaseChangelogLockEntity releaseDatabaseLock(int id) {
        databseChangelogLockRepository.releaseLock(id);
        return databseChangelogLockRepository.getById(id);
    }

    @Transactional
    public List<RoleAssignment> getAssignmentsByActor(String actorId) {

        Set<RoleAssignmentEntity> roleAssignmentEntities = roleAssignmentRepository.findByActorId(actorId);
        //convert into model class
        return roleAssignmentEntities.stream().map(role -> persistenceUtil.convertEntityToRoleAssignment(role))
            .collect(Collectors.toList());

    }


    public List<Assignment> retrieveRoleAssignmentsByQueryRequest(QueryRequest searchRequest,
                                                                  Integer pageNumber,
                                                                  Integer size, String sort,
                                                                  String direction,
                                                                  boolean existingFlag) {

        long startTime = System.currentTimeMillis();
        List<Assignment> roleAssignmentList;

        pageRoleAssignmentEntities = roleAssignmentRepository.findAll(
            Objects.requireNonNull(Objects.requireNonNull(
                Objects.requireNonNull(
                    Objects.requireNonNull(
                        Objects.requireNonNull(
                            Objects.requireNonNull(
                                Objects.requireNonNull(
                                    Objects.requireNonNull(
                                        where(
                                            searchByActorIds(searchRequest.getActorId())))
                                        .and(searchByGrantType(searchRequest.getGrantType())))
                                    .and(searchByValidDate(searchRequest.getValidAt())))
                                .and(searchByAttributes(searchRequest.getAttributes())))
                            .and(searchByRoleType(searchRequest.getRoleType())))
                        .and(searchByRoleName(searchRequest.getRoleName())))
                    .and(searchByClassification(searchRequest.getClassification())))
                                       .and(searchByRoleCategories(searchRequest.getRoleCategory())))
                .and(searchByAuthorisations(searchRequest.getAuthorisations())),
            PageRequest.of(
                (pageNumber != null
                    && pageNumber > 0) ? pageNumber : 0,
                (size != null
                    && size > 0) ? size : defaultSize,
                Sort.by(
                    (direction != null) ? Sort.Direction.fromString(direction) : Sort.DEFAULT_DIRECTION,
                    (sort != null) ? sort : sortColumn
                )
            )
        );

        if (!existingFlag) {
            roleAssignmentList = pageRoleAssignmentEntities.stream()
                .map(role -> persistenceUtil.convertEntityToRoleAssignment(role))
                .collect(Collectors.toList());

        } else {
            roleAssignmentList = pageRoleAssignmentEntities.stream()
                .map(role -> persistenceUtil.convertEntityToExistingRoleAssignment(role))
                .collect(Collectors.toList());

        }

        logger.debug(
            " >> retrieveRoleAssignmentsByQueryRequest execution finished at {} . Time taken = {} milliseconds",
            System.currentTimeMillis(),
            Math.subtractExact(System.currentTimeMillis(), startTime)
        );
        return roleAssignmentList;
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

    public long getTotalRecords() {
        return pageRoleAssignmentEntities != null ? pageRoleAssignmentEntities.getTotalElements() : Long.valueOf(0);

    }

    public boolean getStatusByParam(String flagName, String envName) {
        if (StringUtils.isEmpty(envName)) {
            envName = System.getenv("LAUNCH_DARKLY_ENV");
        }
        return flagConfigRepository.findByFlagNameAndEnv(flagName, envName).getStatus();
    }

    public FlagConfig persistFlagConfig(FlagConfig flagConfig) {
        return flagConfigRepository.save(flagConfig);

    }

}
