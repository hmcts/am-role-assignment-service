package uk.gov.hmcts.reform.roleassignment.domain.service.createroles;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import uk.gov.hmcts.reform.roleassignment.data.casedata.DefaultCaseDataRepository;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ParseRequestService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.RetrieveDataService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ValidationModelService;
import uk.gov.hmcts.reform.roleassignment.domain.service.security.IdamRoleService;

import static org.mockito.Mockito.mock;


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
    /*
    @Test
    void createRoleAssignment() throws IOException {
        AssignmentRequest assignmentRequest = TestDataBuilder.buildAssignmentRequest();
        when(parseRequestService.parseRequest(any())).thenReturn(true);
        when(parseRequestService.parseRequest(any())).thenReturn(true);
        //ResponseEntity<Object> response = sut.createRoleAssignment(assignmentRequest);
        //assertNotNull(response);
        //assertEquals(HttpStatus.OK, response.getStatusCode());
    } */

    @Test
    void addExistingRoleAssignments() {
    }

    @Test
    void updateRequestStatus() {
    }
}
