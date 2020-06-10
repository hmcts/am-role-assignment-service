package uk.gov.hmcts.reform.roleassignment.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
public class AssignmentRequest {
    public Request request;
    public Collection<RequestedRole> requestedRoles = new ArrayList<>();

    @JsonCreator
    public AssignmentRequest(@JsonProperty(value = "roleRequest") Request request,
                             @JsonProperty(value = "requestedRoles") Collection<RequestedRole> requestedRolesCollection) {
        this.request = request;
        this.requestedRoles.addAll(requestedRolesCollection);

    }
}
