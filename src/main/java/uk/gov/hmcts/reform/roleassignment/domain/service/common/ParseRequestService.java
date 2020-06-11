package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.RequestedRole;
import uk.gov.hmcts.reform.roleassignment.util.ValidationUtil;

@Service
public class ParseRequestService {

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

            ValidationUtil.validateNumberTextField(requestedRole.getAttributes().get("attributes").get("jurisdiction").asText());
            ValidationUtil.validateNumberTextField(requestedRole.getAttributes().get("attributes").get("region").asText());
            ValidationUtil.validateNumberTextField(requestedRole.getAttributes().get("attributes").get("contractType").asText());
        }
        return Boolean.TRUE;
    }
}
