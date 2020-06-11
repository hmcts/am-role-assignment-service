package uk.gov.hmcts.reform.roleassignment.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
public class AssignmentRequest {
    public Request request;
    public Collection<RequestedRole> requestedRoles;

    @JsonCreator
    public AssignmentRequest(@JsonProperty(value = "roleRequest") Request request,
                             @JsonProperty(value = "requestedRoles") Collection<RequestedRole> requestedRolesCollection) {
        this.request = request;
        this.requestedRoles.addAll(requestedRolesCollection);

    }
}
