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
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ParseRequestService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.roleassignment.domain.service.createroles.CreateRoleAssignmentOrchestrator;
import uk.gov.hmcts.reform.roleassignment.domain.service.getroles.RetrieveRoleAssignmentOrchestrator;
import uk.gov.hmcts.reform.roleassignment.feignclients.DataStoreFeignClient;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;
import static org.mockito.Mockito.doReturn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
class GetAssignmentControllerTest {

    @Mock
    private transient PersistenceService persistenceServiceMock;

    @Mock
    private transient DataStoreFeignClient dataStoreFeignClientMock;

    @Mock
    private transient RetrieveRoleAssignmentOrchestrator retrieveRoleAssignmentServiceMock;

    @Mock
    private transient ParseRequestService parseRequestService;

    @Mock
    private CreateRoleAssignmentOrchestrator createRoleAssignmentService;


    @InjectMocks
    private GetAssignmentController sut = new GetAssignmentController(parseRequestService,
                                                                      persistenceServiceMock,
                                                                      dataStoreFeignClientMock,
                                                                      createRoleAssignmentService,
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
        long eTag = 1;
        doReturn(eTag).when(retrieveRoleAssignmentServiceMock).retrieveETag(UUID.fromString(actorId));
        ResponseEntity<Object> response = sut.retrieveRoleAssignmentsByActorId("", actorId);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse.getBody(), response.getBody());
    }
}
