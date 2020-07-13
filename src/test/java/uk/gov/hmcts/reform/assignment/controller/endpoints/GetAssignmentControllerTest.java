package uk.gov.hmcts.reform.assignment.controller.endpoints;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.assignment.helper.TestDataBuilder;
import uk.gov.hmcts.reform.assignment.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.assignment.domain.service.getroles.RetrieveRoleAssignmentOrchestrator;
import uk.gov.hmcts.reform.assignment.feignclients.DataStoreFeignClient;

import static org.mockito.Mockito.doReturn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
class GetAssignmentControllerTest {

    @Mock
    private transient PersistenceService persistenceServiceMock;

    @Mock
    private transient DataStoreFeignClient dataStoreFeignClientMock;

    @Mock
    private transient RetrieveRoleAssignmentOrchestrator retrieveRoleAssignmentServiceMock;

    @InjectMocks
    private GetAssignmentController sut = new GetAssignmentController(persistenceServiceMock,
                                                                      dataStoreFeignClientMock,
                                                                      retrieveRoleAssignmentServiceMock);

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void getListOfRoles() throws Exception {
        ResponseEntity<Object> response = sut.getListOfRoles();
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void shouldGetRoleAssignment() throws Exception {
        String actorId = "123e4567-e89b-42d3-a456-556642445678";
        ResponseEntity<Object> expectedResponse = TestDataBuilder.buildRoleAssignmentResponse();
        doReturn(expectedResponse).when(retrieveRoleAssignmentServiceMock).getAssignmentsByActor(actorId);
        ResponseEntity<Object> response = sut.retrieveRoleAssignmentsByActorId("", actorId);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse.getBody(), response.getBody());
    }
}
