package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.roleassignment.domain.model.RequestedRole;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentRequestResource;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleRequest;

import java.util.*;


public class PrepareResponseService {

    private static final Logger LOG = LoggerFactory.getLogger(PrepareResponseService.class);

    private PrepareResponseService() {
    }

    public static ResponseEntity<Object> prepareCreateRoleResponse(RoleAssignmentRequest roleAssignmentRequest) {
        LOG.info(" ----- prepareCreateRoleResponse : {}", roleAssignmentRequest);

        updateRoleRequestResponse(roleAssignmentRequest);
        updateRequestedRolesResponse(roleAssignmentRequest);
        return ResponseEntity.status(HttpStatus.OK).body(new RoleAssignmentRequestResource(roleAssignmentRequest));
    }

    private static void updateRoleRequestResponse(RoleAssignmentRequest roleAssignmentRequest) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        RoleRequest roleRequest = roleAssignmentRequest.getRoleRequest();
        Map<String, Object> requestMetaData = mapper.convertValue(
            roleRequest,
            new TypeReference<Map<String, Object>>() {
            }
        );
        requestMetaData.remove("clientId");
        roleAssignmentRequest.setRoleRequest(mapper.convertValue(requestMetaData, RoleRequest.class));
    }

    private static void updateRequestedRolesResponse(RoleAssignmentRequest roleAssignmentRequest) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        List<RequestedRole> requestedRoles = new ArrayList<>();
        for (RequestedRole requestedRole : roleAssignmentRequest.requestedRoles) {
            Map<String, Object> requestedRoleMetaData = mapper.convertValue(
                requestedRole,
                new TypeReference<Map<String, Object>>() {
                }
            );
            requestedRoleMetaData.remove("grantType");
            requestedRoleMetaData.remove("approved");
            requestedRoleMetaData.remove("rejected");
            requestedRoles.add(mapper.convertValue(requestedRoleMetaData, RequestedRole.class));
        }
        roleAssignmentRequest.setRequestedRoles(requestedRoles);
    }

    private static ResponseEntity<Object> prepareResponse(RoleAssignmentRequest roleAssignmentRequest) {
        return ResponseEntity.status(HttpStatus.OK).body(roleAssignmentRequest);
    }


    public static void addHateoasLinks(Optional<?> payload, UUID roleAssignmentRequestId) {
        if (payload.isPresent()) {
            Object obj = payload.get();
            if (obj instanceof RoleAssignmentRequestResource) {
                ((RoleAssignmentRequestResource) obj).addLinks(roleAssignmentRequestId);
            }

        }

    }
}
