package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentRequestResource;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;

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
        assert assignmentRequestResponse != null;
        assertNull(assignmentRequestResponse.getRoleAssignmentRequest().getRequest().getClientId());
        assertNotNull(assignmentRequestResponse.getRoleAssignmentRequest().getRequest());
        assertNotNull(assignmentRequestResponse.getRoleAssignmentRequest().getRequestedRoles());
    }

    @Test
    void prepareCreateRoleResponse_Rejected() throws IOException {
        ResponseEntity<Object> responseEntity =
            prepareResponseService
                .prepareCreateRoleResponse(TestDataBuilder.buildAssignmentRequest(Status.REJECTED, Status.LIVE, false));
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, responseEntity.getStatusCode());
    }

    @Test
    void prepareRetrieveRoleResponse() throws Exception {
        ResponseEntity<Object> responseEntity =
            prepareResponseService
                .prepareRetrieveRoleResponse((List<RoleAssignment>) TestDataBuilder
                    .buildRequestedRoleCollection(Status.LIVE),
                                             UUID.fromString("6b36bfc6-bb21-11ea-b3de-0242ac140004"));
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    /*@Test
    void addHateosLinks() throws IOException {
        RoleAssignmentRequestResource roleAssignmentRequestResource = new RoleAssignmentRequestResource();
        roleAssignmentRequestResource
            .setRoleAssignmentRequest(TestDataBuilder
                                          .buildAssignmentRequest(Status.CREATED, Status.LIVE, false));

        prepareResponseService.addHateoasLinks(java.util.Optional.of(roleAssignmentRequestResource),
                                               UUID.fromString("6b36bfc6-bb21-11ea-b3de-0242ac140004"));
    }*/
}
