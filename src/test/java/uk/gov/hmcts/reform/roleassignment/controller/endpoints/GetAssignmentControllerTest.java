package uk.gov.hmcts.reform.roleassignment.controller.endpoints;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ParseRequestService;
import uk.gov.hmcts.reform.roleassignment.domain.service.getroles.RetrieveRoleAssignmentOrchestrator;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
class GetAssignmentControllerTest {

    @Mock
    private final RetrieveRoleAssignmentOrchestrator retrieveRoleAssignmentServiceMock =
        mock(RetrieveRoleAssignmentOrchestrator.class);

    @InjectMocks
    private GetAssignmentController sut = new GetAssignmentController(retrieveRoleAssignmentServiceMock);

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void getListOfRoles() throws Exception {
        Mockito.when(retrieveRoleAssignmentServiceMock.getListOfRoles())
            .thenReturn(new ResponseEntity<Object>(HttpStatus.OK));
        ResponseEntity<Object> response = sut.getListOfRoles("123e4567-e89b-42d3-a456-5566");
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void shouldGetRoleAssignment() throws Exception {
        String actorId = "123e4567-e89b-42d3-a456-556642445678";
        ResponseEntity<Object> expectedResponse = TestDataBuilder
            .buildRoleAssignmentResponse(Status.CREATED, Status.LIVE, false);
        doReturn(expectedResponse).when(retrieveRoleAssignmentServiceMock).getAssignmentsByActor(actorId);
        long etag = 1;
        doReturn(etag).when(retrieveRoleAssignmentServiceMock).retrieveETag(actorId);
        ResponseEntity<Object> response = sut.retrieveRoleAssignmentsByActorId("","", actorId);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse.getBody(), response.getBody());
    }
}
