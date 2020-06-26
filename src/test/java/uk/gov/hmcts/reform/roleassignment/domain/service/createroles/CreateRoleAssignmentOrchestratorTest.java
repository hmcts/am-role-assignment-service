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
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.HistoryEntity;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.RequestEntity;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.Request;
import uk.gov.hmcts.reform.roleassignment.domain.model.RequestedRole;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RequestType;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ParseRequestService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ValidationModelService;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;
import uk.gov.hmcts.reform.roleassignment.util.PersistenceUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @InjectMocks
    private CreateRoleAssignmentOrchestrator sut = new CreateRoleAssignmentOrchestrator(parseRequestService,
                                                                                        persistenceService,
                                                                                        validationModelService,
                                                                                        persistenceUtil
    );

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void createRoleAssignment_ReplaceTrue_RejectRoleRequests() throws Exception {
        AssignmentRequest assignmentRequest = TestDataBuilder.buildAssignmentRequest();
        assignmentRequest.getRequest().setReplaceExisting(true);
        RequestEntity requestEntity = TestDataBuilder.buildRequestEntity(assignmentRequest.getRequest());
        HistoryEntity historyEntity = TestDataBuilder.buildHistoryIntoEntity(
            assignmentRequest.getRequestedRoles().iterator().next(), requestEntity);

        when(parseRequestService.parseRequest(any(AssignmentRequest.class), any(RequestType.class))).thenReturn(
            assignmentRequest);
        when(persistenceService.persistRequest(any(Request.class))).thenReturn(requestEntity);
        when(persistenceService.persistHistory(any(RequestedRole.class), any(Request.class))).thenReturn(historyEntity);

        ResponseEntity<Object> response = sut.createRoleAssignment(assignmentRequest);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(parseRequestService, times(1)).parseRequest(any(AssignmentRequest.class), any(RequestType.class));
        verify(persistenceService, times(1)).persistRequest(any(Request.class));
        verify(persistenceService, times(6)).persistHistory(any(RequestedRole.class), any(Request.class));
    }

    @Test
    void createRoleAssignment_ReplaceFalse_RejectRoleRequests() throws Exception {
        AssignmentRequest assignmentRequest = TestDataBuilder.buildAssignmentRequest();
        RequestEntity requestEntity = TestDataBuilder.buildRequestEntity(assignmentRequest.getRequest());
        HistoryEntity historyEntity = TestDataBuilder.buildHistoryIntoEntity(
            assignmentRequest.getRequestedRoles().iterator().next(), requestEntity);

        when(parseRequestService.parseRequest(any(AssignmentRequest.class), any(RequestType.class))).thenReturn(
            assignmentRequest);
        when(persistenceService.persistRequest(any(Request.class))).thenReturn(requestEntity);
        when(persistenceService.persistHistory(any(RequestedRole.class), any(Request.class))).thenReturn(historyEntity);

        ResponseEntity<Object> response = sut.createRoleAssignment(assignmentRequest);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        //TODO assert RejectedRoles assertEquals(response.getBody());
        //parsedAssignmentRequest.getRequestedRoles().stream().filter(role -> role.getStatus().equals(
        //            Status.APPROVED)).collect(
        //            Collectors.toList());

        //ObjectMapper objectMapper = new ObjectMapper();
        //AssignmentRequest assRequest = objectMapper.readValue(response.getBody().toString(), AssignmentRequest.class);

        verify(parseRequestService, times(1)).parseRequest(any(AssignmentRequest.class), any(RequestType.class));
        verify(persistenceService, times(1)).persistRequest(any(Request.class));
        verify(persistenceService, times(6)).persistHistory(any(RequestedRole.class), any(Request.class));
    }

    //@Test
    void createRoleAssignment_ReplaceTrue_ApproveRoleRequests() throws Exception {
        AssignmentRequest assignmentRequest = TestDataBuilder.buildAssignmentRequest();
        assignmentRequest.getRequest().setReplaceExisting(true);
        RequestEntity requestEntity = TestDataBuilder.buildRequestEntity(assignmentRequest.getRequest());
        HistoryEntity historyEntity = TestDataBuilder.buildHistoryIntoEntity(
            assignmentRequest.getRequestedRoles().iterator().next(), requestEntity);
        when(parseRequestService.parseRequest(any(AssignmentRequest.class), any(RequestType.class))).thenReturn(
            assignmentRequest);
        when(persistenceService.persistRequest(any(Request.class))).thenReturn(requestEntity);
        when(persistenceService.persistHistory(any(RequestedRole.class), any(Request.class))).thenReturn(historyEntity);

        ResponseEntity<Object> response = sut.createRoleAssignment(assignmentRequest);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(parseRequestService, times(1)).parseRequest(any(AssignmentRequest.class), any(RequestType.class));
        verify(persistenceService, times(1)).persistRequest(any(Request.class));
        verify(persistenceService, times(6)).persistHistory(any(RequestedRole.class), any(Request.class));
    }

    //@Test
    void createRoleAssignment_ReplaceFalse_ApproveRoleRequests() throws Exception {
        AssignmentRequest assignmentRequest = TestDataBuilder.buildAssignmentRequest();
        RequestEntity requestEntity = TestDataBuilder.buildRequestEntity(assignmentRequest.getRequest());
        HistoryEntity historyEntity = TestDataBuilder.buildHistoryIntoEntity(
            assignmentRequest.getRequestedRoles().iterator().next(), requestEntity);

        when(parseRequestService.parseRequest(any(AssignmentRequest.class), any(RequestType.class))).thenReturn(
            assignmentRequest);
        when(persistenceService.persistRequest(any(Request.class))).thenReturn(requestEntity);
        when(persistenceService.persistHistory(any(RequestedRole.class), any(Request.class))).thenReturn(historyEntity);

        //doCallRealMethod().when(validationModelService).validateRequest(any(AssignmentRequest.class));

        ResponseEntity<Object> response = sut.createRoleAssignment(assignmentRequest);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(parseRequestService, times(1)).parseRequest(any(AssignmentRequest.class), any(RequestType.class));
        verify(persistenceService, times(1)).persistRequest(any(Request.class));
        verify(persistenceService, times(6)).persistHistory(any(RequestedRole.class), any(Request.class));
    }
}
