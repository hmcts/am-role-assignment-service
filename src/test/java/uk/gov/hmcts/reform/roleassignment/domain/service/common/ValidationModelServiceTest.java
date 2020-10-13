package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import com.microsoft.applicationinsights.boot.dependencies.google.common.collect.Sets;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.api.runtime.StatelessKieSession;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.QueryRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.Role;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.domain.service.security.IdamRoleService;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;
import uk.gov.hmcts.reform.roleassignment.util.SecurityUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

class ValidationModelServiceTest {

    StatelessKieSession kieSessionMock = mock(StatelessKieSession.class);

    IdamRoleService idamRoleServiceMock = mock(IdamRoleService.class);

    RetrieveDataService retrieveDataServiceMock = mock(RetrieveDataService.class);

    SecurityUtils securityUtilsMock = mock(SecurityUtils.class);

    AssignmentRequest assignmentRequest;

    PersistenceService persistenceService = mock(PersistenceService.class);

    @Getter
    private static final Map<String, List<Role>> configuredRoles = new HashMap<>();

    @InjectMocks
    ValidationModelService sut = new ValidationModelService(kieSessionMock, idamRoleServiceMock,
                                                            retrieveDataServiceMock,
                                                            securityUtilsMock,
                                                            persistenceService
    );

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void validateRequest() throws IOException {

        assignmentRequest = TestDataBuilder
            .buildAssignmentRequest(Status.CREATED, Status.LIVE, false);

        sut.validateRequest(assignmentRequest);

        Mockito.verify(kieSessionMock, times(1)).setGlobal(
            "retrieveDataService",
            retrieveDataServiceMock
        );
        Mockito.verify(kieSessionMock, times(1)).execute((Iterable) any());
    }

    @Test
    void shouldExecuteQueryParamForCaseRole() throws IOException {

        Set<String> actorIds = new HashSet<>();
        Set<String> requestActorIds = new HashSet<>();
        actorIds.add("123e4567-e89b-42d3-a456-556642445678");
        requestActorIds.add("4dc7dd3c-3fb5-4611-bbde-5101a97681e1");

        HashMap<String, List<String>> attributes = new HashMap<>();
        attributes.put("jurisdiction", Arrays.asList("IA"));

        QueryRequest queryRequest = QueryRequest.builder()
            .actorId(List.copyOf(Sets.union(actorIds, requestActorIds)))
            .roleName(Arrays.asList("senior-tribunal-caseworker", "tribunal-caseworker"))
            .roleType(Arrays.asList("ORGANISATION"))
            .attributes(attributes)
            .build();

        when(persistenceService.retrieveRoleAssignmentsByQueryRequest(queryRequest, 0, 0, null, null))
            .thenReturn((List<RoleAssignment>) TestDataBuilder.buildRequestedRoleCollection(Status.LIVE));
        Set<Object> facts = new HashSet<>();
        sut.executeQueryParamForCaseRole(facts, actorIds, requestActorIds);
        assertNotNull(facts);


    }
}
