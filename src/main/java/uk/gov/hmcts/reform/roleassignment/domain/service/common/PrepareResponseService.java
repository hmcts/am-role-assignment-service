package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.Request;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentRequestResource;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentResource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;


@Service
public class PrepareResponseService {

    private static final Logger LOG = LoggerFactory.getLogger(PrepareResponseService.class);

    private PrepareResponseService() {
    }

    public ResponseEntity<Object> prepareCreateRoleResponse(AssignmentRequest roleAssignmentRequest) {
        LOG.info(" ----- prepareCreateRoleResponse : {}", roleAssignmentRequest);

        updateRoleRequestResponse(roleAssignmentRequest);
        updateRequestedRolesResponse(roleAssignmentRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(new RoleAssignmentRequestResource(roleAssignmentRequest));
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
                                                                     UUID actorId) throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(new RoleAssignmentResource(roleAssignmentResponse, actorId));
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

    private ResponseEntity<Object> prepareResponse(AssignmentRequest roleAssignmentRequest) {
        return ResponseEntity.status(HttpStatus.OK).body(roleAssignmentRequest);
    }


    public void addHateoasLinks(Optional<?> payload, UUID roleAssignmentRequestId) {
        if (payload.isPresent()) {
            Object obj = payload.get();
            if (obj instanceof RoleAssignmentRequestResource) {
                ((RoleAssignmentRequestResource) obj).addLinks(roleAssignmentRequestId);
            }

        }

    }
}
