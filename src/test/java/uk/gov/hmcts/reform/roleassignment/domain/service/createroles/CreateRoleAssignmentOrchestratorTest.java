package uk.gov.hmcts.reform.roleassignment.domain.service.createroles;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.roleassignment.data.HistoryEntity;
import uk.gov.hmcts.reform.roleassignment.data.RequestEntity;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.Request;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RequestType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ParseRequestService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PrepareResponseService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ValidationModelService;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;
import uk.gov.hmcts.reform.roleassignment.util.PersistenceUtil;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.APPROVED;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.CREATED;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.LIVE;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.REJECTED;

@RunWith(MockitoJUnitRunner.class)
class CreateRoleAssignmentOrchestratorTest {

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

    AssignmentRequest assignmentRequest;
    RequestEntity requestEntity;
    HistoryEntity historyEntity;

    @InjectMocks
    private CreateRoleAssignmentOrchestrator sut = new CreateRoleAssignmentOrchestrator(
        parseRequestService,
        prepareResponseService,
        persistenceService,
        validationModelService,
        persistenceUtil

    );

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void createRoleAssignment_ReplaceFalse_AcceptRoleRequests() throws Exception {
        assignmentRequest = TestDataBuilder.buildAssignmentRequest(CREATED, APPROVED, false);
        requestEntity = TestDataBuilder.buildRequestEntity(assignmentRequest.getRequest());
        historyEntity = TestDataBuilder.buildHistoryIntoEntity(
            assignmentRequest.getRequestedRoles().iterator().next(), requestEntity);

        when(parseRequestService.parseRequest(any(AssignmentRequest.class), any(RequestType.class)))
            .thenReturn(
                assignmentRequest);
        when(persistenceService.persistRequest(any(Request.class))).thenReturn(requestEntity);
        when(persistenceService.persistHistory(
            any(RoleAssignment.class),
            any(Request.class)
        )).thenReturn(historyEntity);

        when(prepareResponseService.prepareCreateRoleResponse(any()))
            .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(assignmentRequest));

        doNothing().when(validationModelService).validateRequest(any());

        //actual method call
        ResponseEntity<Object> response = sut.createRoleAssignment(assignmentRequest);
        AssignmentRequest result = (AssignmentRequest) response.getBody();


