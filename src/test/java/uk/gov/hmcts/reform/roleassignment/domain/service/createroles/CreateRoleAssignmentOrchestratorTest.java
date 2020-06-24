package uk.gov.hmcts.reform.roleassignment.domain.service.createroles;

import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ParseRequestService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ValidationModelService;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;
import uk.gov.hmcts.reform.roleassignment.util.PersistenceUtil;

import static org.mockito.Mockito.mock;

import java.io.IOException;

@RunWith(MockitoJUnitRunner.class)
class CreateRoleAssignmentOrchestratorTest {

    @Mock
    private ParseRequestService parseRequestService = mock(ParseRequestService.class);
    @Mock
    private PersistenceService persistenceService = mock(PersistenceService.class);
    @Mock
    private ValidationModelService validationModelService = mock(ValidationModelService.class);

    @Mock
    private PersistenceUtil persistenceUtil = mock(PersistenceUtil.class);

    @InjectMocks
    private CreateRoleAssignmentOrchestrator sut = new CreateRoleAssignmentOrchestrator(parseRequestService,
                                                                                        persistenceService,
                                                                                        validationModelService,
                                                                                        persistenceUtil
    );

    //@Test improve when orchestration layer is solidified
    void createRoleAssignment() throws IOException {
        AssignmentRequest assignmentRequest = TestDataBuilder.buildAssignmentRequest();
        ResponseEntity<Object> expectedResponseEntity = TestDataBuilder.buildResponseEntity(assignmentRequest);
        //when(parseRequestService.parseRequest(any())).thenReturn(true);
        //ResponseEntity<Object> response = sut.createRoleAssignment(assignmentRequest);
        //assertEquals(expectedResponseEntity.getStatusCode(), response.getStatusCode());
    }
}
