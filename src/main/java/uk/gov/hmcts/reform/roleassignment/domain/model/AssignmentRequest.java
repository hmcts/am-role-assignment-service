package uk.gov.hmcts.reform.roleassignment.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RequestType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonCreator;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssignmentRequest {
    public Request request = new Request();
    public Collection<RequestedRole> requestedRoles = new ArrayList<>();

    @JsonCreator
    public AssignmentRequest(@JsonProperty(value = "correlationId") String correlationId,
                             @JsonProperty(value = "clientId") String clientId,
                             @JsonProperty(value = "authenticatedUserId") String authenticatedUserId,
                             @JsonProperty(value = "requestorId") String requestorId,
                             @JsonProperty(value = "requestType") RequestType requestType,
                             @JsonProperty(value = "status") Status status,
                             @JsonProperty(value = "process") String process,
                             @JsonProperty(value = "reference") String reference,
                             @JsonProperty(value = "replaceExisting") boolean replaceExisting,
                             @JsonProperty(value = "roleAssignmentId") String roleAssignmentId,
                             @JsonProperty(value = "timestamp") LocalDateTime timestamp,
                             @JsonProperty(value = "requestedRoles") Collection<RequestedRole> requestedRolesCollection) {
        request.setCorrelationId(correlationId);
        request.setClientId(clientId);
        request.setAuthenticatedUserId(authenticatedUserId);
        request.setRequestorId(requestorId);
        request.setRequestType(requestType);
        request.setStatus(status);
        request.setProcess(process);
        request.setReference(reference);
        request.setReplaceExisting(replaceExisting);
        request.setRoleAssignmentId(roleAssignmentId);
        request.setTimestamp(timestamp);
        this.requestedRoles.addAll(requestedRolesCollection); //maybe not correct, need more info on format

    }
}
