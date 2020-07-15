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
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ParseRequestService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.roleassignment.domain.service.createroles.CreateRoleAssignmentOrchestrator;
import uk.gov.hmcts.reform.roleassignment.domain.service.getroles.RetrieveRoleAssignmentOrchestrator;
import uk.gov.hmcts.reform.roleassignment.feignclients.DataStoreFeignClient;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;
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
        ResponseEntity<Object> response = sut.retrieveRoleAssignmentsByActorId("", actorId);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse.getBody(), response.getBody());
    }

    @Test
    void shouldGetRoleAssignmentByActorIdAndCaseId() throws Exception {
        /*String actorId = "123e4567-e89b-42d3-a456-556642445678";
        String caseId = "1234567890123456";
        ResponseEntity<Object> expectedResponse = TestDataBuilder.buildRoleAssignmentResponse();
        doReturn(expectedResponse).when(retrieveRoleAssignmentServiceMock).retrieveRoleAssignmentsByActorIdOrCaseId(actorId, caseId);
        ResponseEntity<Object> response = sut.retrieveRoleAssignmentsByActorIdAndCaseId(RoleType.CASE.name(), actorId, caseId);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse.getBody(), response.getBody());*/
    }
    @Test
    void shouldGetRoleAssignmentByActorIdOnly() throws Exception {
        /*String actorId = "123e4567-e89b-42d3-a456-556642445678";
        ResponseEntity<Object> expectedResponse = TestDataBuilder.buildRoleAssignmentResponse();
        doReturn(expectedResponse).when(retrieveRoleAssignmentServiceMock).retrieveRoleAssignmentsByActorIdOrCaseId(actorId, caseId);
        ResponseEntity<Object> response = sut.retrieveRoleAssignmentsByActorIdAndCaseId(RoleType.CASE.name(), actorId, caseId);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse.getBody(), response.getBody());*/
    }

    @Test
    void shouldGetRoleAssignmentByCaseIdOnly() throws Exception {
        /*String caseId = "1234567890123456";
        ResponseEntity<Object> expectedResponse = TestDataBuilder.buildRoleAssignmentResponse();
        doReturn(expectedResponse).when(retrieveRoleAssignmentServiceMock).retrieveRoleAssignmentsByActorIdOrCaseId(actorId, caseId);
        ResponseEntity<Object> response = sut.retrieveRoleAssignmentsByActorIdAndCaseId(RoleType.CASE.name(), actorId, caseId);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse.getBody(), response.getBody());*/
    }

    @Test
    void shouldReturn404IfNoAssignmentFoundForGetRoleAssignmentByActorIdAndCaseId() throws Exception {
        /*String actorId = "123e4567-e89b-42d3-a456-556642445678";
        String caseId = "1234567890123456";
        ResponseEntity<Object> expectedResponse = TestDataBuilder.buildRoleAssignmentResponse();
        doReturn(expectedResponse).when(retrieveRoleAssignmentServiceMock).retrieveRoleAssignmentsByActorIdOrCaseId(actorId, caseId);
        ResponseEntity<Object> response = sut.retrieveRoleAssignmentsByActorIdAndCaseId(RoleType.CASE.name(), actorId, caseId);
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldReturn400IfRoleTypeIsNotCASEForGetRoleAssignmentByActorIdAndCaseId() throws Exception {
        String actorId = "123e4567-e89b-42d3-a456-556642445678";
        String caseId = "1234567890123456";
        String CaseType = "SomeFakeCaseType";
        ResponseEntity<Object> response = sut.retrieveRoleAssignmentsByActorIdAndCaseId(CaseType, actorId, caseId);
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());*/
    }
}
