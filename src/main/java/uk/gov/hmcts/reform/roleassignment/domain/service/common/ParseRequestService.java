package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.InvalidRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.util.ValidationUtil;

import static uk.gov.hmcts.reform.roleassignment.apihelper.Constants.*;

@Service
public class ParseRequestService {
    //1. Validate incoming data
    //2. Mapping to model objects

    public boolean parseRequest(AssignmentRequest assignmentRequest) {

        //patterns tbd
        parseCorrelationId(assignmentRequest.request.correlationId);
        //ValidationUtil.validate(assignmentRequest.request.correlationId, NUMBER_TEXT_PATTERN);
        //ValidationUtil.validate(assignmentRequest.request.clientId, NUMBER_TEXT_PATTERN);
        //ValidationUtil.validate(assignmentRequest.request.authenticatedUserId, NUMBER_TEXT_PATTERN);
        //ValidationUtil.validate(assignmentRequest.request.requestorId, NUMBER_TEXT_PATTERN);

        //ValidationUtil.validate(assignmentRequest.request.process, NUMBER_TEXT_PATTERN);
        //ValidationUtil.validate(assignmentRequest.request.reference, NUMBER_TEXT_PATTERN);
        //ValidationUtil.validate(assignmentRequest.request.roleAssignmentId, NUMBER_PATTERN);

        //ValidationUtil.validateLists(assignmentRequest.requestedRoles);

        //Collection<RequestedRole> requestedRoles =  assignmentRequest.getRequestedRoles();
        //requestedRoles.stream().forEach();

        return Boolean.TRUE;
    }

    private void parseCorrelationId(String field) {
        if (isEmptyValue(field) || field == null) {
            throw new InvalidRequest("Invalid Correlation ID value: " + field);
        } else {
            ValidationUtil.validate(field, NUMBER_TEXT_PATTERN);
        }
    }

    public boolean isEmptyValue(String value) {

        boolean isEmpty = false;
        if (value != null && value.trim().isEmpty()) {
            isEmpty = true;
        }
        return isEmpty;
    }
}
