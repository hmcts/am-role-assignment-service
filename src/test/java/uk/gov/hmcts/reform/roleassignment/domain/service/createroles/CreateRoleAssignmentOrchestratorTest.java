package uk.gov.hmcts.reform.roleassignment.domain.service.createroles;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.roleassignment.data.casedata.DefaultCaseDataRepository;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ParseRequestService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.RetrieveDataService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ValidationModelService;
import uk.gov.hmcts.reform.roleassignment.domain.service.security.IdamRoleService;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class CreateRoleAssignmentOrchestratorTest {

    @Mock
    private DefaultCaseDataRepository caseService = mock(DefaultCaseDataRepository.class);
    @Mock
    private IdamRoleService idamService = mock(IdamRoleService.class);
    @Mock
    private RetrieveDataService retrieveDataService = mock(RetrieveDataService.class);
    @Mock
    private ParseRequestService parseRequestService = mock(ParseRequestService.class);
    @Mock
    private PersistenceService persistenceService = mock(PersistenceService.class);
    @Mock
    private ValidationModelService validationModelService = mock(ValidationModelService.class);

    @InjectMocks
    private CreateRoleAssignmentOrchestrator sut = new CreateRoleAssignmentOrchestrator(caseService,
                                                                                        idamService,
                                                                                        retrieveDataService,
                                                                                        parseRequestService,
                                                                                        persistenceService);

    @Test
    void createRoleAssignment() {
        AssignmentRequest assignmentRequest = TestDataBuilder.buildAssignmentRequest();
        when(parseRequestService.parseRequest(any())).thenReturn(true);
        ResponseEntity<Object> response = sut.createRoleAssignment(assignmentRequest);
        assertNotNull(response);
    }

    @Test
    void addExistingRoleAssignments() {
    }

    @Test
    void updateRequestStatus() {
    }
}
