package uk.gov.hmcts.reform.roleassignment.controller.endpoints;

import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.service.createroles.CreateRoleAssignmentOrchestrator;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
class CreateRoleAssignmentControllerTest {

    @Mock
    private CreateRoleAssignmentOrchestrator createRoleAssignmentServiceMock = mock(CreateRoleAssignmentOrchestrator.class);

    @InjectMocks
    private CreateAssignmentController sut;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void createRoleAssignment() {
        ResponseEntity<Object> expectedResponse = new ResponseEntity<>("1234qwer", HttpStatus.CREATED);

        RoleAssignmentRequest request = TestDataBuilder.buildRoleAssignmentRequest();

        when(createRoleAssignmentServiceMock.createRoleAssignment(any())).thenReturn(new ResponseEntity<>("1234qwer", HttpStatus.CREATED));

        ResponseEntity<Object> response = sut.createRoleAssignment("correlationId", "requestorId", "process", "reference", true, request);

        assertEquals(expectedResponse,response);
    }
}
