package uk.gov.hmcts.reform.roleassignment.domain.service.queryroles;


import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.roleassignment.domain.model.QueryRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ParseRequestService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
class QueryRoleAssignmentOrchestratorTest {

    @Mock
    private PersistenceService persistenceServiceMock = mock(PersistenceService.class);

    @Mock
    private ParseRequestService parseRequestService = mock(ParseRequestService.class);

    @InjectMocks
    private QueryRoleAssignmentOrchestrator sut = new QueryRoleAssignmentOrchestrator(
        persistenceServiceMock,
        parseRequestService
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

        when(persistenceServiceMock.retrieveRoleAssignmentsByQueryRequest(queryRequest, 1, 2, "id", "asc"))
            .thenReturn((List<RoleAssignment>) TestDataBuilder.buildRequestedRoleCollection(Status.LIVE));
        when(persistenceServiceMock.getTotalRecords()).thenReturn(Long.valueOf(10));
        when(parseRequestService.getCorrelationId()).thenReturn("1234");
        ResponseEntity<Object> result = sut.retrieveRoleAssignmentsByQueryRequest(queryRequest, 1, 2, "id", "asc");
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
    }


}