        //assert values
        assert result != null;
        assertEquals(assignmentRequest, result);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(result.getRequest().getId());
        verifyNUmberOfInvocationsForRejectedRequest();
        verify(parseRequestService, times(1)).removeCorrelationLog();
    }

    @Test
    void createRoleAssignment_ReplaceFalse_RejectRoleRequests() throws Exception {
        assignmentRequest = TestDataBuilder.buildAssignmentRequest(REJECTED, LIVE, false);
        requestEntity = TestDataBuilder.buildRequestEntity(assignmentRequest.getRequest());
        historyEntity = TestDataBuilder.buildHistoryIntoEntity(
            assignmentRequest.getRequestedRoles().iterator().next(), requestEntity);

        when(parseRequestService.parseRequest(any(AssignmentRequest.class), any(RequestType.class)))
            .thenReturn(
                assignmentRequest);
        when(persistenceService.persistRequest(any(Request.class))).thenReturn(requestEntity);
        when(persistenceService.persistHistory(
            any(RoleAssignment.class),
            any(Request.class)
        )).thenReturn(historyEntity);

        when(prepareResponseService.prepareCreateRoleResponse(any()))
            .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(assignmentRequest));

        //actual method call
        ResponseEntity<Object> response = sut.createRoleAssignment(assignmentRequest);
        AssignmentRequest result = (AssignmentRequest) response.getBody();

        //assert values
        assertEquals(assignmentRequest, result);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        for (RoleAssignment requestedRole : result.getRequestedRoles()) {
            assertEquals(REJECTED, requestedRole.getStatus());
        }
        verifyNUmberOfInvocationsForRejectedRequest();
    }


    @Test
    void createRoleAssignment_ReplaceTrue_RejectRoleRequests() throws Exception {
        assignmentRequest = TestDataBuilder.buildAssignmentRequest(REJECTED, Status.LIVE,
                                                                   false
        );
        assignmentRequest.getRequest().setReplaceExisting(true);
        requestEntity = TestDataBuilder.buildRequestEntity(assignmentRequest.getRequest());


        historyEntity = TestDataBuilder.buildHistoryIntoEntity(
            assignmentRequest.getRequestedRoles().iterator().next(), requestEntity);

        when(persistenceService.getAssignmentsByProcess(anyString(), anyString(), anyString()))
            .thenReturn((List<RoleAssignment>) assignmentRequest.getRequestedRoles());

        when(parseRequestService.parseRequest(any(AssignmentRequest.class), any(RequestType.class))).thenReturn(
            assignmentRequest);
        when(persistenceService.persistRequest(any(Request.class))).thenReturn(requestEntity);
        when(persistenceService.persistHistory(
            any(RoleAssignment.class),
            any(Request.class)
        )).thenReturn(historyEntity);
        when(prepareResponseService.prepareCreateRoleResponse(any()))
            .thenReturn(ResponseEntity.status(HttpStatus.FORBIDDEN).body(assignmentRequest));

        //actual method call
        ResponseEntity<Object> response = sut.createRoleAssignment(assignmentRequest);
        AssignmentRequest result = (AssignmentRequest) response.getBody();

        //assert values
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(REJECTED, result.getRequest().getStatus());
        assertEquals(assignmentRequest.getRequest(), result.getRequest());
        verify(parseRequestService, times(1))
            .parseRequest(any(AssignmentRequest.class), any(RequestType.class));
        verify(persistenceService, times(1))
            .persistRequest(any(Request.class));
        verify(persistenceService, times(1))
            .getAssignmentsByProcess(anyString(), anyString(), anyString());
        verify(prepareResponseService, times(1))
            .prepareCreateRoleResponse(any(AssignmentRequest.class));
    }

    @Test
    void createRoleAssignment_ReplaceTrue_For_DeleteApproved() throws Exception {
        prepareRequestWhenReplaceExistingTrue();
        requestEntity = TestDataBuilder.buildRequestEntity(assignmentRequest.getRequest());
        historyEntity = TestDataBuilder.buildHistoryIntoEntity(
            TestDataBuilder.buildRoleAssignment(Status.APPROVED), requestEntity);

        when(persistenceService.getAssignmentsByProcess(anyString(), anyString(), anyString()))
            .thenReturn((List<RoleAssignment>) TestDataBuilder.buildRequestedRoleCollection_Updated(Status.APPROVED));

        when(parseRequestService.parseRequest(any(AssignmentRequest.class), any(RequestType.class)))
            .thenReturn(
                assignmentRequest);
        when(persistenceService.persistRequest(any(Request.class))).thenReturn(requestEntity);
        when(persistenceService.persistHistory(
            any(RoleAssignment.class),
            any(Request.class)
        )).thenReturn(historyEntity);

        when(prepareResponseService.prepareCreateRoleResponse(any()))
            .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(assignmentRequest));

        //actual method call
        ResponseEntity<Object> response = sut.createRoleAssignment(assignmentRequest);
        AssignmentRequest result = (AssignmentRequest) response.getBody();

        //assert values
        assertEquals(assignmentRequest, result);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        verify(parseRequestService, times(1))
            .parseRequest(any(AssignmentRequest.class), any(RequestType.class));
        verify(persistenceService, times(1))
            .persistRequest(any(Request.class));
        verify(persistenceService, times(10))
            .persistHistory(any(RoleAssignment.class), any(Request.class));
        verify(prepareResponseService, times(1))
            .prepareCreateRoleResponse(any(AssignmentRequest.class));
    }

    @Test
    void createRoleAssignment_ReplaceTrue_For_DeleteRejected() throws Exception {
        assignmentRequest = TestDataBuilder.buildAssignmentRequest(Status.CREATED, Status.LIVE,
                                                                   false
        );
        assignmentRequest.getRequest().setReplaceExisting(true);
        requestEntity = TestDataBuilder.buildRequestEntity(assignmentRequest.getRequest());
        historyEntity = TestDataBuilder.buildHistoryIntoEntity(
            TestDataBuilder.buildRoleAssignment(Status.LIVE), requestEntity);

        when(persistenceService.getAssignmentsByProcess(anyString(), anyString(), anyString()))
            .thenReturn((List<RoleAssignment>) TestDataBuilder.buildRequestedRoleCollection_Updated(Status.LIVE));

        when(parseRequestService.parseRequest(any(AssignmentRequest.class), any(RequestType.class)))
            .thenReturn(
                assignmentRequest);
        when(persistenceService.persistRequest(any(Request.class))).thenReturn(requestEntity);
        when(persistenceService.persistHistory(
            any(RoleAssignment.class),
            any(Request.class)
        )).thenReturn(historyEntity);

        when(prepareResponseService.prepareCreateRoleResponse(any()))
            .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(assignmentRequest));

        //actual method call
        ResponseEntity<Object> response = sut.createRoleAssignment(assignmentRequest);
        AssignmentRequest result = (AssignmentRequest) response.getBody();

        //assert values
        assertEquals(assignmentRequest, result);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verifyNUmberOfInvocationsForRejectedRequest();
        verify(validationModelService, times(1)).validateRequest(any(AssignmentRequest.class));
    }

    @Test
    void createRoleAssignment_ReplaceTrue_When_NeedToCreateEmpty() throws Exception {
        prepareRequestWhenReplaceExistingTrue();
        assignmentRequest.setRequestedRoles(Collections.emptyList());
        requestEntity = TestDataBuilder.buildRequestEntity(assignmentRequest.getRequest());
        historyEntity = TestDataBuilder.buildHistoryIntoEntity(
            TestDataBuilder.buildRoleAssignment(Status.APPROVED), requestEntity);

        when(persistenceService.getAssignmentsByProcess(anyString(), anyString(), anyString()))
            .thenReturn((List<RoleAssignment>) TestDataBuilder.buildRequestedRoleCollection_Updated(Status.APPROVED));

        when(parseRequestService.parseRequest(any(AssignmentRequest.class), any(RequestType.class)))
            .thenReturn(
                assignmentRequest);
        when(persistenceService.persistRequest(any(Request.class))).thenReturn(requestEntity);
        when(persistenceService.persistHistory(
            any(RoleAssignment.class),
            any(Request.class)
        )).thenReturn(historyEntity);

        when(prepareResponseService.prepareCreateRoleResponse(any()))
            .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(assignmentRequest));

        //actual method call
        ResponseEntity<Object> response = sut.createRoleAssignment(assignmentRequest);
        AssignmentRequest result = (AssignmentRequest) response.getBody();


        //assert values
        assertEquals(assignmentRequest, result);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(parseRequestService, times(1))
            .parseRequest(any(AssignmentRequest.class), any(RequestType.class));
        verify(persistenceService, times(1))
            .persistRequest(any(Request.class));
        verify(persistenceService, times(4))
            .persistHistory(any(RoleAssignment.class), any(Request.class));
        verify(prepareResponseService, times(1))
            .prepareCreateRoleResponse(any(AssignmentRequest.class));
    }


    @Test
    void createRoleAssignment_ReplaceTrue_When_DuplicateRequest() throws Exception {
        assignmentRequest = TestDataBuilder.buildAssignmentRequest(Status.CREATED, Status.APPROVED,
                                                                   false
        );
        for (RoleAssignment roleAssignment : assignmentRequest.getRequestedRoles()) {
            roleAssignment.setId(UUID.randomUUID());
        }
        assignmentRequest.getRequest().setReplaceExisting(true);
        requestEntity = TestDataBuilder.buildRequestEntity(assignmentRequest.getRequest());
        historyEntity = TestDataBuilder.buildHistoryIntoEntity(
            TestDataBuilder.buildRoleAssignment(Status.APPROVED), requestEntity);

        when(persistenceService.getAssignmentsByProcess(anyString(), anyString(), anyString()))
            .thenReturn((List<RoleAssignment>) assignmentRequest.getRequestedRoles());

        when(parseRequestService.parseRequest(any(AssignmentRequest.class), any(RequestType.class)))
            .thenReturn(
                assignmentRequest);
        when(persistenceService.persistRequest(any(Request.class))).thenReturn(requestEntity);
        when(persistenceService.persistHistory(
            any(RoleAssignment.class),
            any(Request.class)
        )).thenReturn(historyEntity);

        when(prepareResponseService.prepareCreateRoleResponse(any()))
            .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(assignmentRequest));

        //actual method call
        ResponseEntity<Object> response = sut.createRoleAssignment(assignmentRequest);
        AssignmentRequest result = (AssignmentRequest) response.getBody();


        //assert values
        assertEquals(assignmentRequest, result);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(parseRequestService, times(1))
            .parseRequest(any(AssignmentRequest.class), any(RequestType.class));
        verify(persistenceService, times(1))
            .persistRequest(any(Request.class));
        verify(prepareResponseService, times(2))
            .prepareCreateRoleResponse(any(AssignmentRequest.class));
    }

    @Test
    void shouldReturn201WhenExistingAndIncomingRolesEmpty() throws IOException, ParseException {

        prepareRequestWhenReplaceExistingTrue();
        assignmentRequest.setRequestedRoles(Collections.emptyList());

        requestEntity = TestDataBuilder.buildRequestEntity(assignmentRequest.getRequest());

        when(persistenceService.getAssignmentsByProcess(anyString(), anyString(), anyString()))
            .thenReturn((List<RoleAssignment>) assignmentRequest.getRequestedRoles());

        when(parseRequestService.parseRequest(any(AssignmentRequest.class), any(RequestType.class)))
            .thenReturn(
                assignmentRequest);
        when(persistenceService.persistRequest(any(Request.class))).thenReturn(requestEntity);

        //actual method call
        ResponseEntity<Object> response = sut.createRoleAssignment(assignmentRequest);
        AssignmentRequest assignmentRequest = (AssignmentRequest) response.getBody();

        //assert values
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        assertEquals(APPROVED, assignmentRequest.getRequest().getStatus());
        assertEquals("Request has been approved", assignmentRequest.getRequest().getLog());

        assertEquals(APPROVED.toString(), requestEntity.getStatus());
        assertEquals("Request has been approved", requestEntity.getLog());

        verify(parseRequestService, times(1))
            .parseRequest(any(AssignmentRequest.class), any(RequestType.class));
        verify(persistenceService, times(1))
            .persistRequest(any(Request.class));
        verify(persistenceService, times(1))
            .updateRequest(any(RequestEntity.class));

    }

    private void verifyNUmberOfInvocations() throws ParseException {
        verify(parseRequestService, times(1))
            .parseRequest(any(AssignmentRequest.class), any(RequestType.class));
        verify(persistenceService, times(1))
            .persistRequest(any(Request.class));
        verify(persistenceService, times(6))
            .persistHistory(any(RoleAssignment.class), any(Request.class));
        verify(prepareResponseService, times(1))
            .prepareCreateRoleResponse(any(AssignmentRequest.class));
    }

    private void verifyNUmberOfInvocationsForRejectedRequest() throws ParseException {
        verify(parseRequestService, times(1))
            .parseRequest(any(AssignmentRequest.class), any(RequestType.class));
        verify(persistenceService, times(1))
            .persistRequest(any(Request.class));
        verify(persistenceService, times(4))
            .persistHistory(any(RoleAssignment.class), any(Request.class));
        verify(prepareResponseService, times(1))
            .prepareCreateRoleResponse(any(AssignmentRequest.class));
    }

    private void prepareRequestWhenReplaceExistingTrue() throws IOException {
        assignmentRequest = TestDataBuilder.buildAssignmentRequest(Status.CREATED, Status.APPROVED,
                                                                   true
        );
        //assignmentRequest.getRequest().setReplaceExisting(true);
    }


}
