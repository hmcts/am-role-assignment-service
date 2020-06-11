package uk.gov.hmcts.reform.roleassignment.domain.service.createroles;

import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.roleassignment.data.casedata.DefaultCaseDataRepository;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ParseRequestService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.RetrieveDataService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ValidationModelService;
import uk.gov.hmcts.reform.roleassignment.domain.service.security.IdamRoleService;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

@RunWith(MockitoJUnitRunner.class)
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

    //@Test improve when orchestration layer is solidified
    void createRoleAssignment() throws IOException {
        AssignmentRequest assignmentRequest = TestDataBuilder.buildAssignmentRequest();
        ResponseEntity<Object> expectedResponseEntity = TestDataBuilder.buildResponseEntity(assignmentRequest);
        when(parseRequestService.parseRequest(any())).thenReturn(true);
        ResponseEntity<Object> response = sut.createRoleAssignment(assignmentRequest);
        assertEquals(expectedResponseEntity.getStatusCode(), response.getStatusCode());
    }
}
