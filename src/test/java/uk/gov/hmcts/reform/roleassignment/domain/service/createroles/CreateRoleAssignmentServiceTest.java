package uk.gov.hmcts.reform.roleassignment.domain.service.createroles;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.roleassignment.data.HistoryEntity;
import uk.gov.hmcts.reform.roleassignment.data.RequestEntity;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.Request;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentSubset;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RequestType;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ParseRequestService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PrepareResponseService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ValidationModelService;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;
import uk.gov.hmcts.reform.roleassignment.util.PersistenceUtil;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.APPROVED;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.CREATED;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.DELETE_APPROVED;

class CreateRoleAssignmentServiceTest {

    @Mock
    private ParseRequestService parseRequestService = mock(ParseRequestService.class);
    @Mock
    private PersistenceService persistenceService = mock(PersistenceService.class);
    @Mock
    private ValidationModelService validationModelService = mock(ValidationModelService.class);
    @Mock
    private PersistenceUtil persistenceUtil = mock(PersistenceUtil.class);
    @Mock
    private PrepareResponseService prepareResponseService = mock(PrepareResponseService.class);

    AssignmentRequest existingAssignmentRequest;
    AssignmentRequest incomingAssignmentRequest;
    RequestEntity requestEntity;
    HistoryEntity historyEntity;

    @InjectMocks
    private CreateRoleAssignmentService sut = new CreateRoleAssignmentService(
        parseRequestService,
        persistenceService,
        validationModelService,
        persistenceUtil,
        prepareResponseService
    );

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    void checkAllDeleteApproved_WhenDeleteExistingRecords() throws IOException, ParseException {

        inputForCheckAllDeleteApproved();
        incomingAssignmentRequest = TestDataBuilder.buildAssignmentRequest(CREATED, DELETE_APPROVED,
                                                                           false
        );
        Set<HistoryEntity> historyEntities = new HashSet<>();

        historyEntities.add(historyEntity);




        RoleAssignmentSubset roleAssignmentSubset = RoleAssignmentSubset.builder().build();
        Map<UUID, RoleAssignmentSubset> needToDeleteRoleAssignments = new HashMap<>();
        needToDeleteRoleAssignments.put(UUID.randomUUID(), roleAssignmentSubset);

        requestEntity.setHistoryEntities(historyEntities);
        sut.setNeedToDeleteRoleAssignments(needToDeleteRoleAssignments);
        sut.setRequestEntity(requestEntity);
        Set<RoleAssignmentSubset> needToCreateRoleAssignments = new HashSet<>();
        sut.setNeedToCreateRoleAssignments(needToCreateRoleAssignments);


        when(persistenceService.getAssignmentsByProcess(anyString(), anyString(), anyString()))
            .thenReturn((List<RoleAssignment>) existingAssignmentRequest.getRequestedRoles());

        when(parseRequestService.parseRequest(any(AssignmentRequest.class), any(RequestType.class))).thenReturn(
            existingAssignmentRequest);
        when(persistenceService.persistRequest(any(Request.class))).thenReturn(requestEntity);
        when(persistenceService.persistHistory(
            any(RoleAssignment.class),
            any(Request.class)
        )).thenReturn(historyEntity);

        //Call actual Method
        sut.checkAllDeleteApproved(existingAssignmentRequest, incomingAssignmentRequest);


        //assertion
        verify(persistenceService, times(2))
            .updateRequest(any(RequestEntity.class));

        verify(persistenceService, times(2))
            .deleteRoleAssignment(any(RoleAssignment.class));
        verify(persistenceService, times(2))
            .persistActorCache(any(RoleAssignment.class));
        verify(persistenceService, times(2))
            .persistHistory(any(RoleAssignment.class), any(Request.class));

    }


