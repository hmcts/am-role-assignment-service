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
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentRequestResource;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.Request;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentSubset;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RequestType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ParseRequestService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PrepareResponseService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ValidationModelService;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;
import uk.gov.hmcts.reform.roleassignment.util.PersistenceUtil;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.List;
import java.util.HashSet;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.REJECTED;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.APPROVED;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.CREATED;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.LIVE;
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
        existingAssignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(Status.DELETED, roleAssignment.getStatus());
        });
        verify(persistenceService, times(2))
            .deleteRoleAssignment(any(RoleAssignment.class));
        verify(persistenceService, times(1))
            .persistActorCache(anyCollection());
        verify(persistenceUtil, times(2))
            .prepareHistoryEntityForPersistance(any(RoleAssignment.class), any(Request.class));
        verify(persistenceService, times(1))
            .persistHistoryEntities(any());
    }


    @Test
    void checkAllDeleteApproved_whenExecuteReplaceRequest() throws IOException, ParseException {

        incomingAssignmentRequest = TestDataBuilder.buildAssignmentRequest(CREATED, APPROVED,
                                                                           false
        );
        incomingAssignmentRequest.getRequest().setAssignerId(incomingAssignmentRequest.getRequest()
                                                                 .getAuthenticatedUserId());
        incomingAssignmentRequest.getRequestedRoles().forEach(roleAssignment ->
                                                                  roleAssignment.setGrantType(GrantType.SPECIFIC));
        existingAssignmentRequest.getRequestedRoles().forEach(roleAssignment ->
                                                                  roleAssignment.setGrantType(GrantType.SPECIFIC));

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

        assertEquals(REJECTED, incomingAssignmentRequest.getRequest().getStatus());
        assertEquals(REJECTED.toString(), sut.getRequestEntity().getStatus());

        verify(persistenceService, times(5))
            .updateRequest(any(RequestEntity.class));
        verify(persistenceUtil, times(8))
            .prepareHistoryEntityForPersistance(any(RoleAssignment.class), any(Request.class));
        verify(validationModelService, times(1))
            .validateRequest(any(AssignmentRequest.class));
        verify(persistenceService, times(4))
            .persistHistoryEntities(any());
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
        assertEquals(APPROVED, incomingAssignmentRequest.getRequest().getStatus());
        assertEquals("Request has been approved", incomingAssignmentRequest.getRequest().getLog());
        assertEquals(APPROVED.toString(), sut.getRequestEntity().getStatus());
        assertEquals(incomingAssignmentRequest.getRequest().getLog(), sut.getRequestEntity().getLog());
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

        incomingAssignmentRequest = TestDataBuilder.buildAssignmentRequest(CREATED, LIVE,
                                                                           false
        );

        when(prepareResponseService.prepareCreateRoleResponse(any()))
            .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(
                new RoleAssignmentRequestResource(incomingAssignmentRequest)));
        sut.setRequestEntity(requestEntity);

        //Call actual Method
        ResponseEntity<RoleAssignmentRequestResource> response = sut.duplicateRequest(existingAssignmentRequest,
                                                                                      incomingAssignmentRequest);
        RoleAssignmentRequestResource roleAssignmentRequestResource = response.getBody();
        AssignmentRequest result =  roleAssignmentRequestResource.getRoleAssignmentRequest();

        String msg = "Duplicate Request: Requested Assignments are already live.";

        //assertion
        assertEquals(incomingAssignmentRequest, result);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(msg, result.getRequest().getLog());
        assertEquals(APPROVED, incomingAssignmentRequest.getRequest().getStatus());
        assertEquals(msg, incomingAssignmentRequest.getRequest().getLog());
        assertEquals(APPROVED.toString(), sut.getRequestEntity().getStatus());
        assertEquals(msg, sut.getRequestEntity().getLog());
        assertEquals(existingAssignmentRequest.getRequestedRoles(), incomingAssignmentRequest.getRequestedRoles());

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
        assertEquals(APPROVED, incomingAssignmentRequest.getRequest().getStatus());
        assertEquals(APPROVED.toString(), sut.getRequestEntity().getStatus());
        assertTrue(incomingAssignmentRequest.getRequest().getLog()
                       .contains("Request has been approved"));
        assertTrue(sut.getRequestEntity().getLog()
                       .contains("Request has been approved"));

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
        incomingAssignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(REJECTED, roleAssignment.getStatus());
        });
        assertEquals(REJECTED, incomingAssignmentRequest.getRequest().getStatus());
        assertEquals(REJECTED.toString(), sut.getRequestEntity().getStatus());

        assertTrue(incomingAssignmentRequest.getRequest().getLog()
                       .contains("Request has been rejected due to following assignment Ids :"));
        assertTrue(sut.getRequestEntity().getLog()
                       .contains("Request has been rejected due to following assignment Ids :"));

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

    @Test
    void checkUpdateExistingAssignmentNoMatchingID() throws IOException {


        incomingAssignmentRequest = TestDataBuilder.buildAssignmentRequest(CREATED, LIVE,
                                                                           false
        );
        Set<HistoryEntity> historyEntities = new HashSet<>();

        historyEntities.add(historyEntity);

        Map<UUID, RoleAssignmentSubset> needToDeleteRoleAssignments = new HashMap<>();

        RoleAssignmentSubset roleAssignmentSubset = RoleAssignmentSubset.builder().build();
        needToDeleteRoleAssignments.put(UUID.randomUUID(), roleAssignmentSubset);

        requestEntity.setHistoryEntities(historyEntities);
        sut.setRequestEntity(requestEntity);
        sut.setNeedToDeleteRoleAssignments(needToDeleteRoleAssignments);

        // actual Method call
        sut.updateExistingAssignments(incomingAssignmentRequest);

        //assertion
        assertEquals(0,incomingAssignmentRequest.getRequestedRoles().size());
        assertEquals(2,sut.needToRetainRoleAssignments.size());

        verify(persistenceService, times(1))
            .updateRequest(any(RequestEntity.class));
        verify(persistenceService, times(1))
            .persistHistoryEntities(any());

    }

    @Test
    void checkUpdateNewAssignments() throws IOException, InvocationTargetException, IllegalAccessException {

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

        RoleAssignmentSubset roleAssignmentSubset = RoleAssignmentSubset.builder().build();
        roleAssignmentSubset
            .setActorId(incomingAssignmentRequest.getRequestedRoles().iterator().next().getActorId());
        roleAssignmentSubset
            .setActorIdType(incomingAssignmentRequest.getRequestedRoles().iterator().next().getActorIdType());
        roleAssignmentSubset
            .setRoleType(incomingAssignmentRequest.getRequestedRoles().iterator().next().getRoleType());
        roleAssignmentSubset
            .setRoleName(incomingAssignmentRequest.getRequestedRoles().iterator().next().getRoleName());
        roleAssignmentSubset
            .setClassification(incomingAssignmentRequest.getRequestedRoles().iterator().next().getClassification());
        roleAssignmentSubset
            .setGrantType(incomingAssignmentRequest.getRequestedRoles().iterator().next().getGrantType());
        roleAssignmentSubset
            .setRoleCategory(incomingAssignmentRequest.getRequestedRoles().iterator().next().getRoleCategory());
        roleAssignmentSubset
            .setAttributes(incomingAssignmentRequest.getRequestedRoles().iterator().next().getAttributes());
        roleAssignmentSubset
            .setNotes(incomingAssignmentRequest.getRequestedRoles().iterator().next().getNotes());
        roleAssignmentSubset
            .setBeginTime(incomingAssignmentRequest.getRequestedRoles().iterator().next().getBeginTime());
        roleAssignmentSubset
            .setEndTime(incomingAssignmentRequest.getRequestedRoles().iterator().next().getEndTime());
        roleAssignmentSubset.setReadOnly(true);

        Set<RoleAssignmentSubset> needToCreateRoleAssignments = new HashSet<>();
        Map<UUID, RoleAssignmentSubset> needToDeleteRoleAssignments = new HashMap<>();
        needToCreateRoleAssignments.add(roleAssignmentSubset);
        sut.setNeedToCreateRoleAssignments(needToCreateRoleAssignments);
        sut.setNeedToDeleteRoleAssignments(needToDeleteRoleAssignments);

        Set<RoleAssignment> needToRetainRoleAssignments = new HashSet<>();
        sut.setNeedToRetainRoleAssignments(needToRetainRoleAssignments);

        //actual method call
        sut.updateNewAssignments(existingAssignmentRequest,incomingAssignmentRequest);

        //assertion
        assertEquals(1, incomingAssignmentRequest.getRequestedRoles().size());
        assertFalse(existingAssignmentRequest.getRequestedRoles().isEmpty());
        assertTrue(sut.needToDeleteRoleAssignments.isEmpty());
        assertFalse(sut.needToRetainRoleAssignments.isEmpty());

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
