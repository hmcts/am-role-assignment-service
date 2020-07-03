package uk.gov.hmcts.reform.roleassignment.domain.service.deleteroles;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.HistoryEntity;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.RequestEntity;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ParseRequestService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ValidationModelService;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
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


    @InjectMocks
    private DeleteRoleAssignmentOrchestrator sut = new DeleteRoleAssignmentOrchestrator(
        persistenceService,
        parseRequestService,
        validationModelService
    );

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @DisplayName("should get 204 when role assignment records delete  successful")
    public void shouldDeleteRoleAssignmentByActorId() throws Exception {
        AssignmentRequest assignmentRequest = TestDataBuilder.buildAssignmentRequest();
        RequestEntity requestEntity = TestDataBuilder.buildRequestEntity(assignmentRequest.getRequest());
        RoleAssignment roleAssignment = TestDataBuilder.buildRequestedRole();
        roleAssignment.setStatus(DELETE_APPROVED);
        roleAssignment.setStatusSequence(Status.DELETE_APPROVED.sequence);
        //Set the status approved of all requested role manually for drool validation process
        for (RoleAssignment requestedRole : assignmentRequest.getRequestedRoles()) {
            requestedRole.status = Status.APPROVED;

        }

        HistoryEntity historyEntity = TestDataBuilder.buildHistoryIntoEntity(
            assignmentRequest.getRequestedRoles().iterator().next(), requestEntity);
        historyEntity.setStatus(DELETE_APPROVED.toString());


        when(parseRequestService.prepareDeleteRequest(null, null, ACTOR_ID)).thenReturn(
            assignmentRequest.getRequest());
        when(persistenceService.persistRequest(assignmentRequest.getRequest())).thenReturn(requestEntity);
        when(persistenceService.getAssignmentsByActor(UUID.fromString(ACTOR_ID))).thenReturn((List<RoleAssignment>) assignmentRequest.getRequestedRoles());
        doNothing().when(validationModelService).validateRequest(assignmentRequest);

        when(persistenceService.persistHistory(roleAssignment, assignmentRequest.getRequest())).thenReturn(historyEntity);



        ResponseEntity response = sut.deleteRoleAssignment(ACTOR_ID, null, null);

    }

}
