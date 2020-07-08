package uk.gov.hmcts.reform.roleassignment.controller.endpoints;

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
import uk.gov.hmcts.reform.roleassignment.domain.service.deleteroles.DeleteRoleAssignmentOrchestrator;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.roleassignment.v1.V1.Error.BAD_REQUEST_MISSING_PARAMETERS;

@RunWith(MockitoJUnitRunner.class)
class DeleteAssignmentControllerTest {

    @Mock
    private DeleteRoleAssignmentOrchestrator deleteRoleAssignmentOrchestrator =
        mock(DeleteRoleAssignmentOrchestrator.class);

    @InjectMocks
    private DeleteAssignmentController sut;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    private static final String ACTOR_ID = "123e4567-e89b-42d3-a456-556642445673";
    private static final String PROCESS = "S-50";
    private static final String REFERENCE = "S-1000";


    @Test
    @DisplayName("should get 204 when role assignment records delete  successful")
    public void shouldDeleteRoleAssignmentByActorId() throws Exception {

        when(deleteRoleAssignmentOrchestrator.deleteRoleAssignment(ACTOR_ID, null, null,null))
            .thenReturn(ResponseEntity.status(HttpStatus.NO_CONTENT).build());

        ResponseEntity response = sut.deleteRoleAssignment(null, ACTOR_ID, null, null);

        assertAll(
            () -> assertNotNull(response),
            () -> assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode())
        );
    }

    @Test
    @DisplayName("should get 204 when role assignment records delete  successful")
    public void shouldDeleteRoleAssignmentByProcessAndReference() throws Exception {

        when(deleteRoleAssignmentOrchestrator.deleteRoleAssignment(null, PROCESS, REFERENCE, null))
            .thenReturn(ResponseEntity.status(HttpStatus.NO_CONTENT).build());

        ResponseEntity response = sut.deleteRoleAssignment(null, null, PROCESS, REFERENCE);

        assertAll(
            () -> assertNotNull(response),
            () -> assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode())
        );
    }

    @Test
    @DisplayName("should throw 400 Bad Request when actor Id is null")
    public void shouldThrowBadRequestWhenActorIdNull() throws Exception {

        when(deleteRoleAssignmentOrchestrator.deleteRoleAssignment(null, PROCESS, REFERENCE, null))
            .thenThrow(new BadRequestException(BAD_REQUEST_MISSING_PARAMETERS));
        Assertions.assertThrows(BadRequestException.class, () -> {
            sut.deleteRoleAssignment(null, null, PROCESS, REFERENCE);
        });
    }

    @Test
    @DisplayName("should throw 404 Resource Not Found  when reference is null")
    public void shouldThrowResourceNotFoundWhenReferenceNull() throws Exception {

        when(deleteRoleAssignmentOrchestrator.deleteRoleAssignment(ACTOR_ID, PROCESS, null, null))
            .thenThrow(new ResourceNotFoundException(BAD_REQUEST_MISSING_PARAMETERS));
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            sut.deleteRoleAssignment(null, ACTOR_ID, PROCESS, null);
        });
    }


}