    @Test
    void checkAllDeleteApproved_whenExecuteReplaceRequest() throws IOException, ParseException {

        inputForCheckAllDeleteApproved();
        incomingAssignmentRequest = TestDataBuilder.buildAssignmentRequest(CREATED, APPROVED,
                                                                           false
        );
        Set<HistoryEntity> historyEntities = new HashSet<>();

        historyEntities.add(historyEntity);

        Map<UUID, RoleAssignmentSubset> needToDeleteRoleAssignments = new HashMap<>();
        Set<RoleAssignmentSubset> needToCreateRoleAssignments = new HashSet<>();

        RoleAssignmentSubset roleAssignmentSubset = RoleAssignmentSubset.builder().build();
        needToDeleteRoleAssignments.put(UUID.randomUUID(), roleAssignmentSubset);
        needToCreateRoleAssignments.add(roleAssignmentSubset);

        requestEntity.setHistoryEntities(historyEntities);
        sut.setRequestEntity(requestEntity);
        sut.setNeedToDeleteRoleAssignments(needToDeleteRoleAssignments);
        sut.setNeedToCreateRoleAssignments(needToCreateRoleAssignments);

        when(persistenceService.getAssignmentsByProcess(anyString(), anyString(), anyString()))
            .thenReturn((List<RoleAssignment>) existingAssignmentRequest.getRequestedRoles());

        when(parseRequestService.parseRequest(any(AssignmentRequest.class), any(RequestType.class))).thenReturn(
            existingAssignmentRequest);
        when(persistenceService.persistRequest(any(Request.class))).thenReturn(requestEntity);
        when(persistenceService.persistHistory(
            any(RoleAssignment.class),
            any(Request.class)
        )).thenReturn(historyEntity);


        //Call actual Method
        sut.checkAllDeleteApproved(existingAssignmentRequest, incomingAssignmentRequest);


        //assertion
        /*  verify(persistenceService, times(5))
            .updateRequest(any(RequestEntity.class));

        verify(persistenceService, times(2))
            .deleteRoleAssignment(any(RoleAssignment.class));
        verify(persistenceService, times(2))
            .persistActorCache(any(RoleAssignment.class));
        verify(persistenceService, times(2))
            .persistHistory(any(RoleAssignment.class),any(Request.class));*/

    }


    private void inputForCheckAllDeleteApproved() throws IOException {
        existingAssignmentRequest = TestDataBuilder.buildAssignmentRequest(CREATED, DELETE_APPROVED,
                                                                           false
        );

        existingAssignmentRequest.getRequest().setReplaceExisting(true);
        requestEntity = TestDataBuilder.buildRequestEntity(existingAssignmentRequest.getRequest());


        historyEntity = TestDataBuilder.buildHistoryIntoEntity(
            existingAssignmentRequest.getRequestedRoles().iterator().next(), requestEntity);
    }

    @Test
    void createNewAssignmentRecords() {
    }

    @Test
    void persistInitialRequest() {
    }

    @Test
    void hasAssignmentsUpdated() {
    }

    @Test
    void duplicateRequest() {
    }

    @Test
    void updateNewAssignments() {
    }

    @Test
    void updateExistingAssignments() {
    }

    @Test
    void retrieveExistingAssignments() {
    }

    @Test
    void checkAllApproved() {
    }

    @Test
    void getParseRequestService() {
    }

    @Test
    void getPersistenceService() {
    }

    @Test
    void getValidationModelService() {
    }

    @Test
    void getPersistenceUtil() {
    }

    @Test
    void getPrepareResponseService() {
    }

    @Test
    void getRequestEntity() {
    }

    @Test
    void setCreatedTimeComparator() {
    }

    @Test
    void setNeedToDeleteRoleAssignments() {
    }

    @Test
    void setNeedToCreateRoleAssignments() {
    }

    @Test
    void setNeedToRetainRoleAssignments() {
    }

    @Test
    void testEquals() {
    }

    @Test
    void canEqual() {
    }

    @Test
    void testHashCode() {
    }

    @Test
    void testToString() {
    }
}
