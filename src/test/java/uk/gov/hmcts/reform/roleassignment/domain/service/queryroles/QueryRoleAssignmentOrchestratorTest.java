package uk.gov.hmcts.reform.roleassignment.domain.service.queryroles;


import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.roleassignment.domain.model.QueryRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.QueryRequests;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentResource;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ParseRequestService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PersistenceService;

import java.io.IOException;
import java.util.Arrays;
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

    private ParseRequestService parseRequestService = new ParseRequestService();

    @InjectMocks
    private QueryRoleAssignmentOrchestrator sut = new QueryRoleAssignmentOrchestrator(
        persistenceServiceMock
    );


    @Test
    void should_PostRoleAssignmentsQueryByRequest() throws IOException {

        List<String> actorId = Arrays.asList(
            "123e4567-e89b-42d3-a456-556642445678",
            "4dc7dd3c-3fb5-4611-bbde-5101a97681e1"
        );
        List<String> roleType = Arrays.asList("CASE", "ORGANISATION");

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
    void should_PostRoleAssignmentByMultipleQueryRequest() throws IOException {

        List<String> actorId = Arrays.asList(
            "123e4567-e89b-42d3-a456-556642445678",
            "4dc7dd3c-3fb5-4611-bbde-5101a97681e1"
        );
        List<String> roleType = Arrays.asList("CASE", "ORGANISATION");

        QueryRequest queryRequest = QueryRequest.builder()
            .actorId(actorId)
            .roleType(roleType)
            .build();
        QueryRequests queryRequests  =  QueryRequests.builder()
            .queryRequests(Arrays.asList(queryRequest))
            .build();

        when(persistenceServiceMock.retrieveRoleAssignmentsByQueryRequest(queryRequest,
                                                                          1,
                                                                          2,
                                                                          "id",
                                                                          "asc",
                                                                          false))
            .thenReturn(Collections.emptyList());
        when(persistenceServiceMock.getTotalRecords()).thenReturn(Long.valueOf(10));
        ResponseEntity<RoleAssignmentResource> result = sut.retrieveRoleAssignmentsByMultipleQueryRequest(queryRequests,
                                                                                                  1,
                                                                                                  2,
                                                                                                  "id",
                                                                                                  "asc");
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getHeaders().containsKey("Total-Records"));
    }

}
