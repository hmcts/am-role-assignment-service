package uk.gov.hmcts.reform.roleassignment.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
public class RoleAssignmentRequest {
    public RoleRequest roleRequest;
    public Collection<RequestedRole> requestedRoles;
}
