package uk.gov.hmcts.reform.roleassignment.util;


import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentRequestResource;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentResource;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.CREATED;

public class AuditLoggerUtilTest {

    private AssignmentRequest assignmentRequest;
    private RoleAssignmentRequestResource roleAssignmentRequestResource;
    private ResponseEntity<RoleAssignmentRequestResource> responseEntity;
    private ResponseEntity<RoleAssignmentResource> roleAssignmentResponseEntity;
    private RoleAssignmentResource roleAssignmentResource;

    @Before
    public void setUp() throws IOException {
        assignmentRequest = TestDataBuilder.buildAssignmentRequest(CREATED, CREATED,
                                                                   false
        );

        roleAssignmentRequestResource = new RoleAssignmentRequestResource(assignmentRequest);
        responseEntity = ResponseEntity.ok(roleAssignmentRequestResource);
        roleAssignmentResource = new RoleAssignmentResource();
        roleAssignmentResource.setRoleAssignmentResponse((List<RoleAssignment>) assignmentRequest.getRequestedRoles());
        roleAssignmentResponseEntity = ResponseEntity.ok(roleAssignmentResource);


    }

    @Test
    public void checkAssignmentIds() {

        List<UUID> expectedIds = Arrays.asList(
            UUID.fromString("9785c98c-78f2-418b-ab74-a892c3ccca9f"),
            UUID.fromString("9785c98c-78f2-418b-ab74-a892c3ccca9f")
        );

        List<UUID> assignmentIds = AuditLoggerUtil.buildAssignmentIds(responseEntity);
        assertNotNull(assignmentIds);
        assertThat(assignmentIds).isEqualTo(expectedIds);
    }

    @Test
    public void checkActorIds() {

        List<UUID> expectedIds = Arrays.asList(
            UUID.fromString("21334a2b-79ce-44eb-9168-2d49a744be9c"),
            UUID.fromString("21334a2b-79ce-44eb-9168-2d49a744be9c")
        );

        List<UUID> actorIds = AuditLoggerUtil.buildActorIds(responseEntity);
        assertNotNull(actorIds);
        assertThat(actorIds).isEqualTo(expectedIds);
    }

    @Test
    public void checkRoleNames() {

        List<String> expectedRoles = Arrays.asList(
            "judge",
            "judge"
        );

        List<String> roleNames = AuditLoggerUtil.buildRoleNames(responseEntity);
        assertNotNull(roleNames);
        assertThat(roleNames).isEqualTo(expectedRoles);
    }

    @Test
    public void checkCaseIds() {

        Set<String> expectedCaseIds = new HashSet<>();
        expectedCaseIds.add("1234567890123456");

        Set<String> caseIds = AuditLoggerUtil.buildCaseIds(responseEntity);
        assertNotNull(caseIds);
        assertThat(caseIds).isEqualTo(expectedCaseIds);
    }

    @Test
    public void shouldReturnAssignmentIds() {

        List<UUID> expectedIds = Arrays.asList(
            UUID.fromString("9785c98c-78f2-418b-ab74-a892c3ccca9f"),
            UUID.fromString("9785c98c-78f2-418b-ab74-a892c3ccca9f")
        );

        List<UUID> assignmentIds = AuditLoggerUtil.getAssignmentIds(roleAssignmentResponseEntity);
        assertNotNull(assignmentIds);
        assertThat(assignmentIds).isEqualTo(expectedIds);
    }

    @Test
    public void shouldReturnActorIds() {

        List<UUID> expectedActorIds = Arrays.asList(
            UUID.fromString("21334a2b-79ce-44eb-9168-2d49a744be9c"),
            UUID.fromString("21334a2b-79ce-44eb-9168-2d49a744be9c")
        );

        List<UUID> actorIds = AuditLoggerUtil.getActorIds(roleAssignmentResponseEntity);
        assertNotNull(actorIds);
        assertThat(actorIds).isEqualTo(expectedActorIds);
    }

    @Test
    public void shouldReturnAssignmentIdsForSearch() {
        List<UUID> expectedIds = Arrays.asList(
            UUID.fromString("9785c98c-78f2-418b-ab74-a892c3ccca9f"),
            UUID.fromString("9785c98c-78f2-418b-ab74-a892c3ccca9f")
        );

        ResponseEntity<List<RoleAssignment>> responseEntity = ResponseEntity
            .ok((List<RoleAssignment>) assignmentRequest.getRequestedRoles());
        List<UUID> assignmentIds = AuditLoggerUtil.searchAssignmentIds(responseEntity);
        assertNotNull(assignmentIds);
        assertThat(assignmentIds).isEqualTo(expectedIds);
    }
}
