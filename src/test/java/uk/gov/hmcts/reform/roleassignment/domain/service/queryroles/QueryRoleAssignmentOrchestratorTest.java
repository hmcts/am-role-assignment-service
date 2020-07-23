package uk.gov.hmcts.reform.roleassignment.domain.service.queryroles;


import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.assignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.assignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.assignment.domain.service.common.ParseRequestService;
import uk.gov.hmcts.reform.assignment.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.assignment.domain.service.queryroles.QueryRoleAssignmentOrchestrator;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;

import static org.drools.compiler.lang.DroolsSoftKeywords.CASE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
class QueryRoleAssignmentOrchestratorTest {

    @Mock
    private PersistenceService persistenceServiceMock = mock(PersistenceService.class);

    @Mock
    private ParseRequestService parseRequestServiceMock = mock(ParseRequestService.class);

    @InjectMocks
    private QueryRoleAssignmentOrchestrator sut = new QueryRoleAssignmentOrchestrator(persistenceServiceMock,
                                                                                      parseRequestServiceMock);

    @Test
    void should_retrieveRoleAssignmentsByActorIdAndCaseId() throws IOException {
        String actorId = "003352d0-e699-48bc-b6f5-5810411e60af";
        String caseId = "1234567890123456";

        when(persistenceServiceMock.getAssignmentsByActorAndCaseId(actorId, caseId, CASE))
            .thenReturn((List<RoleAssignment>) TestDataBuilder.buildRequestedRoleCollection(Status.LIVE));
        ResponseEntity<Object> result = sut.retrieveRoleAssignmentsByActorIdAndCaseId(actorId, caseId, CASE);
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
    }
}
