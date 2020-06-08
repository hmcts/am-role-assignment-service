package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.util.ValidationUtil;

@Service
public class ParseRequestService {
    //1. Validate incoming data
    //2. Mapping to model objects

    public boolean parseRequest(AssignmentRequest assignmentRequest) {

        //add some conditioning for failure
        ValidationUtil.validate(assignmentRequest.request.authenticatedUserId);
        ValidationUtil.validate(assignmentRequest.request.clientId);
        ValidationUtil.validate(assignmentRequest.request.correlationId);
        ValidationUtil.validate(assignmentRequest.request.process);
        ValidationUtil.validate(assignmentRequest.request.reference);
        ValidationUtil.validate(assignmentRequest.request.requestorId);
        ValidationUtil.validate(assignmentRequest.request.roleAssignmentId);
        //ValidationUtil.validateLists(assignmentRequest.requestedRoles.toArray());

        return Boolean.TRUE;

    }
}
