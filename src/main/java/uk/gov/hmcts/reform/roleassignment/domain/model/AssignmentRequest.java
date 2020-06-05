package uk.gov.hmcts.reform.roleassignment.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
public class AssignmentRequest {
    public Request request;
    public Collection<RequestedRole> requestedRoles;
}
