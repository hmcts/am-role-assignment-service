package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.RequestedRole;
import uk.gov.hmcts.reform.roleassignment.util.ValidationUtil;

@Service
public class ParseRequestService {
    //1. Validate incoming data
    //2. Mapping to model objects

    public boolean parseRequest(AssignmentRequest assignmentRequest) {
        ValidationUtil.validateNumberTextField(assignmentRequest.request.correlationId);
        ValidationUtil.validateNumberTextField(assignmentRequest.request.clientId);
        ValidationUtil.validateNumberTextField(assignmentRequest.request.authenticatedUserId);
        ValidationUtil.validateNumberTextField(assignmentRequest.request.requestorId);
        ValidationUtil.validateTextField(assignmentRequest.request.requestType.toString());

        ValidationUtil.validateLists(assignmentRequest.requestedRoles);

        for (RequestedRole requestedRole: assignmentRequest.requestedRoles) {
            ValidationUtil.validateUuidField(requestedRole.getId());
            ValidationUtil.validateUuidField(requestedRole.getActorId());

            ValidationUtil.validateNumberTextField(requestedRole.getActorIdType().toString());
            ValidationUtil.validateNumberTextField(requestedRole.getRoleType().toString());
            ValidationUtil.validateNumberTextField(requestedRole.getRoleName());
            ValidationUtil.validateNumberTextField(requestedRole.getClassification().toString());
            ValidationUtil.validateNumberTextField(requestedRole.getGrantType().toString());

            //ValidationUtil.validateNumberTextField(requestedRole.getAttributes().get("jurisdiction").toString());
            //ValidationUtil.validateNumberTextField(requestedRole.getAttributes().get("region").toString());
            //ValidationUtil.validateNumberTextField(requestedRole.getAttributes().get("contractType").toString());
        }

        return Boolean.TRUE;
    }

    public boolean isEmptyValue(String value) {

        boolean isEmpty = false;
        if (value != null && value.trim().isEmpty()) {
            isEmpty = true;
        }
        return isEmpty;
    }
}
