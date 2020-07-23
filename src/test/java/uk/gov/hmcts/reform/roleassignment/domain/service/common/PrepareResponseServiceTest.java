package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.assignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.assignment.domain.model.RoleAssignmentRequestResource;
import uk.gov.hmcts.reform.assignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.assignment.domain.service.common.PrepareResponseService;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
class PrepareResponseServiceTest {

    @InjectMocks
    PrepareResponseService prepareResponseService = new PrepareResponseService();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void prepareCreateRoleResponse() throws IOException {
        ResponseEntity<Object> responseEntity =
            prepareResponseService
                .prepareCreateRoleResponse(TestDataBuilder.buildAssignmentRequest(Status.CREATED, Status.LIVE, false));
        RoleAssignmentRequestResource assignmentRequestResponse =
            (RoleAssignmentRequestResource) responseEntity.getBody();
        assertNull(assignmentRequestResponse.getRoleAssignmentRequest().getRequest().getClientId());
    }

    @Test
    void prepareRetrieveRoleResponse() throws Exception {
        ResponseEntity<Object> responseEntity =
            prepareResponseService
                .prepareRetrieveRoleResponse((List<RoleAssignment>) TestDataBuilder
                    .buildRequestedRoleCollection(Status.LIVE),
                                             UUID.fromString("6b36bfc6-bb21-11ea-b3de-0242ac140004"));
        //RoleAssignmentResource assignmentRequestResponse = (RoleAssignmentResource) responseEntity.getBody();
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
}
