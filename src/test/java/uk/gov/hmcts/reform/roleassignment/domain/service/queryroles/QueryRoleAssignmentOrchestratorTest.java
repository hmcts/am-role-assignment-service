package uk.gov.hmcts.reform.roleassignment.domain.service.queryroles;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.roleassignment.domain.model.Assignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.MultipleQueryRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.QueryRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentResource;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QueryRoleAssignmentOrchestratorTest {

    @Mock
    private PersistenceService persistenceServiceMock;

    @InjectMocks
    private QueryRoleAssignmentOrchestrator sut;


    @Test
    void should_PostRoleAssignmentsQueryByRequest_EmptyResult() {

        // GIVEN
        List<String> actorId = List.of(
            "123e4567-e89b-42d3-a456-556642445678",
            "4dc7dd3c-3fb5-4611-bbde-5101a97681e1"
        );
        List<String> roleType = List.of("CASE", "ORGANISATION");

        QueryRequest queryRequest = QueryRequest.builder()
            .actorId(actorId)
            .roleType(roleType)
            .build();

        // set up mocks for empty result: although it is the default mock behaviour
        when(persistenceServiceMock.retrieveRoleAssignmentsByQueryRequest(queryRequest,
                                                                          1,
                                                                          2,
                                                                          "id",
                                                                          "asc",
                                                                          false))
            .thenReturn(Collections.emptyList());
        when(persistenceServiceMock.getTotalRecords()).thenReturn(0L);

        // WHEN
        ResponseEntity<RoleAssignmentResource> result = sut.retrieveRoleAssignmentsByQueryRequest(queryRequest,
                                                                                                  1,
                                                                                                  2,
                                                                                                  "id",
                                                                                                  "asc",
                                                                                                  true);

        // THEN
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(0, result.getBody().getRoleAssignmentResponse().size());
        assertTrue(result.getHeaders().containsKey("Total-Records"));
        assertEquals("0", result.getHeaders().getOrEmpty("Total-Records").get(0));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldConditionallyAddRoleLabel_PostRoleAssignmentsQueryByRequest(Boolean includeLabels) {

        // GIVEN
        List<String> actorId = List.of("123e4567-e89b-42d3-a456-556642445678");
        List<String> roleType = List.of("CASE", "ORGANISATION");

        QueryRequest queryRequest = QueryRequest.builder()
            .actorId(actorId)
            .roleType(roleType)
            .build();

        RoleAssignment roleAssignment1 = TestDataBuilder.buildRoleAssignment(Status.LIVE);
        roleAssignment1.setRoleType(RoleType.ORGANISATION);
        RoleAssignment roleAssignment2 = TestDataBuilder.buildRoleAssignment(Status.LIVE);
        roleAssignment2.setRoleType(RoleType.ORGANISATION);

        when(persistenceServiceMock.retrieveRoleAssignmentsByQueryRequest(queryRequest,
                                                                          1,
                                                                          2,
                                                                          "id",
                                                                          "asc",
                                                                          false))
            .thenReturn(List.of(roleAssignment1, roleAssignment2));
        when(persistenceServiceMock.getTotalRecords()).thenReturn(2L);

        // WHEN
        ResponseEntity<RoleAssignmentResource> result = sut.retrieveRoleAssignmentsByQueryRequest(queryRequest,
                                                                                                  1,
                                                                                                  2,
                                                                                                  "id",
                                                                                                  "asc",
                                                                                                  includeLabels);

        // THEN
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(2, result.getBody().getRoleAssignmentResponse().size());
        assertTrue(result.getHeaders().containsKey("Total-Records"));
        assertEquals("2", result.getHeaders().getOrEmpty("Total-Records").get(0));

        for (Assignment roleAssignment : result.getBody().getRoleAssignmentResponse()) {
            if (includeLabels) {
                assertEquals("Judge", roleAssignment.getRoleLabel());
            } else {
                assertNull(roleAssignment.getRoleLabel());
            }
        }
    }

    @Test
    void shouldNotAddRoleLabelWhenRoleConfigRoleNull_PostRoleAssignmentsQueryByRequest() {

        // GIVEN
        List<String> actorId = List.of("123e4567-e89b-42d3-a456-556642445678");
        List<String> roleType = List.of("CASE", "ORGANISATION");

        QueryRequest queryRequest = QueryRequest.builder()
            .actorId(actorId)
            .roleType(roleType)
            .build();

        // roleName == "judge" roleCategory == "JUDICIAL" roleType == "CASE"
        // doesn't exist in `roleconfig`
        RoleAssignment roleAssignment = TestDataBuilder.buildRoleAssignment(Status.LIVE);

        when(persistenceServiceMock.retrieveRoleAssignmentsByQueryRequest(queryRequest,
                                                                          1,
                                                                          2,
                                                                          "id",
                                                                          "asc",
                                                                          false))
            .thenReturn(List.of(roleAssignment));
        when(persistenceServiceMock.getTotalRecords()).thenReturn(1L);

        // WHEN
        ResponseEntity<RoleAssignmentResource> result = sut.retrieveRoleAssignmentsByQueryRequest(queryRequest,
                                                                                                  1,
                                                                                                  2,
                                                                                                  "id",
                                                                                                  "asc",
                                                                                                  true);

        // THEN
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().getRoleAssignmentResponse().size());
        assertTrue(result.getHeaders().containsKey("Total-Records"));
        assertEquals("1", result.getHeaders().getOrEmpty("Total-Records").get(0));

        String resultRoleLabel = result.getBody().getRoleAssignmentResponse().get(0).getRoleLabel();
        assertNull(resultRoleLabel);
    }

    @Test
    void shouldFail_PostRoleAssignmentsQueryByRequest() {

        // GIVEN
        QueryRequest queryRequest = QueryRequest.builder()
            .build();

        // WHEN / THEN
        Assertions.assertThrows(BadRequestException.class, () -> sut.retrieveRoleAssignmentsByQueryRequest(queryRequest,
                                                                                                          1,
                                                                                                          2,
                                                                                                          "id",
                                                                                                          "asc",
                                                                                                           true));
    }

    @Test
    void should_PostRoleAssignmentByMultipleQueryRequest_EmptyResult() {

        // GIVEN
        List<String> actorId = List.of(
            "123e4567-e89b-42d3-a456-556642445678",
            "4dc7dd3c-3fb5-4611-bbde-5101a97681e1"
        );
        List<String> roleType = List.of("CASE", "ORGANISATION");

        QueryRequest queryRequest = QueryRequest.builder()
            .actorId(actorId)
            .roleType(roleType)
            .build();
        MultipleQueryRequest multipleQueryRequest =  MultipleQueryRequest.builder()
            .queryRequests(List.of(queryRequest))
            .build();

        // set up mocks for empty result: although it is the default mock behaviour
        when(persistenceServiceMock.retrieveRoleAssignmentsByMultipleQueryRequest(multipleQueryRequest,
                                                                                  1,
                                                                                  2,
                                                                                  "id",
                                                                                  "asc",
                                                                                  false))
            .thenReturn(Collections.emptyList());
        when(persistenceServiceMock.getTotalRecords()).thenReturn(0L);

        // WHEN
        ResponseEntity<RoleAssignmentResource> result = sut
            .retrieveRoleAssignmentsByMultipleQueryRequest(multipleQueryRequest,
                                                            1,
                                                             2,
                                                              "id",
                                                              "asc",
                                                           true);

        // THEN
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(0, result.getBody().getRoleAssignmentResponse().size());
        assertTrue(result.getHeaders().containsKey("Total-Records"));
        assertEquals("0", result.getHeaders().getOrEmpty("Total-Records").get(0));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldConditionallyAddRoleLabel_PostRoleAssignmentsByMultipleQueryRequest(Boolean includeLabels) {

        // GIVEN
        List<String> actorId = List.of("123e4567-e89b-42d3-a456-556642445678");
        List<String> roleType = List.of("CASE", "ORGANISATION");

        QueryRequest queryRequest = QueryRequest.builder()
            .actorId(actorId)
            .roleType(roleType)
            .build();
        MultipleQueryRequest multipleQueryRequest =  MultipleQueryRequest.builder()
            .queryRequests(List.of(queryRequest))
            .build();
        RoleAssignment roleAssignment1 = TestDataBuilder.buildRoleAssignment(Status.LIVE);
        roleAssignment1.setRoleType(RoleType.ORGANISATION);
        RoleAssignment roleAssignment2 = TestDataBuilder.buildRoleAssignment(Status.LIVE);
        roleAssignment2.setRoleType(RoleType.ORGANISATION);

        when(persistenceServiceMock.retrieveRoleAssignmentsByMultipleQueryRequest(multipleQueryRequest,
                                                                                  1,
                                                                                  2,
                                                                                  "id",
                                                                                  "asc",
                                                                                  false))
            .thenReturn(List.of(roleAssignment1, roleAssignment2));
        when(persistenceServiceMock.getTotalRecords()).thenReturn(2L);

        // WHEN
        ResponseEntity<RoleAssignmentResource> result = sut
            .retrieveRoleAssignmentsByMultipleQueryRequest(multipleQueryRequest,
                                                           1,
                                                           2,
                                                           "id",
                                                           "asc",
                                                           includeLabels);

        // THEN
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(2, result.getBody().getRoleAssignmentResponse().size());
        assertTrue(result.getHeaders().containsKey("Total-Records"));
        assertEquals("2", result.getHeaders().getOrEmpty("Total-Records").get(0));

        for (Assignment roleAssignment : result.getBody().getRoleAssignmentResponse()) {
            if (includeLabels) {
                assertEquals("Judge", roleAssignment.getRoleLabel());
            } else {
                assertNull(roleAssignment.getRoleLabel());
            }
        }
    }

    @Test
    void shouldFail_PostRoleAssignmentsQueryByMultipleRequest() {

        // GIVEN
        QueryRequest queryRequest = QueryRequest.builder()
            .build();
        MultipleQueryRequest multipleQueryRequest =  MultipleQueryRequest.builder()
            .queryRequests(List.of(queryRequest))
            .build();

        // WHEN / THEN
        Assertions.assertThrows(BadRequestException.class, () ->
            sut.retrieveRoleAssignmentsByMultipleQueryRequest(multipleQueryRequest,
                                                              1,
                                                              2,
                                                              "id",
                                                              "asc",
                                                              true));
    }

}
