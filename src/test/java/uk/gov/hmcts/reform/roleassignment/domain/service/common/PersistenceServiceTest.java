package uk.gov.hmcts.reform.roleassignment.domain.service.common;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.roleassignment.data.ActorCacheEntity;
import uk.gov.hmcts.reform.roleassignment.data.ActorCacheRepository;
import uk.gov.hmcts.reform.roleassignment.data.HistoryEntity;
import uk.gov.hmcts.reform.roleassignment.data.HistoryRepository;
import uk.gov.hmcts.reform.roleassignment.data.RequestEntity;
import uk.gov.hmcts.reform.roleassignment.data.RequestRepository;
import uk.gov.hmcts.reform.roleassignment.data.RoleAssignmentEntity;
import uk.gov.hmcts.reform.roleassignment.data.RoleAssignmentRepository;
import uk.gov.hmcts.reform.roleassignment.domain.model.ActorCache;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.Request;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;
import uk.gov.hmcts.reform.roleassignment.util.PersistenceUtil;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    @InjectMocks
    private PersistenceService sut = new PersistenceService(
        historyRepository, requestRepository, roleAssignmentRepository, persistenceUtil, actorCacheRepository);

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void persistRequest() throws IOException {
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
    void persistRequestToHistory() throws IOException {
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

        assertEquals(assignmentRequest.getRequest().getId(), historyEntityResult.getRequestEntity().getId());
        assertEquals(assignmentRequest.getRequestedRoles().iterator().next().getId(), historyEntityResult.getId());

        verify(persistenceUtil, times(1)).convertRequestToEntity(any(Request.class));
        verify(persistenceUtil, times(1)).convertRoleAssignmentToHistoryEntity(
            any(RoleAssignment.class),any(RequestEntity.class));
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
        ActorCacheEntity entity = new ActorCacheEntity(roleAssignment.getActorId(),1234, rootNode);
        ActorCache actorCache = TestDataBuilder.prepareActorCache(roleAssignment);
        when(persistenceUtil.convertActorCacheToEntity(any())).thenReturn(entity);
        when(actorCacheRepository.findByActorId(roleAssignment.getActorId())).thenReturn(entity);
        when(actorCacheRepository.save(entity)).thenReturn(entity);

        ActorCacheEntity result = sut.persistActorCache(roleAssignment);

        assertNotNull(result);
        assertNotNull(result.getActorId());

        verify(persistenceUtil, times(1)).convertActorCacheToEntity(any());
        verify(actorCacheRepository, times(1)).findByActorId(roleAssignment.getActorId());
    }

    /*@Test
    void getExistingRoleAssignment() throws IOException {
        RoleAssignment requestedRole = TestDataBuilder.buildAssignmentRequest().getRequestedRoles().iterator().next();
        Set<RoleAssignmentEntity> roleAssignmentEntities = new HashSet<>();
        roleAssignmentEntities.add(TestDataBuilder.convertRoleAssignmentToEntity(requestedRole));
        RoleAssignment existingRole = TestDataBuilder.convertRoleAssignmentEntityInModel(
            roleAssignmentEntities.iterator().next());
        UUID id = UUID.randomUUID();

        when(roleAssignmentRepository.findByActorId(id)).thenReturn(roleAssignmentEntities);
        when(persistenceUtil.convertRoleAssignmentEntityInModel(
            roleAssignmentEntities.iterator().next())).thenReturn(existingRole);

        sut.getExistingRoleAssignment(id);

        verify(roleAssignmentRepository, times(1)).findByActorId(id);
        verify(persistenceUtil, times(1)).convertRoleAssignmentEntityInModel(
            roleAssignmentEntities.iterator().next());
    }*/

    @Test
    void getActorCacheEntity() throws IOException {
        UUID id = UUID.randomUUID();
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

        sut.getAssignmentsByProcess("process", "reference", "status");

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
    }

    @Test
    void getAssignmentsByActor() throws IOException {
        UUID id = UUID.randomUUID();
        Set<RoleAssignmentEntity> roleAssignmentEntitySet = new HashSet<>();
        roleAssignmentEntitySet.add(TestDataBuilder.buildRoleAssignmentEntity(TestDataBuilder
                                                                           .buildRoleAssignment(Status.LIVE)));
        when(roleAssignmentRepository.findByActorId(id))
            .thenReturn(roleAssignmentEntitySet);
        when(persistenceUtil.convertEntityToRoleAssignment(roleAssignmentEntitySet.iterator().next()))
            .thenReturn(TestDataBuilder.buildRoleAssignment(Status.LIVE));
        List<RoleAssignment> roleAssignmentList = sut.getAssignmentsByActor(id);
        assertNotNull(roleAssignmentList);

        verify(persistenceUtil, times(1))
            .convertEntityToRoleAssignment(roleAssignmentEntitySet.iterator().next());
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
}
