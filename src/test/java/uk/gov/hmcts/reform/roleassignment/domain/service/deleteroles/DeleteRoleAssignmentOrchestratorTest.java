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
import uk.gov.hmcts.reform.roleassignment.util.PersistenceUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.APPROVED;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.CREATED;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.DELETED;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.DELETE_APPROVED;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.DELETE_REJECTED;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.REJECTED;

@RunWith(MockitoJUnitRunner.class)
class DeleteRoleAssignmentOrchestratorTest {

    @Mock
    private ParseRequestService parseRequestService = mock(ParseRequestService.class);
    @Mock
    private PersistenceService persistenceService = mock(PersistenceService.class);
    @Mock
    private ValidationModelService validationModelService = mock(ValidationModelService.class);
    @Mock
    private PersistenceUtil persistenceUtil;

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
        validationModelService,
        persistenceUtil
    );

    @BeforeEach
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        assignmentRequest = TestDataBuilder.buildAssignmentRequest(CREATED, Status.LIVE, false);
        requestEntity = TestDataBuilder.buildRequestEntity(assignmentRequest.getRequest());
        roleAssignment = TestDataBuilder.buildRoleAssignment(Status.LIVE);
        historyEntity = TestDataBuilder.buildHistoryIntoEntity(
            assignmentRequest.getRequestedRoles().iterator().next(), requestEntity);
    }

    @Test
    @DisplayName("should get 204 when process and reference doesn't exist")
    void shouldThrowResourceNotFoundWhenProcessNotExist() throws Exception {
        mockRequest();
        ResponseEntity response = sut.deleteRoleAssignmentByProcessAndReference(PROCESS, REFERENCE);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(persistenceService,times(1)).updateRequest(any(RequestEntity.class));
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
        )).thenReturn(Collections.emptyList());
        mockHistoryEntity();

        ResponseEntity<Void> response = sut.deleteRoleAssignmentByProcessAndReference(PROCESS,
                                                                                                     REFERENCE);
        assertEquals(APPROVED.toString(), sut.getRequestEntity().getStatus());
        assertEquals(sut.getRequest().getId(), sut.getRequestEntity().getId());
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

    }

    @Test
    @DisplayName("should delete records from role_assignment table for a valid Assignment Id")
    void shouldDeleteRecordsFromRoleAssignment() throws Exception {

        //Set the status approved of all requested role manually for drool validation process
        String assignmentId = UUID.randomUUID().toString();
        setApprovedStatusByDrool();
        mockRequest();
        when(persistenceService.getAssignmentById(UUID.fromString(assignmentId)))
            .thenReturn(Collections.emptyList());
        mockHistoryEntity();
        ResponseEntity<?> response = sut.deleteRoleAssignmentByAssignmentId(assignmentId);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(persistenceService, times(1)).getAssignmentById(UUID.fromString(assignmentId));

    }

    @Test
    @DisplayName("should delete any delete approved records that are present")
    void shouldDeleteRecordsForApprovedItems() throws Exception {
        mockRequest();
        when(persistenceUtil.prepareHistoryEntityForPersistance(any(), any())).thenReturn(historyEntity);
        assignmentRequest.getRequestedRoles().forEach(roleAssignment1 -> {
            roleAssignment1.setStatus(DELETE_APPROVED);
        });
        mockHistoryEntity();

        //set history entity into request entity
        Set<HistoryEntity> historyEntities = new HashSet<>();
        historyEntities.add(historyEntity);
        requestEntity.setHistoryEntities(historyEntities);
        sut.setRequestEntity(requestEntity);
        sut.checkAllDeleteApproved(assignmentRequest, assignmentRequest.getRequest().getAssignerId());
        verify(persistenceService, times(2)).deleteRoleAssignmentByActorId(any());
        verify(persistenceService, times(1)).persistActorCache(any());
        verify(persistenceService, times(2)).updateRequest(any(RequestEntity.class));
    }

    @Test
    @DisplayName("should delete only delete approved records and remove other records")
    void shouldDeleteRecordsOnlyForApprovedItems() throws Exception {
        mockRequest();
        when(persistenceUtil.prepareHistoryEntityForPersistance(any(), any())).thenReturn(historyEntity);
        //Set 1 of 2 to delete approved status
        assignmentRequest.getRequestedRoles().iterator().next().setStatus(DELETE_APPROVED);

        mockHistoryEntity();

        //set history entity into request entity
        Set<HistoryEntity> historyEntities = new HashSet<>();
        historyEntities.add(historyEntity);
        requestEntity.setHistoryEntities(historyEntities);
        sut.setRequestEntity(requestEntity);
        sut.checkAllDeleteApproved(assignmentRequest, assignmentRequest.getRequest().getAssignerId());
        assertEquals(1, assignmentRequest.getRequestedRoles().size());
        assertEquals(REJECTED.toString(),sut.getRequestEntity().getStatus());
        assertEquals(assignmentRequest.getRequest().getLog(),sut.getRequestEntity().getLog());

    }

    @Test
    @DisplayName("should delete any delete approved records that are present even with no actorID passed")
    void shouldDeleteRecordsForApprovedItemsNoActorID() throws Exception {
        mockRequest();
        when(persistenceUtil.prepareHistoryEntityForPersistance(any(), any())).thenReturn(historyEntity);
        assignmentRequest.getRequestedRoles().forEach(roleAssignment1 -> {
            roleAssignment1.setStatus(DELETE_APPROVED);
        });
        mockHistoryEntity();

        //set history entity into request entity
        Set<HistoryEntity> historyEntities = new HashSet<>();
        historyEntities.add(historyEntity);
        requestEntity.setHistoryEntities(historyEntities);
        sut.setRequestEntity(requestEntity);
        sut.checkAllDeleteApproved(assignmentRequest, "");
        verify(persistenceService, times(2)).deleteRoleAssignment(any());
        verify(persistenceService, times(1)).persistActorCache(any());
        verify(persistenceService, times(2)).updateRequest(any(RequestEntity.class));
    }

    @Test
    @DisplayName("should throw 204 exception for a non existing Assignment id")
    void shouldThrowNotFoundForAssignmentId() throws Exception {
        String assignmentId = UUID.randomUUID().toString();
        setApprovedStatusByDrool();
        mockRequest();
        when(persistenceService.getAssignmentById(UUID.fromString(assignmentId))).thenReturn(Collections.emptyList());
        mockHistoryEntity();

        ResponseEntity<?> response = sut.deleteRoleAssignmentByAssignmentId(assignmentId);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(persistenceService, times(1)).updateRequest(any(RequestEntity.class));
    }

    @Test
    @DisplayName("should not delete any records if delete approved records are zero")
    void shouldNotDeleteRecordsForZeroApprovedItems() throws Exception {
        mockRequest();
        when(persistenceUtil.prepareHistoryEntityForPersistance(any(), any())).thenReturn(historyEntity);
        sut.setRequestEntity(new RequestEntity());
        sut.checkAllDeleteApproved(new AssignmentRequest(new Request(), Collections.emptyList()), "actorId");
        verify(persistenceService, times(0)).deleteRoleAssignmentByActorId(any());
        verify(persistenceService, times(0)).persistActorCache(any());
        verify(persistenceService, times(2)).updateRequest(any(RequestEntity.class));
    }

    @Test
    @DisplayName("should not delete any records if delete approved records don't match requested items")
    void shouldNotDeleteRecordsForNonMatchingRequestItems() throws Exception {
        mockRequest();
        RoleAssignment roleAssignment = RoleAssignment.builder().status(DELETE_APPROVED).build();
        when(persistenceUtil.prepareHistoryEntityForPersistance(any(), any())).thenReturn(historyEntity);

        sut.setRequestEntity(RequestEntity.builder().historyEntities(new HashSet<>()).build());
        sut.checkAllDeleteApproved(new AssignmentRequest(
            new Request(),
            new ArrayList<>() {
                {
                    add(roleAssignment);
                    add(RoleAssignment.builder().status(DELETE_REJECTED).build());
                    add(RoleAssignment.builder().status(DELETED).build());

                }
            }
        ), "actorId");
        assertEquals(DELETE_REJECTED,roleAssignment.getStatus());
        verify(persistenceService, times(0)).deleteRoleAssignmentByActorId(any());
        verify(persistenceService, times(0)).persistActorCache(any());
        verify(persistenceService, times(2)).updateRequest(any(RequestEntity.class));

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
        verify(validationModelService,times(1)).validateRequest(any(AssignmentRequest.class));
        verify(persistenceService,times(3)).updateRequest(any(RequestEntity.class));
        verify(persistenceService,times(2)).persistHistoryEntities(any());

    }

    @Test
    @DisplayName("should throw 400 when reference doesn't exist")
    void shouldThrowBadRequestWhenReferenceNotExist() throws Exception {
        mockRequest();
        Assertions.assertThrows(BadRequestException.class, () ->
            sut.deleteRoleAssignmentByProcessAndReference(PROCESS, null)
        );
    }

    @Test
    @DisplayName("should throw 400 when reference blank")
    void shouldThrowBadRequestWhenReferenceBlank() throws Exception {
        mockRequest();
        Assertions.assertThrows(BadRequestException.class, () ->
            sut.deleteRoleAssignmentByProcessAndReference(PROCESS, " ")
        );
    }

    private void assertion() throws Exception {
        verify(parseRequestService, times(1)).prepareDeleteRequest(any(), any(), any(), any());
        verify(persistenceService, times(1)).persistRequest(any(Request.class));
        verify(persistenceUtil, times(4))
            .prepareHistoryEntityForPersistance(any(RoleAssignment.class), any(Request.class));
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
        when(persistenceUtil.prepareHistoryEntityForPersistance(
            roleAssignment,
            assignmentRequest.getRequest()
        )).thenReturn(historyEntity);
    }

}
