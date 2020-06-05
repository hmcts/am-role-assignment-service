package uk.gov.hmcts.reform.roleassignment.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RequestType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RoleAssignmentRequest {
    public RoleRequest roleRequest = new RoleRequest();
    public List<RequestedRole> requestedRoles = new ArrayList<>();

    @JsonCreator
    public RoleAssignmentRequest(@JsonProperty("correlationId") String correlationId,
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
                                 @JsonProperty(value = "requestedRoles") List<RequestedRole> requestedRolesCollection) {
        roleRequest.setCorrelationId(correlationId);
        roleRequest.setClientId(clientId);
        roleRequest.setAuthenticatedUserId(authenticatedUserId);
        roleRequest.setRequestorId(requestorId);
        roleRequest.setRequestType(requestType);
        roleRequest.setStatus(status);
        roleRequest.setProcess(process);
        roleRequest.setReference(reference);
        roleRequest.setReplaceExisting(replaceExisting);
        roleRequest.setRoleAssignmentId(roleAssignmentId);
        roleRequest.setTimestamp(timestamp);
        this.requestedRoles.addAll(requestedRolesCollection); //maybe not correct, need more info on format

    }
}
