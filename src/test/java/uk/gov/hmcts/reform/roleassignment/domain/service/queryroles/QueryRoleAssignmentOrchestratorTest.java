package uk.gov.hmcts.reform.roleassignment.domain.service.queryroles;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.roleassignment.domain.model.QueryRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.MultipleQueryRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentResource;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PersistenceService;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
class QueryRoleAssignmentOrchestratorTest {

    @Mock
    private PersistenceService persistenceServiceMock = mock(PersistenceService.class);

    @InjectMocks
    private QueryRoleAssignmentOrchestrator sut = new QueryRoleAssignmentOrchestrator(
        persistenceServiceMock
    );


    @Test
    void should_PostRoleAssignmentsQueryByRequest() {

        List<String> actorId = List.of(
            "123e4567-e89b-42d3-a456-556642445678",
            "4dc7dd3c-3fb5-4611-bbde-5101a97681e1"
        );
        List<String> roleType = List.of("CASE", "ORGANISATION");

        QueryRequest queryRequest = QueryRequest.builder()
            .actorId(actorId)
            .roleType(roleType)
            .build();

        when(persistenceServiceMock.retrieveRoleAssignmentsByQueryRequest(queryRequest,
                                                                          1,
                                                                          2,
                                                                          "id",
                                                                          "asc",
                                                                          false))
            .thenReturn(Collections.emptyList());
        when(persistenceServiceMock.getTotalRecords()).thenReturn(Long.valueOf(10));
        ResponseEntity<RoleAssignmentResource> result = sut.retrieveRoleAssignmentsByQueryRequest(queryRequest,
                                                                                                  1,
                                                                                                  2,
                                                                                                  "id",
                                                                                                  "asc");
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getHeaders().containsKey("Total-Records"));
    }

    @Test
    void shouldFail_PostRoleAssignmentsQueryByRequest() {

        QueryRequest queryRequest = QueryRequest.builder()
            .build();

        when(persistenceServiceMock.retrieveRoleAssignmentsByQueryRequest(queryRequest,
                                                                          1,
                                                                          2,
                                                                          "id",
                                                                          "asc",
                                                                          false))
            .thenReturn(Collections.emptyList());
        when(persistenceServiceMock.getTotalRecords()).thenReturn(Long.valueOf(10));
        Assertions.assertThrows(BadRequestException.class, () -> sut.retrieveRoleAssignmentsByQueryRequest(queryRequest,
                                                                                                          1,
                                                                                                          2,
                                                                                                          "id",
                                                                                                          "asc"));
    }

    @Test
    void should_PostRoleAssignmentByMultipleQueryRequest() {

        List<String> actorId = List.of(
            "123e4567-e89b-42d3-a456-556642445678",
            "4dc7dd3c-3fb5-4611-bbde-5101a97681e1"
        );
        List<String> roleType = List.of("CASE", "ORGANISATION");

        QueryRequest queryRequest = QueryRequest.builder()
            .actorId(actorId)
            .roleType(roleType)
            .build();
        MultipleQueryRequest multipleQueryRequest =  MultipleQueryRequest.builder()
            .queryRequests(List.of(queryRequest))
            .build();

        when(persistenceServiceMock.retrieveRoleAssignmentsByQueryRequest(queryRequest,
                                                                          1,
                                                                          2,
                                                                          "id",
                                                                          "asc",
                                                                          false))
            .thenReturn(Collections.emptyList());
        when(persistenceServiceMock.getTotalRecords()).thenReturn(Long.valueOf(10));
        ResponseEntity<RoleAssignmentResource> result = sut
            .retrieveRoleAssignmentsByMultipleQueryRequest(multipleQueryRequest,
                                                            1,
                                                             2,
                                                              "id",
                                                              "asc");
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getHeaders().containsKey("Total-Records"));
    }

    @Test
    void shouldFail_PostRoleAssignmentsQueryByMultipleRequest() {

        QueryRequest queryRequest = QueryRequest.builder()
            .build();
        MultipleQueryRequest multipleQueryRequest =  MultipleQueryRequest.builder()
            .queryRequests(List.of(queryRequest))
            .build();

        when(persistenceServiceMock.retrieveRoleAssignmentsByQueryRequest(queryRequest,
                                                                          1,
                                                                          2,
                                                                          "id",
                                                                          "asc",
                                                                          false))
            .thenReturn(Collections.emptyList());
        when(persistenceServiceMock.getTotalRecords()).thenReturn(Long.valueOf(10));
        Assertions.assertThrows(BadRequestException.class, () ->
            sut.retrieveRoleAssignmentsByMultipleQueryRequest(multipleQueryRequest,
                                                              1,
                                                              2,
                                                              "id",
                                                              "asc"));
    }

}
