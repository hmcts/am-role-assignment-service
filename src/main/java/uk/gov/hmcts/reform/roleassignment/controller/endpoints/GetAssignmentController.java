
package uk.gov.hmcts.reform.roleassignment.controller.endpoints;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.roleassignment.auditlog.LogAudit;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentResource;
import uk.gov.hmcts.reform.roleassignment.domain.service.getroles.RetrieveRoleAssignmentOrchestrator;
import uk.gov.hmcts.reform.roleassignment.v1.V1;

import java.io.IOException;

import static uk.gov.hmcts.reform.roleassignment.auditlog.AuditOperationType.GET_ASSIGNMENTS_BY_ACTOR;

@Slf4j
@Api(value = "roles")
@RestController
public class GetAssignmentController {

    private RetrieveRoleAssignmentOrchestrator retrieveRoleAssignmentService;

    public GetAssignmentController(@Autowired RetrieveRoleAssignmentOrchestrator retrieveRoleAssignmentService) {
        this.retrieveRoleAssignmentService = retrieveRoleAssignmentService;
    }
    //**************** Get role assignment records by actorId API ***************

    @GetMapping(
        path = "/am/role-assignments/actors/{actorId}",
        produces = V1.MediaType.GET_ASSIGNMENTS
    )
    @ApiOperation("Retrieve JSON representation of multiple Role Assignment records.")
    @ApiResponses({
        @ApiResponse(
            code = 200,
            message = "Success",
            response = RoleAssignmentResource.class
        ),
        @ApiResponse(
            code = 400,
            message = V1.Error.INVALID_REQUEST
        )
    })
    @LogAudit(operationType = GET_ASSIGNMENTS_BY_ACTOR,
        id = "T(uk.gov.hmcts.reform.roleassignment.util.AuditLoggerUtil).getAssignmentIds(#result)",
        actorId = "T(uk.gov.hmcts.reform.roleassignment.util.AuditLoggerUtil).getActorIds(#result)",
        correlationId = "#correlationId")
    public ResponseEntity<RoleAssignmentResource> retrieveRoleAssignmentsByActorId(
                                 @RequestHeader(value = "x-correlation-id",
                                 required = false) String correlationId,
                                @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch,
                                 @ApiParam(value = "Actor Id ", required = true)
                       @PathVariable("actorId") String actorId) {

        ResponseEntity<RoleAssignmentResource> responseEntity = retrieveRoleAssignmentService.getAssignmentsByActor(
            actorId
        );
        HttpHeaders responseHeaders = new HttpHeaders();
        RoleAssignmentResource body = responseEntity.getBody();

        if (body != null && CollectionUtils.isNotEmpty(body.getRoleAssignmentResponse())) {
            long etag = retrieveRoleAssignmentService.retrieveETag(actorId);
            String weakEtag = "W/\"" + etag + "\"";
            responseHeaders.setETag(weakEtag);
        }

        return ResponseEntity
            .status(HttpStatus.OK)
            .headers(responseHeaders)
            .body(body);
    }

    //**************** Get role configurations API ***************

    @GetMapping(
        path = "/am/role-assignments/roles",
        produces = V1.MediaType.GET_ROLES
    )
    @ResponseStatus(code = HttpStatus.OK)
    @ApiOperation("retrieves a list of roles configurations available in role assignment service")
    @ApiResponses({
        @ApiResponse(
            code = 200,
            message = "Ok",
            response = Object.class
        )
    })
    public ResponseEntity<JsonNode> getListOfRoles(@RequestHeader(value = "x-correlation-id",
        required = false) String correlationId) throws IOException {
        JsonNode rootNode = retrieveRoleAssignmentService.getListOfRoles();
        return ResponseEntity.status(HttpStatus.OK).body(rootNode);
    }
}
