package uk.gov.hmcts.reform.roleassignment.domain.service.common;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.roleassignment.data.cachecontrol.ActorCacheEntity;
import uk.gov.hmcts.reform.roleassignment.data.cachecontrol.ActorCacheRepository;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.HistoryEntity;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.HistoryRepository;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.RequestEntity;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.RequestRepository;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.RoleAssignmentEntity;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.RoleAssignmentRepository;
import uk.gov.hmcts.reform.roleassignment.domain.model.ActorCache;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.Request;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;
import uk.gov.hmcts.reform.roleassignment.util.PersistenceUtil;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
        Request request = TestDataBuilder.buildAssignmentRequest().getRequest();
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
        Request request = TestDataBuilder.buildAssignmentRequest().getRequest();
        RequestEntity requestEntity = TestDataBuilder.buildRequestEntity(request);
        try {
            sut.persistRequestToHistory(requestEntity);
        } catch (Exception e) {
            throw new InternalError(e);
        }
    }


    @Test
    void persistHistory() throws IOException {
        AssignmentRequest assignmentRequest = TestDataBuilder.buildAssignmentRequest();
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
        verify(persistenceUtil, times(1)).convertRequestToEntity(any(Request.class));
        verify(persistenceUtil, times(1)).convertRoleAssignmentToHistoryEntity(
            any(RoleAssignment.class),any(RequestEntity.class));
        verify(historyRepository, times(1)).save(any(HistoryEntity.class));
    }

    @Test
    void persistRoleAssignment() throws IOException {
        AssignmentRequest assignmentRequest = TestDataBuilder.buildAssignmentRequest();
        RoleAssignmentEntity roleAssignmentEntity = TestDataBuilder.convertRoleAssignmentToEntity(
            assignmentRequest.getRequestedRoles().iterator().next());
        when(persistenceUtil.convertRoleAssignmentToEntity(
            assignmentRequest.getRequestedRoles().iterator().next())).thenReturn(roleAssignmentEntity);

        sut.persistRoleAssignment(assignmentRequest.getRequestedRoles().iterator().next());

        verify(persistenceUtil, times(1)).convertRoleAssignmentToEntity(any(RoleAssignment.class));
    }

    @Test
    void persistActorCache() throws IOException {
        RoleAssignment roleAssignment = TestDataBuilder.buildRoleAssignment();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.createObjectNode();
        ActorCacheEntity entity = new ActorCacheEntity(roleAssignment.actorId,1234, rootNode);
        ActorCache actorCache = TestDataBuilder.prepareActorCache(roleAssignment);
        when(persistenceUtil.convertActorCacheToEntity(any())).thenReturn(entity);
        when(actorCacheRepository.findByActorId(roleAssignment.actorId)).thenReturn(entity);

        sut.persistActorCache(roleAssignment);

        verify(persistenceUtil, times(1)).convertActorCacheToEntity(any());
        verify(actorCacheRepository, times(1)).findByActorId(roleAssignment.actorId);
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
    void getActorCacheEntity() {
        UUID id = UUID.randomUUID();
        ActorCacheEntity actorCacheEntity = new ActorCacheEntity();
        when(actorCacheRepository.findByActorId(id)).thenReturn(actorCacheEntity);
        sut.getActorCacheEntity(id);
        verify(actorCacheRepository, times(1)).findByActorId(id);
    }

    @Test
    void getExistingRoleByProcessAndReference() throws IOException {
        AssignmentRequest assignmentRequest = TestDataBuilder.buildAssignmentRequest();
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
        AssignmentRequest assignmentRequest = TestDataBuilder.buildAssignmentRequest();
        RoleAssignmentEntity roleAssignmentEntity = TestDataBuilder.convertRoleAssignmentToEntity(
            assignmentRequest.getRequestedRoles().iterator().next());

        when(persistenceUtil.convertRoleAssignmentToEntity(
            assignmentRequest.getRequestedRoles().iterator().next())).thenReturn(roleAssignmentEntity);

        sut.deleteRoleAssignment(assignmentRequest.getRequestedRoles().iterator().next());

        verify(persistenceUtil, times(1)).convertRoleAssignmentToEntity(any(RoleAssignment.class));
    }
}
