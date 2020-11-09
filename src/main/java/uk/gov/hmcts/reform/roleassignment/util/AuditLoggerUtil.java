package uk.gov.hmcts.reform.roleassignment.util;

import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentRequestResource;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentResource;

import javax.annotation.CheckForNull;
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

    @CheckForNull
    public static List<UUID> buildAssignmentIds(final ResponseEntity<RoleAssignmentRequestResource> response) {
        if (response.getBody() != null && response.getBody() instanceof RoleAssignmentRequestResource) {
            return response.getBody().getRoleAssignmentRequest().getRequestedRoles().stream().limit(10)
                .map(RoleAssignment::getId)
                .collect(Collectors.toList());
        }
        return List.of();
    }

    public static List<String> buildActorIds(final ResponseEntity<RoleAssignmentRequestResource> response) {
        if (response.getBody() != null && response.getBody() instanceof RoleAssignmentRequestResource) {
            RoleAssignmentRequestResource roleAssignmentRequestResource = response.getBody();
            if (roleAssignmentRequestResource != null) {
                return roleAssignmentRequestResource.getRoleAssignmentRequest().getRequestedRoles().stream().limit(10)
                    .map(RoleAssignment::getActorId)
                    .collect(Collectors.toList());
            }
        }
        return List.of();
    }

    @CheckForNull
    public static List<String> buildRoleNames(final ResponseEntity<RoleAssignmentRequestResource> response) {
        if (response.getBody() != null && response.getBody() instanceof RoleAssignmentRequestResource) {
            return response.getBody().getRoleAssignmentRequest().getRequestedRoles().stream().limit(10)
                .map(RoleAssignment::getRoleName)
                .collect(Collectors.toList());
        }
        return List.of();
    }

    @CheckForNull
    public static Set<String> buildCaseIds(final ResponseEntity<RoleAssignmentRequestResource> response) {
        Set<String> caseIds = new HashSet<>();
        if (response.getBody() != null && response.getBody() instanceof RoleAssignmentRequestResource) {

            response.getBody().getRoleAssignmentRequest().getRequestedRoles()
                .stream().map(RoleAssignment::getAttributes).forEach(obj -> obj.forEach((key, value) -> {
                    if (key.equals("caseId")) {
                        caseIds.add(value.asText());
                    }
                }));

        }
        return caseIds;
    }

    public static List<UUID> getAssignmentIds(final ResponseEntity<RoleAssignmentResource> response) {
        RoleAssignmentResource roleAssignmentResource = response.getBody();
        if (roleAssignmentResource != null) {
            return roleAssignmentResource.getRoleAssignmentResponse().stream().limit(10)
                .map(RoleAssignment::getId)
                .collect(Collectors.toList());
        }
        return List.of();
    }

    public static List<String> getActorIds(final ResponseEntity<RoleAssignmentResource> response) {
        RoleAssignmentResource roleAssignmentResource = response.getBody();
        if (roleAssignmentResource != null) {
            return roleAssignmentResource.getRoleAssignmentResponse().stream().limit(10)
                .map(RoleAssignment::getActorId)
                .collect(Collectors.toList());
        }
        return List.of();
    }

    public static List<UUID> searchAssignmentIds(final ResponseEntity<List<RoleAssignment>> response) {
        List<RoleAssignment> roleAssignmentResource = response.getBody();
        if (roleAssignmentResource != null) {
            return roleAssignmentResource.stream().limit(10)
                .map(RoleAssignment::getId)
                .collect(Collectors.toList());
        }
        return List.of();
    }
}
