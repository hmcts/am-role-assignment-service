package uk.gov.hmcts.reform.roleassignment.helper;

import lombok.Setter;
import org.bouncycastle.cert.ocsp.Req;
import uk.gov.hmcts.reform.roleassignment.domain.model.RequestedRole;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RequestType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
