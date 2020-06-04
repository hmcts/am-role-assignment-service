package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.util.ValidationUtil;

@Service
public class ParseRequestService {
    //1. Validate incoming data
    //2. Mapping to model objects

    public boolean parseRequest(RoleAssignmentRequest roleAssignmentRequest) {

        //add some conditioning for failure
        ValidationUtil.validate(roleAssignmentRequest.roleRequest.authenticatedUserId);
        ValidationUtil.validate(roleAssignmentRequest.roleRequest.clientId);
        ValidationUtil.validate(roleAssignmentRequest.roleRequest.correlationId);
        ValidationUtil.validate(roleAssignmentRequest.roleRequest.process);
        ValidationUtil.validate(roleAssignmentRequest.roleRequest.reference);
        ValidationUtil.validate(roleAssignmentRequest.roleRequest.requestorId);
        ValidationUtil.validate(roleAssignmentRequest.roleRequest.roleAssignmentId);
        ValidationUtil.validateLists(roleAssignmentRequest.requestedRoles);

        return Boolean.TRUE;

    }
}
