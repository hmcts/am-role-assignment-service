package uk.gov.hmcts.reform.roleassignment.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

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
        this.requestedRoles = requestedRolesCollection;

    }
}
