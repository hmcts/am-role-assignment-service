package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.util.ValidationUtil;

@Service
public class ParseRequestService {

    public boolean parseRequest(AssignmentRequest assignmentRequest) {
        //1. validate request and assignment record
        ValidationUtil.validateRoleRequest(assignmentRequest.getRequest());
        ValidationUtil.validateRequestedRoles(assignmentRequest.getRequestedRoles());
        return Boolean.TRUE;

        //2. Request Parsing
        //a. Extract client Id and place in the request
        //b. Extract AuthenticatedUser Id from the User token and place in the request.
        //c. Set Status=Created and created Time = now
        //d. correlationId if it is empty then generate a new value and set.
        //3. RoleAssignment Parsing
        //a. Copy process and reference from the request to RoleAssignment
        //b. Set Status=Created and statusSequenceNumber from Status Enum
        //c. created Time = now
    }
}
