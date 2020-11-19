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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Service
public class PrepareResponseService {

    public ResponseEntity<Object> prepareCreateRoleResponse(AssignmentRequest roleAssignmentRequest) {

       // set clientId null to avoid the it to expose in response
        roleAssignmentRequest.getRequest().setClientId(null);


        if (roleAssignmentRequest.getRequest().getStatus().equals(Status.REJECTED)) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(
                roleAssignmentRequest);
        } else {
            return ResponseEntity.status(HttpStatus.CREATED).body(new RoleAssignmentRequestResource(
                roleAssignmentRequest));
        }

    }


    public ResponseEntity<Object> prepareRetrieveRoleResponse(List<RoleAssignment> roleAssignmentResponse,
                                                              UUID actorId)  {
        return ResponseEntity.status(HttpStatus.OK).body(new RoleAssignmentResource(roleAssignmentResponse, actorId));
    }


}
