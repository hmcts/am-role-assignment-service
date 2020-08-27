package uk.gov.hmcts.reform.roleassignment.util;

import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentRequestResource;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentResource;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Named
@Singleton
public class AuditLoggerUtil {

    private AuditLoggerUtil() {

    }

    public static List<UUID> buildAssignmentIds(ResponseEntity<RoleAssignmentRequestResource> response) {
        return response.getBody().getRoleAssignmentRequest().getRequestedRoles().stream().limit(10)
            .map(RoleAssignment::getId)
            .collect(Collectors.toList());
    }

    public static List<UUID> buildActorIds(ResponseEntity<RoleAssignmentRequestResource> response) {
        return response.getBody().getRoleAssignmentRequest().getRequestedRoles().stream().limit(10)
            .map(RoleAssignment::getActorId)
            .collect(Collectors.toList());
    }

    public static List<String> buildRoleNames(ResponseEntity<RoleAssignmentRequestResource> response) {
        return response.getBody().getRoleAssignmentRequest().getRequestedRoles().stream().limit(10)
            .map(RoleAssignment::getRoleName)
            .collect(Collectors.toList());
    }

    public static Set<String> buildCaseIds(ResponseEntity<RoleAssignmentRequestResource> response) {
        Set<String> caseIds = new HashSet<>();
        response.getBody().getRoleAssignmentRequest().getRequestedRoles().stream()
            .map(RoleAssignment::getAttributes).forEach(obj -> obj.forEach((key, value) -> {
                if (key.equals("caseId")) {
                    caseIds.add(value.asText());
                }

            }));
        return caseIds;
    }

    public static List<UUID> getAssignmentIds(ResponseEntity<RoleAssignmentResource> response) {
        return response.getBody().getRoleAssignmentResponse().stream().limit(10)
            .map(RoleAssignment::getId)
            .collect(Collectors.toList());
    }

    public static List<UUID> getActorIds(ResponseEntity<RoleAssignmentResource> response) {
        return response.getBody().getRoleAssignmentResponse().stream().limit(10)
            .map(RoleAssignment::getActorId)
            .collect(Collectors.toList());
    }

    public static List<UUID> searchAssignmentIds(ResponseEntity<List<RoleAssignment>> response) {
        return response.getBody().stream().limit(10)
            .map(RoleAssignment::getId)
            .collect(Collectors.toList());
    }
}
