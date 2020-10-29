package uk.gov.hmcts.reform.roleassignment.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.roleassignment.data.RoleAssignmentEntity;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
class PersistenceUtilTest {

    @InjectMocks
    PersistenceUtil persistenceUtil = new PersistenceUtil();


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void convertRoleAssignmentToHistoryEntity() throws IOException {
        assertNotNull(persistenceUtil.convertRoleAssignmentToHistoryEntity(
            TestDataBuilder.buildRoleAssignment(Status.LIVE),
            TestDataBuilder.buildRequestEntity(TestDataBuilder.buildRequest(Status.APPROVED, false))));
    }

    @Test
    void convertRequestToEntity() {
        assertNotNull(persistenceUtil.convertRequestToEntity(TestDataBuilder.buildRequest(Status.APPROVED, false)));
    }

    @Test
    void convertRoleAssignmentToEntity() throws IOException {
        assertNotNull(persistenceUtil.convertRoleAssignmentToEntity(
            TestDataBuilder.buildRoleAssignment(Status.LIVE),
            true
        ));
    }

    @Test
    void convertActorCacheToEntity() throws IOException {
        assertNotNull(persistenceUtil.convertActorCacheToEntity(TestDataBuilder.buildActorCache()));
    }

    @Test
    void convertHistoryEntityToRoleAssignment() throws IOException {
        assertNotNull(persistenceUtil.convertHistoryEntityToRoleAssignment(
            TestDataBuilder.buildHistoryEntity(
                TestDataBuilder.buildRoleAssignment(Status.LIVE),
                TestDataBuilder.buildRequestEntity(TestDataBuilder.buildRequest(Status.APPROVED, false)))));
    }

    @Test
    void convertEntityToRoleAssignment() throws IOException {
        assertNotNull(persistenceUtil.convertEntityToRoleAssignment(
            TestDataBuilder.buildRoleAssignmentEntity(TestDataBuilder.buildRoleAssignment(Status.LIVE))));
    }

    @Test
    void convertEntityToRoleAssignmentWithAutorisations() throws IOException {

        RoleAssignmentEntity entity =  TestDataBuilder.buildRoleAssignmentEntity(TestDataBuilder
                                                            .buildRoleAssignment(Status.LIVE));
        entity.setAuthorisations("dev;tester");
        assertNotNull(persistenceUtil.convertEntityToRoleAssignment(
            entity));
    }

    /*@Test
    void persistHistory() throws IOException {
        AssignmentRequest assignmentRequest = TestDataBuilder
            .buildAssignmentRequest(Status.CREATED, Status.LIVE, false);
        RequestEntity requestEntity = TestDataBuilder.buildRequestEntity(assignmentRequest.getRequest());
        HistoryEntity historyEntity = TestDataBuilder.buildHistoryIntoEntity(
            assignmentRequest.getRequestedRoles().iterator().next(), requestEntity);

        when(persistenceUtil.convertRequestToEntity(assignmentRequest.getRequest())).thenReturn(requestEntity);
        when(persistenceUtil.convertRoleAssignmentToHistoryEntity(
            assignmentRequest.getRequestedRoles().iterator().next(), requestEntity)).thenReturn(historyEntity);

        HistoryEntity historyEntityResult = sut.prepareHistoryEntityForPersistance(
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
        verify(entityManager, times(1)).persist(any(HistoryEntity.class));
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

        HistoryEntity historyEntityResult = sut.prepareHistoryEntityForPersistance(
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
        verify(entityManager, times(1)).persist(any(HistoryEntity.class));
    }*/
}
