package uk.gov.hmcts.reform.roleassignment.domain.service.getroles;


import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.roleassignment.data.ActorCacheEntity;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentResource;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PrepareResponseService;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
class RetrieveRoleAssignmentOrchestratorTest {

    @Mock
    private PersistenceService persistenceService = mock(PersistenceService.class);

    @Mock
    private PrepareResponseService prepareResponseService = mock(PrepareResponseService.class);

    private static final String ROLE_TYPE = "CASE";

    @InjectMocks
    private RetrieveRoleAssignmentOrchestrator sut = new RetrieveRoleAssignmentOrchestrator(
        persistenceService,
        prepareResponseService
    );

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void getRoleAssignment_shouldGetAssignmentsByActor() throws Exception {

        List<RoleAssignment> roleAssignments
            = (List<RoleAssignment>) TestDataBuilder.buildRequestedRoleCollection(Status.LIVE);
        String actorId = "123e4567-e89b-42d3-a456-556642445678";
        ResponseEntity<RoleAssignmentResource> roles = TestDataBuilder.buildResourceRoleAssignmentResponse(Status.LIVE);
        when(persistenceService.getAssignmentsByActor(actorId)).thenReturn(roleAssignments);
        when(prepareResponseService.prepareRetrieveRoleResponse(roleAssignments, actorId)).thenReturn(
            roles);

        ResponseEntity<RoleAssignmentResource> response = sut.getAssignmentsByActor(actorId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(persistenceService, times(1)).getAssignmentsByActor(any(String.class));
        verify(prepareResponseService, times(1))
            .prepareRetrieveRoleResponse(any(), any(String.class));
    }

    @Test
    void getRoleAssignment_shouldThrowBadRequestWhenActorIsEmpty() throws Exception {

        List<RoleAssignment> roleAssignments
            = (List<RoleAssignment>) TestDataBuilder.buildRequestedRoleCollection(Status.LIVE);
        String actorId = "";
        Assertions.assertThrows(BadRequestException.class, () ->
            sut.getAssignmentsByActor(actorId)
        );
    }

    @Test
    void getRoleAssignment_shouldThrowBadRequestWhenActorIsNotValid() throws Exception {

        List<RoleAssignment> roleAssignments
            = (List<RoleAssignment>) TestDataBuilder.buildRequestedRoleCollection(Status.LIVE);
        String actorId = "123e4567-e89b-42d3-a456-^&%$£%";
        Assertions.assertThrows(BadRequestException.class, () ->
            sut.getAssignmentsByActor(actorId)
        );
    }

    @Test
    void getRoleAssignment_shouldThrowResourceNotFoundWhenActorIsNotAvailable() throws Exception {

        List<RoleAssignment> roleAssignments = new ArrayList<>();
        String actorId = "123e4567-e89b-42d3-a456-556642445678";
        ResponseEntity<Object> roles = TestDataBuilder.buildRoleAssignmentResponse(Status.CREATED, Status.LIVE, false);
        when(persistenceService.getAssignmentsByActor(actorId)).thenReturn(roleAssignments);
        Assertions.assertThrows(ResourceNotFoundException.class, () ->
            sut.getAssignmentsByActor(actorId)
        );
        verify(persistenceService, times(1)).getAssignmentsByActor(any(String.class));
    }

    @Test
    void getRoleAssignment_shouldRetrieveETag() throws Exception {

        String actorId = "123e4567-e89b-42d3-a456-556642445678";
        ResponseEntity<Object> roles = TestDataBuilder.buildRoleAssignmentResponse(Status.CREATED, Status.LIVE, false);
        ActorCacheEntity actorCacheEntity = TestDataBuilder.buildActorCacheEntity();
        when(persistenceService.getActorCacheEntity(actorId)).thenReturn(actorCacheEntity);
        long etag = sut.retrieveETag(actorId);
        assertEquals(1, etag);
        verify(persistenceService, times(1)).getActorCacheEntity(any(String.class));
    }

    @Test
    void getListOfRoles() throws IOException {
        JsonNode roles = sut.getListOfRoles();
        assertNotNull(roles);
        assertEquals(4, roles.size());
    }
}
