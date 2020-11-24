package uk.gov.hmcts.reform.roleassignment.domain.service.createroles;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.APPROVED;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.CREATED;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.DELETE_APPROVED;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.LIVE;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.REJECTED;

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
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        prepareInput();
    }


    @Test
    void checkAllDeleteApproved_WhenDeleteExistingRecords() throws IOException, ParseException {

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
        when(persistenceUtil.prepareHistoryEntityForPersistance(
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
        verify(persistenceService, times(1))
            .persistActorCache(anyCollection());
        verify(persistenceUtil, times(2))
            .prepareHistoryEntityForPersistance(any(RoleAssignment.class), any(Request.class));

    }


    @Test
    void checkAllDeleteApproved_whenExecuteReplaceRequest() throws IOException, ParseException {

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
        when(persistenceUtil.prepareHistoryEntityForPersistance(
            any(RoleAssignment.class),
            any(Request.class)
        )).thenReturn(historyEntity);


        //Call actual Method
        sut.checkAllDeleteApproved(existingAssignmentRequest, incomingAssignmentRequest);


        //assertion
        verify(persistenceService, times(5))
            .updateRequest(any(RequestEntity.class));
        verify(persistenceUtil, times(8))
            .prepareHistoryEntityForPersistance(any(RoleAssignment.class), any(Request.class));
        verify(validationModelService, times(1))
            .validateRequest(any(AssignmentRequest.class));

    }

    @Test
    void check_ExecuteReplaceRequest() throws IOException, ParseException {


        incomingAssignmentRequest = TestDataBuilder.buildAssignmentRequest(CREATED, LIVE,
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
        when(persistenceUtil.prepareHistoryEntityForPersistance(
            any(RoleAssignment.class),
            any(Request.class)
        )).thenReturn(historyEntity);


        // actual Method call
        sut.executeReplaceRequest(existingAssignmentRequest, incomingAssignmentRequest);


        //assertion
        verify(persistenceService, times(3))
            .updateRequest(any(RequestEntity.class));

        verify(persistenceService, times(2))
            .deleteRoleAssignment(any(RoleAssignment.class));
        verify(persistenceService, times(2))
            .persistActorCache(anyCollection());
        verify(persistenceUtil, times(2))
            .prepareHistoryEntityForPersistance(any(RoleAssignment.class), any(Request.class));

    }

    @Test
    void check_DuplicateRequest() throws IOException {

        String msg = "Duplicate Request: Requested Assignments are already live.";
        incomingAssignmentRequest = TestDataBuilder.buildAssignmentRequest(APPROVED, LIVE,
                                                                           false
        );
        incomingAssignmentRequest.getRequest().setLog(msg);

        when(prepareResponseService.prepareCreateRoleResponse(any()))
            .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(incomingAssignmentRequest));
        sut.setRequestEntity(requestEntity);

        //Call actual Method
        ResponseEntity<Object> response = sut.duplicateRequest(existingAssignmentRequest, incomingAssignmentRequest);
        AssignmentRequest result = (AssignmentRequest) response.getBody();

        //assertion
        assertEquals(incomingAssignmentRequest, result);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(msg, result.getRequest().getLog());
        assertEquals(incomingAssignmentRequest, result);

        verify(prepareResponseService, times(1))
            .prepareCreateRoleResponse(any(AssignmentRequest.class));
        verify(persistenceService, times(1))
            .updateRequest(any(RequestEntity.class));
        verify(parseRequestService, times(1))
            .removeCorrelationLog();
    }

    @Test
    void checkAllApproved_ByDrool() throws IOException {

        incomingAssignmentRequest = TestDataBuilder.buildAssignmentRequest(CREATED, APPROVED,
                                                                           false
        );
        existingAssignmentRequest = TestDataBuilder.buildAssignmentRequest(CREATED, APPROVED,
                                                                           false
        );
        //prepare request entity
        requestEntity = TestDataBuilder.buildRequestEntity(existingAssignmentRequest.getRequest());

        sut.setRequestEntity(requestEntity);

        //build history entity
        historyEntity = TestDataBuilder.buildHistoryIntoEntity(
            existingAssignmentRequest.getRequestedRoles().iterator().next(), requestEntity);

        //set history entity into request entity
        Set<HistoryEntity> historyEntities = new HashSet<>();
        historyEntities.add(historyEntity);
        requestEntity.setHistoryEntities(historyEntities);

        when(persistenceUtil.convertHistoryEntityToRoleAssignment(any(HistoryEntity.class)))
            .thenReturn(existingAssignmentRequest.getRequestedRoles().iterator().next());
        when(persistenceUtil.prepareHistoryEntityForPersistance(
            any(RoleAssignment.class),
            any(Request.class)
        )).thenReturn(historyEntity);


        //actual method call
        sut.checkAllApproved(incomingAssignmentRequest);

        //assertion
        verify(persistenceService, times(2))
            .updateRequest(any(RequestEntity.class));

        verify(persistenceService, times(1))
            .persistActorCache(anyCollection());
        verify(persistenceUtil, times(2))
            .prepareHistoryEntityForPersistance(any(RoleAssignment.class), any(Request.class));
        verify(persistenceService, times(1))
            .persistRoleAssignments(anyList());
    }

    @Test
    void checkAllApproved_ByDrool_Rejected_Scenario() throws IOException {

        incomingAssignmentRequest = TestDataBuilder.buildAssignmentRequest(CREATED, REJECTED,
                                                                           false
        );
        existingAssignmentRequest = TestDataBuilder.buildAssignmentRequest(CREATED, REJECTED,
                                                                           false
        );
        //prepare request entity
        requestEntity = TestDataBuilder.buildRequestEntity(existingAssignmentRequest.getRequest());

        sut.setRequestEntity(requestEntity);

        //build history entity
        historyEntity = TestDataBuilder.buildHistoryIntoEntity(
            existingAssignmentRequest.getRequestedRoles().iterator().next(), requestEntity);

        //set history entity into request entity
        Set<HistoryEntity> historyEntities = new HashSet<>();
        historyEntities.add(historyEntity);
        requestEntity.setHistoryEntities(historyEntities);

        when(persistenceUtil.convertHistoryEntityToRoleAssignment(any(HistoryEntity.class)))
            .thenReturn(existingAssignmentRequest.getRequestedRoles().iterator().next());
        when(persistenceUtil.prepareHistoryEntityForPersistance(
            any(RoleAssignment.class),
            any(Request.class)
        )).thenReturn(historyEntity);

        //actual method call
        sut.checkAllApproved(incomingAssignmentRequest);

        //assertion
        verify(persistenceService, times(2))
            .updateRequest(any(RequestEntity.class));

        verify(persistenceService, times(0))
            .persistActorCache(anyCollection());
        verify(persistenceUtil, times(0))
            .prepareHistoryEntityForPersistance(any(RoleAssignment.class), any(Request.class));
        verify(persistenceService, times(0))
            .persistRoleAssignments(anyList());
    }

    @Test
    void identifyRoleAssignments_FromIncomingRequest() {

        Map<UUID, RoleAssignmentSubset> existingRecords = new HashMap<>();
        Set<RoleAssignmentSubset> incomingRecords = new HashSet<>();
        Map<UUID, RoleAssignmentSubset> commonRecords = new HashMap<>();
        RoleAssignmentSubset roleAssignmentSubset = RoleAssignmentSubset.builder().build();
        incomingRecords.add(roleAssignmentSubset);

        //actual method call
        sut.identifyRoleAssignments(existingRecords, incomingRecords, commonRecords);

        //assertion
        assertEquals(incomingRecords, sut.needToCreateRoleAssignments);


    }

    @Test
    void identifyRoleAssignments_FromExistingRequest() {
        Map<UUID, RoleAssignmentSubset> existingRecords = new HashMap<>();
        Set<RoleAssignmentSubset> incomingRecords = new HashSet<>();
        Map<UUID, RoleAssignmentSubset> commonRecords = new HashMap<>();
        RoleAssignmentSubset roleAssignmentSubset = RoleAssignmentSubset.builder().build();
        existingRecords.put(UUID.randomUUID(), roleAssignmentSubset);

        //actual method call
        sut.identifyRoleAssignments(existingRecords, incomingRecords, commonRecords);

        //assertion
        assertEquals(existingRecords, sut.needToDeleteRoleAssignments);
        assertEquals(existingRecords.values(), sut.needToDeleteRoleAssignments.values());
    }


    private void prepareInput() throws IOException {
        existingAssignmentRequest = TestDataBuilder.buildAssignmentRequest(CREATED, DELETE_APPROVED,
                                                                           false
        );

        requestEntity = TestDataBuilder.buildRequestEntity(existingAssignmentRequest.getRequest());


        historyEntity = TestDataBuilder.buildHistoryIntoEntity(
            existingAssignmentRequest.getRequestedRoles().iterator().next(), requestEntity);
    }


}
