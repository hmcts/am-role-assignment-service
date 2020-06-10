package uk.gov.hmcts.reform.roleassignment.helper;

import lombok.Setter;
import uk.gov.hmcts.reform.roleassignment.domain.model.Request;
import uk.gov.hmcts.reform.roleassignment.domain.model.RequestedRole;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RequestType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

@Setter
public class TestDataBuilder {

    private TestDataBuilder() {
        //not meant to be instantiated.
    }

    public static AssignmentRequest buildAssignmentRequest() {
        LocalDateTime timeStamp = LocalDateTime.now();
        return new AssignmentRequest(buildRequest(),buildRequestedRoleCollection());
    }

    public static Request buildRequest() {
        LocalDateTime timeStamp = LocalDateTime.now();
        return Request.builder().id(UUID.fromString("21334a2b-79ce-44eb-9168-2d49a744be9c")).correlationId(
            "correlationId").clientId("clientId").authenticatedUserId("userId").requestorId("requestorId").requestType(
            RequestType.CREATE).status(Status.APPROVED).process("process").reference("reference").replaceExisting(true).roleAssignmentId(
            "roleAssignmentId").timestamp(timeStamp).build();
    }

    //TODO update this
    private static RequestedRole buildRequestedRole() {
        return new RequestedRole(Status.APPROVED, "log"); //minimum
    }

    public static Collection<RequestedRole> buildRequestedRoleCollection() {
        Collection<RequestedRole> requestedRoles = new ArrayList<>();
        requestedRoles.add(buildRequestedRole());
        return requestedRoles;
    }



}
