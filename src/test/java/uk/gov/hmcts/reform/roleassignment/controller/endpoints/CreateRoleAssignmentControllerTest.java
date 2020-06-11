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
import uk.gov.hmcts.reform.roleassignment.domain.service.createroles.CreateRoleAssignmentOrchestrator;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    void createRoleAssignment() throws IOException { //TODO improve this
        AssignmentRequest request = TestDataBuilder.buildAssignmentRequest();
        AssignmentRequest expectedResponse = buildExpectedResponse();
        when(createRoleAssignmentServiceMock.createRoleAssignment(any())).thenReturn(new ResponseEntity<>(expectedResponse, HttpStatus.CREATED));
        ResponseEntity<Object> response = sut.createRoleAssignment(request);
        assertNotNull(response);
    }

    //TODO update this when we decide on expected response
    private AssignmentRequest buildExpectedResponse() throws IOException {
        AssignmentRequest expectedResponse = new AssignmentRequest(TestDataBuilder.buildRequest(), TestDataBuilder.buildRequestedRoleCollection());
        return expectedResponse;
    }
}
