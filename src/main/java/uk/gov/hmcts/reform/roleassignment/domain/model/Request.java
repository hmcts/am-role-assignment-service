package uk.gov.hmcts.reform.roleassignment.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RequestType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Request {
    public UUID id;
    public String correlationId;
    public String clientId;
    public UUID authenticatedUserId;
    public String requestorId;
    public RequestType requestType;
    public Status status;
    public String process;
    public String reference;
    public boolean replaceExisting;
    public String roleAssignmentId;
    public LocalDateTime created;
    public LocalDateTime lastUpdateTime;
    public String log;
}
