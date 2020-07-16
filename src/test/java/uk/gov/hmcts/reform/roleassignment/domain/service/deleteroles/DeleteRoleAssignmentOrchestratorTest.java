package uk.gov.hmcts.reform.roleassignment.domain.service.deleteroles;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.DELETED;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.DELETE_APPROVED;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.DELETE_REJECTED;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

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
import uk.gov.hmcts.reform.roleassignment.data.HistoryEntity;
import uk.gov.hmcts.reform.roleassignment.data.RequestEntity;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.Request;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ParseRequestService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ValidationModelService;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;
import uk.gov.hmcts.reform.roleassignment.domain.service.deleteroles.DeleteRoleAssignmentOrchestrator;

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
    @DisplayName("should get 204 when process and reference doesn't exist")
    void shouldThrowResourceNotFoundWhenProcessNotExist() throws Exception {
        mockRequest();
        ResponseEntity response = sut.deleteRoleAssignmentByProcessAndReference(PROCESS, REFERENCE);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    @DisplayName("should get 204 when role assignment records delete  successful")
    void shouldDeleteRoleAssignmentByProcess() throws Exception {

        //Set the status approved of all requested role manually for drool validation process
        setApprovedStatusByDrool();
        mockRequest();
        when(persistenceService.getAssignmentsByProcess(
            PROCESS,
            REFERENCE,
            Status.LIVE.toString()
        )).thenReturn((List<RoleAssignment>) assignmentRequest.getRequestedRoles());
        mockHistoryEntity();

        ResponseEntity<Object> response = sut.deleteRoleAssignmentByProcessAndReference(PROCESS, REFERENCE);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(persistenceService, times(2)).deleteRoleAssignment(any());
        verify(persistenceService, times(2)).persistActorCache(any());
        assertion();

    }

    @Test
    @DisplayName("should delete records from role_assignment table for a valid Assignment Id")
    void shouldDeleteRecordsFromRoleAssignment() throws Exception {

        //Set the status approved of all requested role manually for drool validation process
        String assignmentId = UUID.randomUUID().toString();
        setApprovedStatusByDrool();
        mockRequest();
        when(persistenceService.getAssignmentById(UUID.fromString(assignmentId)))
            .thenReturn((List<RoleAssignment>) assignmentRequest.getRequestedRoles());
        mockHistoryEntity();
        ResponseEntity<Object> response = sut.deleteRoleAssignmentAssignmentId(assignmentId);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(persistenceService, times(1)).getAssignmentById(UUID.fromString(assignmentId));
        assertion();
    }

    @Test
    @DisplayName("should throw 204 exception for a non existing Assignment id")
    void shouldThrowNotFoundForAssignmentId() throws Exception {
        String assignmentId = UUID.randomUUID().toString();
        setApprovedStatusByDrool();
        mockRequest();
        when(persistenceService.getAssignmentById(UUID.fromString(assignmentId))).thenReturn(Collections.emptyList());
        mockHistoryEntity();

        ResponseEntity<Object> response = sut.deleteRoleAssignmentAssignmentId(assignmentId);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    @DisplayName("should not delete any records if delete approved records are zero")
    void shouldNotDeleteRecordsForZeroApprovedItems() throws Exception {
        mockRequest();
        when(persistenceService.persistHistory(any(), any())).thenReturn(historyEntity);
        sut.requestEntity = new RequestEntity();
        sut.checkAllDeleteApproved(new AssignmentRequest(new Request(), Collections.emptyList()), "actorId");
        verify(persistenceService, times(0)).deleteRoleAssignmentByActorId(any());
        verify(persistenceService, times(0)).persistActorCache(any());
    }

    @Test
    @DisplayName("should not delete any records if delete approved records don't match requested items")
    void shouldNotDeleteRecordsForNonMatchingRequestItems() throws Exception {
        mockRequest();
        when(persistenceService.persistHistory(any(), any())).thenReturn(historyEntity);

        RequestEntity requestEntity = RequestEntity.builder().historyEntities(new HashSet<>()).build();
        sut.requestEntity = requestEntity;
        sut.checkAllDeleteApproved(new AssignmentRequest(
            new Request(),
            new ArrayList<>() {
                {
                    add(RoleAssignment.builder().status(DELETE_APPROVED).build());
                    add(RoleAssignment.builder().status(DELETE_REJECTED).build());
                    add(RoleAssignment.builder().status(DELETED).build());

                }
            }
        ), "actorId");
        verify(persistenceService, times(0)).deleteRoleAssignmentByActorId(any());
        verify(persistenceService, times(0)).persistActorCache(any());

    }

    @Test
    @DisplayName("should throw Unprocessable Entity 422 if any record is rejected for deletion")
    void shouldThrowUnprocessableIfRecordIsRejected() throws Exception {
        //Set the status approved of all requested role manually for drool validation process
        setApprovedStatusByDrool();
        mockRequest();
        when(persistenceService.getAssignmentsByProcess(PROCESS,
                                                        REFERENCE,
                                                        Status.LIVE.toString()))
            .thenReturn(new ArrayList<>() {
                {
                    add(RoleAssignment.builder().status(DELETE_APPROVED).build());
                    add(RoleAssignment.builder().status(DELETE_REJECTED).build());
                    add(RoleAssignment.builder().status(DELETED).build());
                }
            });
        mockHistoryEntity();
        ResponseEntity response = sut.deleteRoleAssignmentByProcessAndReference(PROCESS, REFERENCE);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
    }

    @Test
    @DisplayName("should throw 400 when reference doesn't exist")
    void shouldThrowBadRequestWhenReferenceNotExist() throws Exception {
        mockRequest();
        Assertions.assertThrows(BadRequestException.class, () -> {
            sut.deleteRoleAssignmentByProcessAndReference(PROCESS, null);
        });
    }

    private void assertion() throws Exception {
        verify(parseRequestService, times(1)).prepareDeleteRequest(any(), any(), any(), any());
        verify(persistenceService, times(1)).persistRequest(any(Request.class));
        verify(persistenceService, times(4)).persistHistory(any(RoleAssignment.class), any(Request.class));
    }

    private void mockRequest() throws Exception {
        when(parseRequestService.prepareDeleteRequest(any(), any(), any(), any())).thenReturn(
            assignmentRequest.getRequest());
        when(persistenceService.persistRequest(any())).thenReturn(requestEntity);
    }

    private void setApprovedStatusByDrool() {
        for (RoleAssignment requestedRole : assignmentRequest.getRequestedRoles()) {
            requestedRole.setStatus(Status.APPROVED);
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
