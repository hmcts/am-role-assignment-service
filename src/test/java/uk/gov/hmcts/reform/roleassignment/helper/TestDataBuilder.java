package uk.gov.hmcts.reform.roleassignment.helper;

import lombok.Setter;
import uk.gov.hmcts.reform.roleassignment.domain.model.RequestedRole;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RequestType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
public class TestDataBuilder {

    private TestDataBuilder() {
        //not meant to be instantiated.
    }

    public static RoleAssignmentRequest buildRoleAssignmentRequest() {
        LocalDateTime timeStamp = LocalDateTime.now();
        List<RequestedRole> requestedRoles = new ArrayList<>();
        requestedRoles.add(buildRequestedRole());
        return new RoleAssignmentRequest("correlationId", "clientId", "userId",
                                         "requestorId", RequestType.CREATE, Status.APPROVED,
                                         "process", "reference", true,
                                         "roleAssignmentId", timeStamp, requestedRoles);

    }

    public static RequestedRole buildRequestedRole() {
        return new RequestedRole(Status.APPROVED, "log"); //minimum
    }


}
