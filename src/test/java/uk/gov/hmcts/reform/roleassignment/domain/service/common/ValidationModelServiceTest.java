package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.api.runtime.StatelessKieSession;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.roleassignment.domain.model.Assignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.Role;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;
import uk.gov.hmcts.reform.roleassignment.launchdarkly.FeatureToggleService;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
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
        persistenceService);

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

        actorIds.add("123e4567-e89b-42d3-a456-556642445678");
        actorIds.add("4dc7dd3c-3fb5-4611-bbde-5101a97681e1");

        doReturn(TestDataBuilder.buildRequestedRoleCollection(LIVE)).when(persistenceService)
            .retrieveRoleAssignmentsByQueryRequest(
            any(),
            anyInt(),
            anyInt(),
            any(),
            any(),
            anyBoolean()
        );

        List<? extends Assignment> existingRecords = sut.getCurrentRoleAssignmentsForActors(actorIds);
        assertNotNull(existingRecords);
        assertEquals(2, existingRecords.size());


    }


}
