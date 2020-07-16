package uk.gov.hmcts.reform.roleassignment.controller.endpoints;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ParseRequestService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.roleassignment.domain.service.createroles.CreateRoleAssignmentOrchestrator;
import uk.gov.hmcts.reform.roleassignment.domain.service.getroles.RetrieveRoleAssignmentOrchestrator;
import uk.gov.hmcts.reform.roleassignment.feignclients.DataStoreFeignClient;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;

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
    private static final String ROLE_TYPE = "CASE";


    @InjectMocks
    private GetAssignmentController sut = new GetAssignmentController(
        parseRequestService,
        persistenceServiceMock,
        dataStoreFeignClientMock,
        createRoleAssignmentService,
        retrieveRoleAssignmentServiceMock
    );

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
        String actorId = "123e4567-e89b-42d3-a456-556642445678";
        String caseId = "1234567890123456";
        ResponseEntity<Object> expectedResponse = TestDataBuilder.buildRoleAssignmentResponse();
        doReturn(expectedResponse).when(retrieveRoleAssignmentServiceMock).retrieveRoleAssignmentsByActorIdAndCaseId(
            actorId, caseId, ROLE_TYPE);
        ResponseEntity<Object> response = sut.retrieveRoleAssignmentsByActorIdAndCaseId(actorId, caseId, ROLE_TYPE);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse.getBody(), response.getBody());
    }

    @Test
    void shouldGetRoleAssignmentByActorIdOnly() throws Exception {
        String actorId = "123e4567-e89b-42d3-a456-556642445678";
        ResponseEntity<Object> expectedResponse = TestDataBuilder.buildRoleAssignmentResponse();
        doReturn(expectedResponse).when(retrieveRoleAssignmentServiceMock)
                                  .retrieveRoleAssignmentsByActorIdAndCaseId(actorId, null, ROLE_TYPE);
        ResponseEntity<Object> response = sut
            .retrieveRoleAssignmentsByActorIdAndCaseId(actorId, null, RoleType.CASE.name());
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse.getBody(), response.getBody());
    }

    @Test
    void shouldGetRoleAssignmentByCaseIdOnly() throws Exception {
        String caseId = "1234567890123456";
        ResponseEntity<Object> expectedResponse = TestDataBuilder.buildRoleAssignmentResponse();
        doReturn(expectedResponse).when(retrieveRoleAssignmentServiceMock)
                                  .retrieveRoleAssignmentsByActorIdAndCaseId(null, caseId, ROLE_TYPE);
        ResponseEntity<Object> response = sut.retrieveRoleAssignmentsByActorIdAndCaseId(null, caseId, ROLE_TYPE);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse.getBody(), response.getBody());
    }

    @Test
    void shouldReturn404IfNoAssignmentFoundForGetRoleAssignmentByActorIdAndCaseId() throws Exception {
        String actorId = "123e4567-e89b-42d3-a456-556642445678";
        String caseId = "1234567890123456";
        ResponseEntity<Object> expectedResponse = ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        doReturn(expectedResponse).when(retrieveRoleAssignmentServiceMock)
                                  .retrieveRoleAssignmentsByActorIdAndCaseId(actorId, caseId, ROLE_TYPE);

        ResponseEntity<Object> response = sut.retrieveRoleAssignmentsByActorIdAndCaseId(actorId, caseId, ROLE_TYPE);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

    }

    @Test
    void shouldReturn400IfRoleTypeIsNotCaseForGetRoleAssignmentByActorIdAndCaseId() throws Exception {
        String actorId = "123e4567-e89b-42d3-a456-556642445678";
        String roleType = "SomeFakeCaseType";
        Assertions.assertThrows(BadRequestException.class, () -> {
            sut.retrieveRoleAssignmentsByActorIdAndCaseId(actorId, null, roleType);
        });
    }
}
