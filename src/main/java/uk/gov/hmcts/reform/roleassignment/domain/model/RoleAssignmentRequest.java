package uk.gov.hmcts.reform.roleassignment.domain.model;

import java.util.Collection;

public class RoleAssignmentRequest {
    public RoleRequest roleRequest;
    public Collection<RequestedRole> requestedRoles;
}
