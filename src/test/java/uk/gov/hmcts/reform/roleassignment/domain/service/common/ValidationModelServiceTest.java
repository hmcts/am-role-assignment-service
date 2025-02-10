package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.api.runtime.StatelessKieSession;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.roleassignment.config.EnvironmentConfiguration;
import uk.gov.hmcts.reform.roleassignment.domain.model.Assignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.FeatureFlagEnum;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.LIVE;

class ValidationModelServiceTest {

    private static final int PAGE_SIZE_INTERNAL = 20;
    private static final String SORT_COLUMN_UNIQUE = "id";

    StatelessKieSession kieSessionMock = mock(StatelessKieSession.class);


    RetrieveDataService retrieveDataServiceMock = mock(RetrieveDataService.class);


    AssignmentRequest assignmentRequest;

    PersistenceService persistenceService = mock(PersistenceService.class);

    @Mock
    Logger logger = mock(Logger.class);

    @Mock
    EnvironmentConfiguration environmentConfiguration;

    @InjectMocks
    ValidationModelService sut = new ValidationModelService(
        kieSessionMock,
        retrieveDataServiceMock,
        persistenceService
    );

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void validateRequest() throws IOException {
        when(environmentConfiguration.getEnvironment()).thenReturn("prod");
        assignmentRequest = TestDataBuilder
            .buildAssignmentRequest(Status.CREATED, LIVE, false);
        AssignmentRequest assignmentRequestSpy = Mockito.spy(assignmentRequest);
        sut.validateRequest(assignmentRequestSpy);

        verify(assignmentRequestSpy, times(6)).getRequest();
        verify(assignmentRequestSpy, times(2)).getRequestedRoles();

        verify(kieSessionMock, times(1)).execute((Iterable) any());
    }

    @Test
    void validateRequest_withEmptyRoles() throws IOException {
        when(environmentConfiguration.getEnvironment()).thenReturn("prod");
        assignmentRequest = TestDataBuilder
            .buildAssignmentRequest(Status.CREATED, LIVE, false);
        assignmentRequest.setRequestedRoles(Collections.emptyList());
        AssignmentRequest assignmentRequestSpy = Mockito.spy(assignmentRequest);
        sut.validateRequest(assignmentRequestSpy);

        verify(assignmentRequestSpy, times(6)).getRequest();
        verify(assignmentRequestSpy, times(2)).getRequestedRoles();

        verify(kieSessionMock, times(1)).execute((Iterable) any());
        verify(kieSessionMock, times(1)).setGlobal(any(), any());

        // verify when in PROD environment: the flag cache is used: i.e. not data from DB/persistenceService
        verify(persistenceService, never()).getStatusByParam(any(), any());
    }

    @Test
    void validateRequest_Scenario_withPrEnv() throws IOException {
        when(environmentConfiguration.getEnvironment()).thenReturn("pr");
        assignmentRequest = TestDataBuilder.buildEmptyAssignmentRequest(LIVE);
        AssignmentRequest assignmentRequestSpy = Mockito.spy(assignmentRequest);

        sut.validateRequest(assignmentRequestSpy);
        verify(assignmentRequestSpy, times(4)).getRequest();
        verify(assignmentRequestSpy, times(1)).getRequestedRoles();
        verify(kieSessionMock, times(1)).execute((Iterable) any());

        // verify when in none PROD environment: all flags are loaded from DB/persistenceService
        Mockito.verify(persistenceService, Mockito.times(FeatureFlagEnum.values().length))
            .getStatusByParam(any(), eq("pr"));
    }

    @Test
    void validateRequest_Scenario_withoutEnv() throws IOException {
        assignmentRequest = TestDataBuilder.buildEmptyAssignmentRequest(LIVE);
        AssignmentRequest assignmentRequestSpy = Mockito.spy(assignmentRequest);

        assertThrows(NullPointerException.class, () -> sut.validateRequest(assignmentRequestSpy));
    }

    @Test
    void shouldExecuteQueryParamForCaseRole() throws IOException {
        ReflectionTestUtils.setField(sut, "sizeInternal", PAGE_SIZE_INTERNAL);
        ReflectionTestUtils.setField(sut, "sortColumnUnique", SORT_COLUMN_UNIQUE);

        Set<String> actorIds = Set.of(
            "123e4567-e89b-42d3-a456-556642445678",
            "4dc7dd3c-3fb5-4611-bbde-5101a97681e1"
        );

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
        verify(persistenceService, times(1)).retrieveRoleAssignmentsByQueryRequest(
            any(), anyInt(), eq(PAGE_SIZE_INTERNAL), eq(SORT_COLUMN_UNIQUE), any(), anyBoolean()
        );
    }

    @Test
    void shouldExecuteQueryParamForMultipleCaseRole() throws IOException {
        ReflectionTestUtils.setField(sut, "sizeInternal", PAGE_SIZE_INTERNAL);
        ReflectionTestUtils.setField(sut, "sortColumnUnique", SORT_COLUMN_UNIQUE);

        final Set<String> actorIds = Set.of(
            "123e4567-e89b-42d3-a456-556642445678",
            "4dc7dd3c-3fb5-4611-bbde-5101a97681e1"
        );

        doReturn(TestDataBuilder.buildRequestedRoleCollection(LIVE)).when(persistenceService)
            .retrieveRoleAssignmentsByQueryRequest(
                any(),
                anyInt(),
                anyInt(),
                any(),
                any(),
                anyBoolean()
        );
        when(persistenceService.getTotalRecords()).thenReturn(21L);

        List<? extends Assignment> existingRecords = sut.getCurrentRoleAssignmentsForActors(actorIds);
        assertNotNull(existingRecords);
        assertEquals(4, existingRecords.size());
        verify(persistenceService, times(2)).retrieveRoleAssignmentsByQueryRequest(
            any(), anyInt(), eq(PAGE_SIZE_INTERNAL), eq(SORT_COLUMN_UNIQUE), any(), anyBoolean()
        );

    }

    @Test
    void shouldLogWhenTotalRecordsExceed100() throws IOException {
        ReflectionTestUtils.setField(sut, "sizeInternal", PAGE_SIZE_INTERNAL);
        ReflectionTestUtils.setField(sut, "sortColumnUnique", SORT_COLUMN_UNIQUE);

        final Set<String> actorIds = Set.of(
            "123e4567-e89b-42d3-a456-556642445678",
            "4dc7dd3c-3fb5-4611-bbde-5101a97681e1"
        );

        doReturn(TestDataBuilder.buildRequestedRoleCollection(LIVE)).when(persistenceService)
            .retrieveRoleAssignmentsByQueryRequest(
                any(),
                anyInt(),
                anyInt(),
                any(),
                any(),
                anyBoolean()
        );
        when(persistenceService.getTotalRecords()).thenReturn(2200L);

        List<? extends Assignment> existingRecords = sut.getCurrentRoleAssignmentsForActors(actorIds);
        assertNotNull(existingRecords);
        assertEquals(220, existingRecords.size());
        verify(persistenceService, times(110)).retrieveRoleAssignmentsByQueryRequest(
            any(), anyInt(), eq(PAGE_SIZE_INTERNAL), eq(SORT_COLUMN_UNIQUE), any(), anyBoolean()
        );

    }

    @Test
    void shouldLogMsg() {
        Mockito.doNothing().when(logger).debug(any());
        ValidationModelService.logMsg("1234567890123456");
        assertNotNull(logger);
    }


}
