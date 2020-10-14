package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.Request;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentRequestResource;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentResource;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Service
public class PrepareResponseService {

    private ParseRequestService parseRequestService;

    public PrepareResponseService(ParseRequestService parseRequestService) {
        this.parseRequestService = parseRequestService;
    }

    public ResponseEntity<Object> prepareCreateRoleResponse(AssignmentRequest roleAssignmentRequest) {

        updateRoleRequestResponse(roleAssignmentRequest);
        updateRequestedRolesResponse(roleAssignmentRequest);

        if (roleAssignmentRequest.getRequest().getStatus().equals(Status.REJECTED)) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .header(Constants.CORRELATION_ID_HEADER_NAME, parseRequestService.getCorrelationId())
                .body(roleAssignmentRequest);
        } else {
            return ResponseEntity.status(HttpStatus.CREATED)
                .header(Constants.CORRELATION_ID_HEADER_NAME, parseRequestService.getCorrelationId())
                .body(new RoleAssignmentRequestResource(roleAssignmentRequest));
        }

    }

    private void updateRoleRequestResponse(AssignmentRequest roleAssignmentRequest) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        Request roleRequest = roleAssignmentRequest.getRequest();
        Map<String, Object> requestMetaData = mapper.convertValue(
            roleRequest,
            new TypeReference<Map<String, Object>>() {
            }
        );
        requestMetaData.remove("clientId");
        roleAssignmentRequest.setRequest(mapper.convertValue(requestMetaData, Request.class));
    }

    public ResponseEntity<Object> prepareRetrieveRoleResponse(List<RoleAssignment> roleAssignmentResponse,
                                                              UUID actorId)  {
        return ResponseEntity.status(HttpStatus.OK)
            .header(Constants.CORRELATION_ID_HEADER_NAME, parseRequestService.getCorrelationId())
            .body(new RoleAssignmentResource(roleAssignmentResponse, actorId));
    }

    private void updateRequestedRolesResponse(AssignmentRequest roleAssignmentRequest) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        List<RoleAssignment> requestedRoles = new ArrayList<>();
        for (RoleAssignment requestedRole : roleAssignmentRequest.getRequestedRoles()) {
            Map<String, Object> requestedRoleMetaData = mapper.convertValue(
                requestedRole,
                new TypeReference<Map<String, Object>>() {
                }
            );
            requestedRoleMetaData.remove("approved");
            requestedRoleMetaData.remove("rejected");
            requestedRoleMetaData.remove("request");
            requestedRoles.add(mapper.convertValue(requestedRoleMetaData, RoleAssignment.class));
        }
        roleAssignmentRequest.setRequestedRoles(requestedRoles);
    }

    /*public void addHateoasLinks(Optional<?> payload, UUID roleAssignmentRequestId) {
        if (payload.isPresent()) {
            Object obj = payload.get();
            if (obj instanceof RoleAssignmentRequestResource) {
                ((RoleAssignmentRequestResource) obj).addLinks(roleAssignmentRequestId);
            }

        }

    }*/
}
