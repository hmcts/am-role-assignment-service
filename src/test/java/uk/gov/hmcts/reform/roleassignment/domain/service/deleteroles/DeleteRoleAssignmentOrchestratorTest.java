package uk.gov.hmcts.reform.roleassignment.domain.service.deleteroles;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.HistoryEntity;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.RequestEntity;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.Request;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ParseRequestService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ValidationModelService;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.DELETE_APPROVED;

@RunWith(MockitoJUnitRunner.class)
class DeleteRoleAssignmentOrchestratorTest {

    @Mock
    private ParseRequestService parseRequestService = mock(ParseRequestService.class);
    @Mock
    private PersistenceService persistenceService = mock(PersistenceService.class);
    @Mock
    private ValidationModelService validationModelService = mock(ValidationModelService.class);

    private static final String ACTOR_ID = "21334a2b-79ce-44eb-9168-2d49a744be9c";
    private static final String PROCESS = "process";
    private static final String REFERENCE = "reference";
    AssignmentRequest assignmentRequest;
    RequestEntity requestEntity;
    RoleAssignment roleAssignment;
    HistoryEntity historyEntity;


    @InjectMocks
    private DeleteRoleAssignmentOrchestrator sut = new DeleteRoleAssignmentOrchestrator(
        persistenceService,
        parseRequestService,
        validationModelService
    );

    @BeforeEach
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        assignmentRequest = TestDataBuilder.buildAssignmentRequest();
        requestEntity = TestDataBuilder.buildRequestEntity(assignmentRequest.getRequest());
        roleAssignment = TestDataBuilder.buildRequestedRole();
        historyEntity = TestDataBuilder.buildHistoryIntoEntity(
            assignmentRequest.getRequestedRoles().iterator().next(), requestEntity);
    }

    @Test
    @DisplayName("should get 204 when role assignment records delete  successful")
    public void shouldDeleteRoleAssignmentByActorId() throws Exception {

        //Set the status approved of all requested role manually for drool validation process
        setApprovedStatusByDrool();
        mockRequest(null, null, ACTOR_ID, null);
        when(persistenceService.getAssignmentsByActor(UUID.fromString(ACTOR_ID)))
            .thenReturn((List<RoleAssignment>) assignmentRequest.getRequestedRoles());
        mockHistoryEntity();
        ResponseEntity response = sut.deleteRoleAssignment(ACTOR_ID, null, null, null);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(persistenceService, times(2)).deleteRoleAssignmentByActorId(UUID.fromString(ACTOR_ID));
        verify(persistenceService, times(2)).persistActorCache(any());
        assertion();

    }


    @Test
    @DisplayName("should throw 404 when process and reference doesn't exist")
    public void shouldThrowResourceNotFoundWhenProcessNotExist() throws Exception {
        mockRequest(PROCESS, REFERENCE, null, null);
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            sut.deleteRoleAssignment(null, PROCESS, REFERENCE, null);
        });
    }

    @Test
    @DisplayName("should throw 404 when actorId doesn't exist")
    public void shouldThrowResourceNotFoundWhenActorIdNotExist() throws Exception {
        mockRequest(null, null, ACTOR_ID, null);
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            sut.deleteRoleAssignment(ACTOR_ID, null, null, null);
        });
    }


    @Test
    @DisplayName("should get 204 when role assignment records delete  successful")
    public void shouldDeleteRoleAssignmentByProcess() throws Exception {

        //Set the status approved of all requested role manually for drool validation process
        setApprovedStatusByDrool();
        mockRequest(PROCESS, REFERENCE, null, null);
        when(persistenceService.getAssignmentsByProcess(
            PROCESS,
            REFERENCE,
            Status.LIVE.toString())).thenReturn((List<RoleAssignment>) assignmentRequest.getRequestedRoles());
        mockHistoryEntity();

        ResponseEntity response = sut.deleteRoleAssignment(null, PROCESS, REFERENCE, null);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(persistenceService, times(2)).deleteRoleAssignment(any());
        verify(persistenceService, times(2)).persistActorCache(any());
        assertion();

    }


    /*@Test
    @DisplayName("should get 409 Conflict when request is rejected by drool rules")
    public void shouldReturn409WhenDroolValidationRejectRequest() throws Exception {
        historyEntity.setStatus(DELETE_REJECTED.toString());
        mockRequest(null, null, ACTOR_ID);
        when(persistenceService.getAssignmentsByActor(UUID.fromString(ACTOR_ID)))
            .thenReturn((List<RoleAssignment>) assignmentRequest.getRequestedRoles());
        mockHistoryEntity();
        ResponseEntity response = sut.deleteRoleAssignment(ACTOR_ID, null, null, null);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertion();
    }*/

    @Test
    @DisplayName("should delete records from role_assignment table for a valid Assignment Id")
    public void shouldDeleteRecordsFromRoleAssignment() throws Exception {
    }

    @Test
    @DisplayName("should throw 400 exception for a syntactically bad Assignment id")
    public void shouldThrowBadRequestForMalformedAssignmentId() throws Exception {
    }

    @Test
    @DisplayName("should throw 404 exception for a non existing Assignment id")
    public void shouldThrowNotFoundForAssignmentId() throws Exception {
    }

    @Test
    @DisplayName("should not delete any records if delete approved records are zero")
    public void shouldNotDeleteRecordsForZeroApprovedItems() throws Exception {
    }

    @Test
    @DisplayName("should not delete any records if delete approved records don't match requested items")
    public void shouldNotDeleteRecordsForNonMatchingRequestItems() throws Exception {
    }

    @Test
    @DisplayName("should throw Conflict 409 if any record is rejected for deletion")
    public void shouldThrowConflictIfRecordIsRejected() throws Exception {
    }

    @Test
    @DisplayName("should throw 400 when  reference doesn't exist")
    public void shouldThrowBadRequestWhenReferenceNotExist() throws Exception {
        mockRequest(PROCESS, REFERENCE, null, null);
        Assertions.assertThrows(BadRequestException.class, () -> {
            sut.deleteRoleAssignment(null, PROCESS, null, null);
        });
    }

    private void assertion() throws Exception {
        verify(parseRequestService, times(1)).prepareDeleteRequest(any(), any(), any(), any());
        verify(persistenceService, times(1)).persistRequest(any(Request.class));
        verify(persistenceService, times(4)).persistHistory(any(RoleAssignment.class), any(Request.class));
    }

    private void mockRequest(String process, String reference, String actorId, String assignmentId) throws Exception {
        when(parseRequestService.prepareDeleteRequest(any(), any(), any(), any())).thenReturn(
            assignmentRequest.getRequest());
        when(persistenceService.persistRequest(any())).thenReturn(requestEntity);
    }

    private void setApprovedStatusByDrool() {
        for (RoleAssignment requestedRole : assignmentRequest.getRequestedRoles()) {
            requestedRole.status = Status.APPROVED;

        }
        historyEntity.setStatus(DELETE_APPROVED.toString());
    }

    private void mockHistoryEntity() throws Exception {
        doNothing().when(validationModelService).validateRequest(assignmentRequest);
        when(persistenceService.persistHistory(
            roleAssignment,
            assignmentRequest.getRequest()
                                              )).thenReturn(historyEntity);
    }


}
