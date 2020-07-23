package uk.gov.hmcts.reform.roleassignmentrefactored.controller.endpoints;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.roleassignmentrefactored.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignmentrefactored.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignmentrefactored.domain.service.queryroles.QueryRoleAssignmentOrchestrator;
import uk.gov.hmcts.reform.roleassignmentrefactored.helper.TestDataBuilder;

@RunWith(MockitoJUnitRunner.class)
class QueryAssignmentControllerTest {

    private final QueryRoleAssignmentOrchestrator queryRoleAssignmentOrchestrator =
        mock(QueryRoleAssignmentOrchestrator.class);

    private static final String ROLE_TYPE = "CASE";


    @InjectMocks
    private final QueryAssignmentController sut = new QueryAssignmentController(queryRoleAssignmentOrchestrator);

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void shouldGetRoleAssignmentByActorIdAndCaseId() throws Exception {
        String actorId = "123e4567-e89b-42d3-a456-556642445678";
        String caseId = "1234567890123456";
        ResponseEntity<Object> expectedResponse
            = TestDataBuilder.buildRoleAssignmentResponse(Status.CREATED, Status.LIVE, false);
        doReturn(expectedResponse).when(queryRoleAssignmentOrchestrator).retrieveRoleAssignmentsByActorIdAndCaseId(
            actorId, caseId, ROLE_TYPE);
        ResponseEntity<Object> response = sut.retrieveRoleAssignmentsByActorIdAndCaseId(actorId, caseId, ROLE_TYPE);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse.getBody(), response.getBody());
    }

    @Test
    void shouldGetRoleAssignmentByActorIdOnly() throws Exception {
        String actorId = "123e4567-e89b-42d3-a456-556642445678";
        ResponseEntity<Object> expectedResponse
            = TestDataBuilder.buildRoleAssignmentResponse(Status.CREATED, Status.LIVE, false);
        doReturn(expectedResponse).when(queryRoleAssignmentOrchestrator)
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
        ResponseEntity<Object> expectedResponse
            = TestDataBuilder.buildRoleAssignmentResponse(Status.CREATED, Status.LIVE, false);
        doReturn(expectedResponse).when(queryRoleAssignmentOrchestrator)
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
        doReturn(expectedResponse).when(queryRoleAssignmentOrchestrator)
                                  .retrieveRoleAssignmentsByActorIdAndCaseId(actorId, caseId, ROLE_TYPE);

        ResponseEntity<Object> response = sut.retrieveRoleAssignmentsByActorIdAndCaseId(actorId, caseId, ROLE_TYPE);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

    }

}
