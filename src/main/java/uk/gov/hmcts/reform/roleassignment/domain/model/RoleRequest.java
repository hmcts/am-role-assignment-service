package uk.gov.hmcts.reform.roleassignment.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RequestType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoleRequest {
    public String id;
    public String correlationId;
    public String clientId;
    public String authenticatedUserId;
    public String requestorId;
    public RequestType requestType;
    public Status status;
    public String process;
    public String reference;
    public boolean replaceExisting;
    public String roleAssignmentId;
    public LocalDateTime timestamp;
}
