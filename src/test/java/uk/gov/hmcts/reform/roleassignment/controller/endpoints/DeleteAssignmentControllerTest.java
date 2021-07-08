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
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.roleassignment.domain.service.deleteroles.DeleteRoleAssignmentOrchestrator;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.roleassignment.versions.V1.Error.BAD_REQUEST_MISSING_PARAMETERS;

@RunWith(MockitoJUnitRunner.class)
class DeleteAssignmentControllerTest {

    @Mock
    private DeleteRoleAssignmentOrchestrator deleteRoleAssignmentOrchestrator =
        mock(DeleteRoleAssignmentOrchestrator.class);

    @InjectMocks
    private DeleteAssignmentController sut = new DeleteAssignmentController(deleteRoleAssignmentOrchestrator);

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    private static final String PROCESS = "S-50";
    private static final String REFERENCE = "S-1000";


    @Test
    @DisplayName("should get 204 when role assignment records delete  successful")
    void shouldDeleteRoleAssignmentByProcessAndReference() {

        when(deleteRoleAssignmentOrchestrator.deleteRoleAssignmentByProcessAndReference(PROCESS, REFERENCE))
            .thenReturn(ResponseEntity.status(HttpStatus.NO_CONTENT).build());

        ResponseEntity<?> response = sut.deleteRoleAssignment(null, PROCESS, REFERENCE);

        assertAll(
            () -> assertNotNull(response),
            () -> assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode())
        );
    }

    @Test
    @DisplayName("should get 204 when role assignment records delete by Id successful")
    void shouldDeleteRoleAssignmentById() {

        when(deleteRoleAssignmentOrchestrator
                 .deleteRoleAssignmentByAssignmentId("003352d0-e699-48bc-b6f5-5810411e68af"))
            .thenReturn(ResponseEntity.status(HttpStatus.NO_CONTENT).build());

        ResponseEntity<?> response = sut
            .deleteRoleAssignmentById("003352d0-e699-48bc-b6f5-5810411e68af",
                                      "003352d0-e699-48bc-b6f5-5810411e68af");

        assertAll(
            () -> assertNotNull(response),
            () -> assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode())
        );
    }

    @Test
    @DisplayName("should throw 404 Resource Not Found  when reference is null")
    void shouldThrowResourceNotFoundWhenReferenceNull() {

        when(deleteRoleAssignmentOrchestrator.deleteRoleAssignmentByProcessAndReference(PROCESS, null))
            .thenThrow(new ResourceNotFoundException(BAD_REQUEST_MISSING_PARAMETERS));
        Assertions.assertThrows(ResourceNotFoundException.class, () ->
            sut.deleteRoleAssignment(null, PROCESS, null));
    }


}
