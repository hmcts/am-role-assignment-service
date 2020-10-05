package uk.gov.hmcts.reform.roleassignment.domain.service.common;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.QueryRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.Request;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;
import uk.gov.hmcts.reform.roleassignment.util.PersistenceUtil;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class PersistenceServiceTest {

    @Mock
    private HistoryRepository historyRepository;
    @Mock
    private RequestRepository requestRepository;
    @Mock
    private RoleAssignmentRepository roleAssignmentRepository;
    @Mock
    private PersistenceUtil persistenceUtil;
    @Mock
    private ActorCacheRepository actorCacheRepository;
    @Mock
    private DatabseChangelogLockRepository databseChangelogLockRepository;

    @InjectMocks
    private PersistenceService sut = new PersistenceService(
        historyRepository, requestRepository, roleAssignmentRepository, persistenceUtil, actorCacheRepository,
        databseChangelogLockRepository
    );


    @Mock
    Specification<RoleAssignmentEntity> mockSpec;

    @Mock
    Root<RoleAssignmentEntity> root;
    @Mock
    CriteriaQuery<RoleAssignmentEntity> query;
    @Mock
    CriteriaBuilder builder;
    @Mock
    Predicate predicate;


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void persistRequest() {
        Request request = TestDataBuilder.buildRequest(Status.CREATED, false);
        RequestEntity requestEntity = TestDataBuilder.buildRequestEntity(request);
        when(persistenceUtil.convertRequestToEntity(request)).thenReturn(requestEntity);
        when(requestRepository.save(requestEntity)).thenReturn(requestEntity);

        RequestEntity result = sut.persistRequest(request);
        assertNotNull(result);
        verify(persistenceUtil, times(1)).convertRequestToEntity(any(Request.class));
        verify(requestRepository, times(1)).save(requestEntity);
    }

    @Test
    void persistRequestToHistory() {
        Request request = TestDataBuilder.buildRequest(Status.CREATED, false);
        RequestEntity requestEntity = TestDataBuilder.buildRequestEntity(request);
        try {
            sut.updateRequest(requestEntity);
            assertNotNull(requestEntity);
            verify(requestRepository, times(1)).save(requestEntity);

        } catch (Exception e) {
            throw new InternalError(e);
        }
    }


    @Test
    void persistHistory() throws IOException {
        AssignmentRequest assignmentRequest = TestDataBuilder
            .buildAssignmentRequest(Status.CREATED, Status.LIVE, false);
        RequestEntity requestEntity = TestDataBuilder.buildRequestEntity(assignmentRequest.getRequest());
        HistoryEntity historyEntity = TestDataBuilder.buildHistoryIntoEntity(
            assignmentRequest.getRequestedRoles().iterator().next(), requestEntity);

        when(persistenceUtil.convertRequestToEntity(assignmentRequest.getRequest())).thenReturn(requestEntity);
        when(persistenceUtil.convertRoleAssignmentToHistoryEntity(
            assignmentRequest.getRequestedRoles().iterator().next(), requestEntity)).thenReturn(historyEntity);
        when(historyRepository.save(historyEntity)).thenReturn(historyEntity);

        HistoryEntity historyEntityResult = sut.persistHistory(
            assignmentRequest.getRequestedRoles().iterator().next(), assignmentRequest.getRequest());

        assertNotNull(historyEntityResult);
        assertNotNull(assignmentRequest.getRequest().getId());
        assertNotNull(historyEntityResult.getRequestEntity().getId());

        assertEquals(assignmentRequest.getRequest().getId(), historyEntityResult.getRequestEntity().getId());
        assertEquals(assignmentRequest.getRequestedRoles().iterator().next().getId(), historyEntityResult.getId());
        for (RoleAssignment requestedRole : assignmentRequest.getRequestedRoles()) {
            assertEquals(requestedRole.getId(), historyEntityResult.getId());
        }

        verify(persistenceUtil, times(1)).convertRequestToEntity(any(Request.class));
        verify(persistenceUtil, times(1)).convertRoleAssignmentToHistoryEntity(
            any(RoleAssignment.class), any(RequestEntity.class));
        verify(historyRepository, times(1)).save(any(HistoryEntity.class));
    }

    @Test
    void persistHistory_NullRequestId() throws IOException {
        AssignmentRequest assignmentRequest = TestDataBuilder
            .buildAssignmentRequest(Status.CREATED, Status.LIVE, false);
        assignmentRequest.getRequest().setId(null);
        assignmentRequest.getRequestedRoles().iterator().next().setId(null);
        RequestEntity requestEntity = TestDataBuilder.buildRequestEntity(assignmentRequest.getRequest());
        HistoryEntity historyEntity = TestDataBuilder.buildHistoryIntoEntity(
            assignmentRequest.getRequestedRoles().iterator().next(), requestEntity);

        when(persistenceUtil.convertRequestToEntity(assignmentRequest.getRequest())).thenReturn(requestEntity);
        when(persistenceUtil.convertRoleAssignmentToHistoryEntity(any(), any())).thenReturn(historyEntity);
        when(historyRepository.save(historyEntity)).thenReturn(historyEntity);

        HistoryEntity historyEntityResult = sut.persistHistory(
            assignmentRequest.getRequestedRoles().iterator().next(), assignmentRequest.getRequest());

        assertNotNull(historyEntityResult);
        assertNotNull(historyEntityResult.getId());
        assertNull(historyEntityResult.getRequestEntity().getId());

        assertEquals(
            assignmentRequest.getRequestedRoles().iterator().next().getId(),
            historyEntityResult.getRequestEntity().getId()
        );

        verify(persistenceUtil, times(1)).convertRequestToEntity(any(Request.class));
        verify(persistenceUtil, times(1)).convertRoleAssignmentToHistoryEntity(
            any(RoleAssignment.class), any(RequestEntity.class));
        verify(historyRepository, times(1)).save(any(HistoryEntity.class));
    }

    @Test
    void persistRoleAssignment() throws IOException {
        AssignmentRequest assignmentRequest = TestDataBuilder
            .buildAssignmentRequest(Status.CREATED, Status.LIVE, false);
        RoleAssignmentEntity roleAssignmentEntity = TestDataBuilder.convertRoleAssignmentToEntity(
            assignmentRequest.getRequestedRoles().iterator().next());
        when(persistenceUtil.convertRoleAssignmentToEntity(
            assignmentRequest.getRequestedRoles().iterator().next())).thenReturn(roleAssignmentEntity);

        sut.persistRoleAssignment(assignmentRequest.getRequestedRoles().iterator().next());

        verify(persistenceUtil, times(1))
            .convertRoleAssignmentToEntity(any(RoleAssignment.class));
    }

    @Test
    void persistActorCache() throws IOException {
        RoleAssignment roleAssignment = TestDataBuilder.buildRoleAssignment(Status.LIVE);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.createObjectNode();
        ActorCacheEntity entity = new ActorCacheEntity(roleAssignment.getActorId(), 1234, rootNode);
        TestDataBuilder.prepareActorCache(roleAssignment);
        when(persistenceUtil.convertActorCacheToEntity(any())).thenReturn(entity);
        when(actorCacheRepository.findByActorId(roleAssignment.getActorId())).thenReturn(entity);
        when(actorCacheRepository.save(entity)).thenReturn(entity);

        ActorCacheEntity result = sut.persistActorCache(roleAssignment);

        assertNotNull(result);
        assertNotNull(result.getActorId());
        assertEquals(entity.getEtag(), result.getEtag());

        verify(persistenceUtil, times(1)).convertActorCacheToEntity(any());
        verify(actorCacheRepository, times(1)).findByActorId(roleAssignment.getActorId());
    }

    @Test
    void persistActorCache_nullEntity() throws IOException {
        RoleAssignment roleAssignment = TestDataBuilder.buildRoleAssignment(Status.LIVE);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.createObjectNode();
        ActorCacheEntity entity = new ActorCacheEntity(roleAssignment.getActorId(), 1234, rootNode);
        TestDataBuilder.prepareActorCache(roleAssignment);
        when(persistenceUtil.convertActorCacheToEntity(any())).thenReturn(entity);
        when(actorCacheRepository.findByActorId(roleAssignment.getActorId())).thenReturn(null);
        when(actorCacheRepository.save(entity)).thenReturn(entity);

        ActorCacheEntity result = sut.persistActorCache(roleAssignment);

        assertNotNull(result);
        assertNotNull(result.getActorId());
        assertEquals(entity.getEtag(), result.getEtag());

        verify(persistenceUtil, times(1)).convertActorCacheToEntity(any());
        verify(actorCacheRepository, times(1)).findByActorId(roleAssignment.getActorId());
    }

    @Test
    void getActorCacheEntity() throws IOException {
        String id = UUID.randomUUID().toString();
        ActorCacheEntity actorCacheEntity = TestDataBuilder.buildActorCacheEntity();
        when(actorCacheRepository.findByActorId(id)).thenReturn(actorCacheEntity);
        ActorCacheEntity result = sut.getActorCacheEntity(id);
        assertEquals(actorCacheEntity, result);
        verify(actorCacheRepository, times(1)).findByActorId(id);
    }

    @Test
    void getExistingRoleByProcessAndReference() throws IOException {
        AssignmentRequest assignmentRequest = TestDataBuilder
            .buildAssignmentRequest(Status.CREATED, Status.LIVE, false);
        RequestEntity requestEntity = TestDataBuilder.buildRequestEntity(assignmentRequest.getRequest());
        HistoryEntity historyEntity = TestDataBuilder.buildHistoryIntoEntity(
            assignmentRequest.getRequestedRoles().iterator().next(), requestEntity);
        Set<HistoryEntity> historyEntities = new HashSet<>();
        historyEntities.add(historyEntity);

        RoleAssignment requestedRole = TestDataBuilder.convertHistoryEntityInModel(historyEntity);

        when(historyRepository.findByReference(
            "process", "reference", "status")).thenReturn(historyEntities);
        when(persistenceUtil.convertHistoryEntityToRoleAssignment(historyEntity)).thenReturn(requestedRole);

        List<RoleAssignment> result = sut.getAssignmentsByProcess("process", "reference", "status");

        assertNotNull(result);
        assertFalse(result.isEmpty());

        verify(historyRepository, times(1)).findByReference(
            "process", "reference", "status");
        verify(persistenceUtil, times(1)).convertHistoryEntityToRoleAssignment(historyEntity);
    }

    @Test
    void deleteRoleAssignment() throws IOException {
        AssignmentRequest assignmentRequest = TestDataBuilder
            .buildAssignmentRequest(Status.CREATED, Status.LIVE, false);
        RoleAssignmentEntity roleAssignmentEntity = TestDataBuilder.convertRoleAssignmentToEntity(
            assignmentRequest.getRequestedRoles().iterator().next());

        when(persistenceUtil.convertRoleAssignmentToEntity(
            assignmentRequest.getRequestedRoles().iterator().next())).thenReturn(roleAssignmentEntity);

        sut.deleteRoleAssignment(assignmentRequest.getRequestedRoles().iterator().next());

        verify(persistenceUtil, times(1))
            .convertRoleAssignmentToEntity(any(RoleAssignment.class));
        verify(roleAssignmentRepository, times(1)).delete(any(RoleAssignmentEntity.class));
    }

    @Test
    void deleteRoleAssignmentById() throws IOException {
        sut.deleteRoleAssignmentByActorId(UUID.randomUUID().toString());
        verify(roleAssignmentRepository, times(1)).deleteByActorId(any(String.class));
    }

    @Test
    void getAssignmentsByActor() throws IOException {
        String id = UUID.randomUUID().toString();
        Set<RoleAssignmentEntity> roleAssignmentEntitySet = new HashSet<>();
        roleAssignmentEntitySet.add(TestDataBuilder.buildRoleAssignmentEntity(TestDataBuilder
                                                                                  .buildRoleAssignment(Status.LIVE)));
        when(roleAssignmentRepository.findByActorId(id))
            .thenReturn(roleAssignmentEntitySet);
        when(persistenceUtil.convertEntityToRoleAssignment(roleAssignmentEntitySet.iterator().next()))
            .thenReturn(TestDataBuilder.buildRoleAssignment(Status.LIVE));
        List<RoleAssignment> roleAssignmentList = sut.getAssignmentsByActor(id);
        assertNotNull(roleAssignmentList);

        assertNotNull(roleAssignmentList);
        assertFalse(roleAssignmentList.isEmpty());

        verify(persistenceUtil, times(1))
            .convertEntityToRoleAssignment(roleAssignmentEntitySet.iterator().next());
        verify(roleAssignmentRepository, times(1))
            .findByActorId(id);
    }

    @Test
    void getAssignmentsByActor_NPE() throws IOException {
        String id = UUID.randomUUID().toString();
        Set<RoleAssignmentEntity> roleAssignmentEntitySet = new HashSet<>();
        roleAssignmentEntitySet.add(TestDataBuilder.buildRoleAssignmentEntity(TestDataBuilder
                                                                                  .buildRoleAssignment(Status.LIVE)));
        when(roleAssignmentRepository.findByActorId(id))
            .thenReturn(null);

        Assertions.assertThrows(NullPointerException.class, () ->
            sut.getAssignmentsByActor(id)
        );

        verify(roleAssignmentRepository, times(1))
            .findByActorId(id);
    }


    @Test
    void getAssignmentById() throws IOException {
        UUID id = UUID.randomUUID();
        Optional<RoleAssignmentEntity> roleAssignmentOptional =
            Optional.of(TestDataBuilder.buildRoleAssignmentEntity(TestDataBuilder.buildRoleAssignment(Status.LIVE)));
        when(roleAssignmentRepository.findById(id)).thenReturn(roleAssignmentOptional);
        List<RoleAssignment> roleAssignmentList = sut.getAssignmentById(id);
        assertNotNull(roleAssignmentList);
    }

    @Test
    void getAssignmentById_NPE() {
        UUID id = UUID.randomUUID();
        when(roleAssignmentRepository.findById(id)).thenReturn(null);
        Assertions.assertThrows(NullPointerException.class, () ->
            sut.getAssignmentById(id)
        );
    }

    @Test
    void releaseDBChangeLock() {
        DatabaseChangelogLockEntity databaseChangelogLockEntity = DatabaseChangelogLockEntity.builder().id(1).locked(
            false).lockedby(null).build();
        when(databseChangelogLockRepository.getById(1)).thenReturn(databaseChangelogLockEntity);
        DatabaseChangelogLockEntity entity = sut.releaseDatabaseLock(1);
        assertFalse(entity.isLocked());

    }

    @Test
    void postRoleAssignmentsByQueryRequest() throws IOException {


        List<RoleAssignmentEntity> tasks = new ArrayList<>();
        tasks.add(TestDataBuilder.buildRoleAssignmentEntity(TestDataBuilder.buildRoleAssignment(Status.LIVE)));

        Page<RoleAssignmentEntity> page = new PageImpl<RoleAssignmentEntity>(tasks);


        List<String> actorId = Arrays.asList(
            "123e4567-e89b-42d3-a456-556642445678",
            "4dc7dd3c-3fb5-4611-bbde-5101a97681e1"
        );
        List<String> roleType = Arrays.asList("CASE", "ORGANISATION");

        QueryRequest queryRequest = QueryRequest.builder()
            .actorId(actorId)
            .roleType(roleType)
            .build();

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(
            Pageable.class);

        Specification<RoleAssignmentEntity> spec = Specification.where(any());

        when(roleAssignmentRepository.findAll(spec, pageableCaptor.capture()
        ))
            .thenReturn(page);


        when(mockSpec.toPredicate(root, query, builder)).thenReturn(predicate);


        when(persistenceUtil.convertEntityToRoleAssignment(page.iterator().next()))
            .thenReturn(TestDataBuilder.buildRoleAssignment(Status.LIVE));

        List<RoleAssignment> roleAssignmentList = sut.retrieveRoleAssignmentsByQueryRequest(queryRequest, 1, 1, "id",
                                                                                            "desc"
        );
        assertNotNull(roleAssignmentList);

        assertNotNull(roleAssignmentList);
        assertFalse(roleAssignmentList.isEmpty());

        verify(persistenceUtil, times(1))
            .convertEntityToRoleAssignment(page.iterator().next());

    }

    @Test
    void postRoleAssignmentsByQueryRequestWithAllParameters() throws IOException {


        List<RoleAssignmentEntity> tasks = new ArrayList<>();
        tasks.add(TestDataBuilder.buildRoleAssignmentEntity(TestDataBuilder.buildRoleAssignment(Status.LIVE)));

        Page<RoleAssignmentEntity> page = new PageImpl<RoleAssignmentEntity>(tasks);


        List<String> actorId = Arrays.asList(
            "123e4567-e89b-42d3-a456-556642445678",
            "4dc7dd3c-3fb5-4611-bbde-5101a97681e1"
        );
        List<String> roleType = Arrays.asList("CASE", "ORGANISATION");
        List<String> roleNmaes = Arrays.asList("judge", "senior judge");
        List<String> roleCategories = Arrays.asList("JUDICIAL");
        List<String> classifications = Arrays.asList("PUBLIC", "PRIVATE");
        Map<String, List<String>> attributes = new HashMap<>();
        List<String> regions = Arrays.asList("London", "JAPAN");
        List<String> contractTypes = Arrays.asList("SALARIED", "Non SALARIED");
        attributes.put("region", regions);
        attributes.put("contractType", contractTypes);
        List<String> grantTypes = Arrays.asList("SPECIFIC", "STANDARD");

        QueryRequest queryRequest = QueryRequest.builder()
            .actorId(actorId)
            .roleType(roleType)
            .roleCategory(roleCategories)
            .roleName(roleNmaes)
            .classification(classifications)
            .attributes(attributes)
            .validAt(LocalDateTime.now())
            .grantType(grantTypes)
            .build();

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(
            Pageable.class);

        Specification<RoleAssignmentEntity> spec = Specification.where(any());

        when(roleAssignmentRepository.findAll(spec, pageableCaptor.capture()
        ))
            .thenReturn(page);


        when(mockSpec.toPredicate(root, query, builder)).thenReturn(predicate);


        when(persistenceUtil.convertEntityToRoleAssignment(page.iterator().next()))
            .thenReturn(TestDataBuilder.buildRoleAssignment(Status.LIVE));

        List<RoleAssignment> roleAssignmentList = sut.retrieveRoleAssignmentsByQueryRequest(queryRequest, 1, 1, "id",
                                                                                            "desc"
        );
        assertNotNull(roleAssignmentList);

        assertNotNull(roleAssignmentList);
        assertFalse(roleAssignmentList.isEmpty());

        verify(persistenceUtil, times(1))
            .convertEntityToRoleAssignment(page.iterator().next());

    }

    @Test
    void postRoleAssignmentsByQueryRequest_ThrowsException() {

        List<String> actorId = Arrays.asList(
            "123e4567-e89b-42d3-a456-556642445678",
            "4dc7dd3c-3fb5-4611-bbde-5101a97681e1"
        );
        List<String> roleType = Arrays.asList("CASE", "ORGANISATION");
        QueryRequest queryRequest = QueryRequest.builder()
            .actorId(actorId)
            .roleType(roleType)
            .build();

        Specification<RoleAssignmentEntity> spec = Specification.where(any());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(
            Pageable.class);

        when(roleAssignmentRepository.findAll(spec, pageableCaptor.capture()
        ))
            .thenThrow(ResourceNotFoundException.class);


        Assertions.assertThrows(ResourceNotFoundException.class, () ->
            sut.retrieveRoleAssignmentsByQueryRequest(queryRequest, 1, 1, "id", "desc")
        );

    }

    @Test
    void postRoleAssignmentsByAuthorisations_ThrowsException() {

        List<String> authorisations = Arrays.asList(
            "dev",
            "ops"
        );

        QueryRequest queryRequest = QueryRequest.builder()
            .authorisations(authorisations)
            .build();

        Specification<RoleAssignmentEntity> spec = Specification.where(any());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(
            Pageable.class);

        when(roleAssignmentRepository.findAll(spec, pageableCaptor.capture()
        ))
            .thenThrow(ResourceNotFoundException.class);


        Assertions.assertThrows(ResourceNotFoundException.class, () ->
            sut.retrieveRoleAssignmentsByQueryRequest(queryRequest, 1, 1, "id", "desc")
        );

    }

    @Test
    void postRoleAssignmentsByAuthorisation() throws IOException {


        List<RoleAssignmentEntity> tasks = new ArrayList<>();
        tasks.add(TestDataBuilder.buildRoleAssignmentEntity(TestDataBuilder.buildRoleAssignment(Status.LIVE)));

        Page<RoleAssignmentEntity> page = new PageImpl<RoleAssignmentEntity>(tasks);


        List<String> authorisations = Arrays.asList(
            "dev",
            "tester"
        );

        QueryRequest queryRequest = QueryRequest.builder()
            .authorisations(authorisations)
            .build();

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(
            Pageable.class);

        Specification<RoleAssignmentEntity> spec = Specification.where(any());

        when(roleAssignmentRepository.findAll(spec, pageableCaptor.capture()
        ))
            .thenReturn(page);


        when(mockSpec.toPredicate(root, query, builder)).thenReturn(predicate);


        when(persistenceUtil.convertEntityToRoleAssignment(page.iterator().next()))
            .thenReturn(TestDataBuilder.buildRoleAssignment(Status.LIVE));

        List<RoleAssignment> roleAssignmentList = sut.retrieveRoleAssignmentsByQueryRequest(queryRequest, 1, 1, "id",
                                                                                            "desc"
        );
        assertNotNull(roleAssignmentList);

        assertNotNull(roleAssignmentList);
        assertFalse(roleAssignmentList.isEmpty());

        verify(persistenceUtil, times(1))
            .convertEntityToRoleAssignment(page.iterator().next());

    }
}
