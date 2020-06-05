package uk.gov.hmcts.reform.roleassignment.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;

@Getter
@Setter
@NoArgsConstructor
public class RequestedRole extends RoleAssignment {

    // Fields for the role assignment status record
    public Status status;
    public String log;


    public void approve(String message) {
        if (!status.equals(Status.REJECTED)) {
            status = Status.APPROVED;
            log(message);
        }
    }

    public boolean isApproved() {
        return status.equals(Status.APPROVED);
    }

    public void reject(String message) {
        status = Status.REJECTED;
        log(message);
    }

    public boolean isRejected() {
        return status.equals(Status.REJECTED);
    }

    public void log(String message) {
        log += message + "\n";
    }
}
