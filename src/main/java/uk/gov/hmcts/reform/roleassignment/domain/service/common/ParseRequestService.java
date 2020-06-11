package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.util.ValidationUtil;

@Service
public class ParseRequestService {

    public boolean parseRequest(AssignmentRequest assignmentRequest) {

        ValidationUtil.validateRoleRequest(assignmentRequest.getRequest());
        ValidationUtil.validateRequestedRoles(assignmentRequest.getRequestedRoles());
        return true;
    }
}
