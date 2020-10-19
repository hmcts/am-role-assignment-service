package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.api.runtime.StatelessKieSession;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.Role;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RequestType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.DELETED;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.LIVE;

class ValidationModelServiceTest {

    StatelessKieSession kieSessionMock = mock(StatelessKieSession.class);


    RetrieveDataService retrieveDataServiceMock = mock(RetrieveDataService.class);


    AssignmentRequest assignmentRequest;

    PersistenceService persistenceService = mock(PersistenceService.class);

    @Getter
    private static final Map<String, List<Role>> configuredRoles = new HashMap<>();

    @InjectMocks
    ValidationModelService sut = new ValidationModelService(
        kieSessionMock,
        retrieveDataServiceMock,
        persistenceService
    );

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void validateRequest() throws IOException {

        assignmentRequest = TestDataBuilder
            .buildAssignmentRequest(Status.CREATED, LIVE, false);

        sut.validateRequest(assignmentRequest);


        Mockito.verify(kieSessionMock, times(1)).execute((Iterable) any());
    }

    @Test
    void shouldExecuteQueryParamForCaseRole() throws IOException {

        Set<String> actorIds = new HashSet<>();
        Set<String> requestActorIds = new HashSet<>();
        actorIds.add("123e4567-e89b-42d3-a456-556642445678");
        requestActorIds.add("4dc7dd3c-3fb5-4611-bbde-5101a97681e1");

        when(persistenceService.retrieveRoleAssignmentsByQueryRequest(any(), anyInt(), anyInt(), any(), any()))
            .thenReturn((List<RoleAssignment>) TestDataBuilder.buildRequestedRoleCollection(LIVE));
        Set<Object> facts = new HashSet<>();
        sut.executeQueryParamForCaseRole(facts, actorIds, requestActorIds);
        assertNotNull(facts);
        assertEquals(2, facts.size());


    }

    @Test
    void shouldAddJudgeRoleExistingRecordsForDelete() throws IOException {

        Set<String> actorIds = new HashSet<>();
        Set<String> requestActorIds = new HashSet<>();
        actorIds.add("123e4567-e89b-42d3-a456-556642445678");
        requestActorIds.add("4dc7dd3c-3fb5-4611-bbde-5101a97681e1");
        AssignmentRequest assignmentRequest = TestDataBuilder.buildAssignmentRequest(
            DELETED, LIVE, false);


        Set<Object> facts = new HashSet<>();
        sut.addExistingRecordsForDelete(assignmentRequest, facts);
        assertNotNull(facts);
        assertEquals(0, facts.size());


    }

    @Test
    void shouldAddExistingRecordsForDelete() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Set<String> actorIds = new HashSet<>();
        Set<String> requestActorIds = new HashSet<>();
        actorIds.add("123e4567-e89b-42d3-a456-556642445678");
        requestActorIds.add("4dc7dd3c-3fb5-4611-bbde-5101a97681e1");
        AssignmentRequest assignmentRequest = TestDataBuilder.buildAssignmentRequest(
            DELETED, LIVE, false);
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            roleAssignment.setActorId(assignmentRequest.getRequest().getAuthenticatedUserId());
            roleAssignment.setRoleName("tribunal-caseworker");
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.getAttributes().put("jurisdiction", mapper.valueToTree("IA"));
        });


        Set<Object> facts = new HashSet<>();
        sut.addExistingRecordsForDelete(assignmentRequest, facts);
        assertNotNull(facts);
        assertEquals(1, facts.size());


    }

    @Test
    void shouldExecuteForDeleteRequest() throws IOException {

        AssignmentRequest assignmentRequest = TestDataBuilder.buildAssignmentRequest(
            DELETED, LIVE, false);
        assignmentRequest.getRequest().setRequestType(RequestType.DELETE);

        sut.validateRequest(assignmentRequest);
        Mockito.verify(kieSessionMock, times(1)).execute((Iterable) any());


    }

    @Test
    void shouldExecuteQueryParamForDelete() throws IOException {

        AssignmentRequest assignmentRequest = TestDataBuilder.buildAssignmentRequest(
            DELETED, LIVE, false);
        assignmentRequest.getRequestedRoles().forEach(roleAssignment ->
                                                          roleAssignment.setRoleName("tribunal-caseworker")

        );

        when(persistenceService.retrieveRoleAssignmentsByQueryRequest(any(), anyInt(), anyInt(), any(), any()))
            .thenReturn((List<RoleAssignment>) TestDataBuilder.buildRequestedRoleCollection(LIVE));
        Set<Object> facts = new HashSet<>();
        sut.addExistingRecordsForDelete(assignmentRequest, facts);
        assertNotNull(facts);
        assertEquals(2, facts.size());


    }
}
