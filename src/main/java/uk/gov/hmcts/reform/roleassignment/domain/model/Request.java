package uk.gov.hmcts.reform.roleassignment.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RequestType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Request {
    public UUID id;
    public String correlationId;
    public String clientId;
    public UUID authenticatedUserId;
    public UUID requestorId;
    public RequestType requestType;
    public Status status;
    public String process;
    public String reference;
    public boolean replaceExisting;
    public String roleAssignmentId;
    public LocalDateTime created;
    public LocalDateTime lastUpdateTime;
    public String log;

    @JsonCreator
    public Request(@JsonProperty(value = "correlationId") String correlationId,
                   @JsonProperty(value = "clientId") String clientId,
                   @JsonProperty(value = "authenticatedUserId") UUID authenticatedUserId,
                   @JsonProperty(value = "requestorId") UUID requestorId,
                   @JsonProperty(value = "requestType") RequestType requestType,
                   @JsonProperty(value = "status") Status status,
                   @JsonProperty(value = "process") String process,
                   @JsonProperty(value = "reference") String reference,
                   @JsonProperty(value = "replaceExisting") boolean replaceExisting,
                   @JsonProperty(value = "roleAssignmentId") String roleAssignmentId,
                   @JsonProperty(value = "created") LocalDateTime created) {
        this.correlationId = correlationId;
        this.clientId = clientId;
        this.authenticatedUserId = authenticatedUserId;
        this.requestorId = requestorId;
        this.requestType = requestType;
        this.status = status;
        this.process = process;
        this.reference = reference;
        this.replaceExisting = replaceExisting;
        this.roleAssignmentId = roleAssignmentId;
        this.created = created;
    }
}
