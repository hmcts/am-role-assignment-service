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
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleRequest;
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

        RoleAssignmentRequest request = TestDataBuilder.buildRoleAssignmentRequest();
        RoleAssignmentRequest expectedResponse = buildExpectedResponse();

        when(createRoleAssignmentServiceMock.createRoleAssignment(any())).thenReturn(new ResponseEntity<>(expectedResponse, HttpStatus.CREATED));

        ResponseEntity<Object> response = sut.createRoleAssignment(request);

        assertNotNull(response);
    }

    private RoleAssignmentRequest buildExpectedResponse() {
        RoleAssignmentRequest expectedResponse = new RoleAssignmentRequest();
        LocalDateTime timeStamp = LocalDateTime.now();

        RoleRequest request = RoleRequest.builder().id(UUID.fromString("21334a2b-79ce-44eb-9168-2d49a744be9c")).correlationId(
            "correlationId").clientId("clientId").authenticatedUserId("userId").requestorId("requestorId").requestType(
            RequestType.CREATE).status(Status.APPROVED).process("process").reference("reference").replaceExisting(true).roleAssignmentId(
            "roleAssignmentId").timestamp(timeStamp).build();
        expectedResponse.setRoleRequest(request);
        return expectedResponse;
    }
}
