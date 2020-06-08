package uk.gov.hmcts.reform.roleassignment.controller.endpoints;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.Request;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.*;
import uk.gov.hmcts.reform.roleassignment.domain.service.createroles.CreateRoleAssignmentOrchestrator;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

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
    void createRoleAssignment() { //TODO improve this
        //ResponseEntity<Object> expectedResponse = new ResponseEntity<>("1234qwer", HttpStatus.CREATED);

        AssignmentRequest request = TestDataBuilder.buildRoleAssignmentRequest();
        AssignmentRequest expectedResponse = buildExpectedResponse();

        when(createRoleAssignmentServiceMock.createRoleAssignment(any())).thenReturn(new ResponseEntity<>(expectedResponse, HttpStatus.CREATED));

        ResponseEntity<Object> response = sut.createRoleAssignment(request);

        assertNotNull(response);
    }

    private AssignmentRequest buildExpectedResponse() {
        AssignmentRequest expectedResponse = new AssignmentRequest();

        Request request = TestDataBuilder.buildRequest();
        expectedResponse.setRequest(request);
        return expectedResponse;
    }
}
