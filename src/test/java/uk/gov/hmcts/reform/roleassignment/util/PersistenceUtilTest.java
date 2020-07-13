package uk.gov.hmcts.reform.roleassignment.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.RequestEntity;
import uk.gov.hmcts.reform.roleassignment.domain.model.Request;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
class PersistenceUtilTest {

    @InjectMocks
    PersistenceUtil persistenceUtil = new PersistenceUtil();


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void convertRoleAssignmentToHistoryEntity() {
        //idamRoleService.convertRoleAssignmentToHistoryEntity();
    }

    @Test
    void convertRequestToEntity() {
        Request request = TestDataBuilder.buildRequest();
        RequestEntity requestEntity = persistenceUtil.convertRequestToEntity(request);
        assertNotNull(requestEntity);
    }

    @Test
    void convertRoleAssignmentToEntity() {
        //idamRoleService.convertRoleAssignmentToEntity();
    }

    @Test
    void convertActorCacheToEntity() {
        //idamRoleService.convertActorCacheToEntity();
    }

    @Test
    void convertHistoryEntityToRoleAssignment() {
        //idamRoleService.convertHistoryEntityToRoleAssignment();
    }

    @Test
    void convertEntityToRoleAssignment() {
        //idamRoleService.convertEntityToRoleAssignment();
    }

    private RequestEntity convertRequestToEntityHelper(Request request) {
        return RequestEntity.builder()
            .correlationId(request.getCorrelationId())
            .status(request.getStatus().toString())
            .process(request.getProcess())
            .reference(request.getReference())
            .authenticatedUserId(request.getAuthenticatedUserId())
            .clientId(request.getClientId())
            .assignerId(request.getAssignerId())
            .replaceExisting(request.replaceExisting)
            .requestType(request.getRequestType().toString())
            .created(request.getCreated())
            .log(request.getLog())
            .roleAssignmentId(request.getRoleAssignmentId())
            .build();

    }
}
