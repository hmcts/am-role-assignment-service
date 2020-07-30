
package uk.gov.hmcts.reform.roleassignment.controller.endpoints;

import java.io.IOException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.roleassignment.domain.service.deleteroles.DeleteRoleAssignmentOrchestrator;
import uk.gov.hmcts.reform.roleassignment.launchdarkly.LdFlagChecker;
import uk.gov.hmcts.reform.roleassignment.util.Constants;
import uk.gov.hmcts.reform.roleassignment.util.SecurityUtils;
import uk.gov.hmcts.reform.roleassignment.v1.V1;


@Api(value = "roles")
@RestController
public class DeleteAssignmentController {

    @Autowired
    private LdFlagChecker ldFlagChecker;
    @Autowired
    private SecurityUtils securityUtils;

    private DeleteRoleAssignmentOrchestrator deleteRoleAssignmentOrchestrator;

    public DeleteAssignmentController(DeleteRoleAssignmentOrchestrator deleteRoleAssignmentOrchestrator) {
        this.deleteRoleAssignmentOrchestrator = deleteRoleAssignmentOrchestrator;
    }

    @DeleteMapping(
        path = "am/role-assignments",
        produces = V1.MediaType.DELETE_ASSIGNMENTS
    )
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    @ApiOperation("Deletes multiple role assignments  based on query parameters.")

    @ApiResponses({
        @ApiResponse(
            code = 204,
            message = "No Content"
        ),
        @ApiResponse(
            code = 400,
            message = V1.Error.BAD_REQUEST_INVALID_PARAMETER
        ),
        @ApiResponse(
            code = 400,
            message = V1.Error.BAD_REQUEST_MISSING_PARAMETERS
        ),
        @ApiResponse(
            code = 422,
            message = V1.Error.UNPROCESSABLE_ENTITY_REQUEST_REJECTED
        )
    })
    public ResponseEntity<Object> deleteRoleAssignment(@RequestHeader(value = "assignerId", required = false)
                                                           String assignerId,
                                                       @RequestParam(value = "process", required = false)
                                                           String process,
                                                       @RequestParam(value = "reference", required = false)
                                                           String reference) {
        return deleteRoleAssignmentOrchestrator.deleteRoleAssignmentByProcessAndReference(process, reference);
    }

    @DeleteMapping(
        path = "am/role-assignments/{assignmentId}",
        produces = V1.MediaType.DELETE_ASSIGNMENTS
    )
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    @ApiOperation("Deletes the role assignment by assignment id.")
    @ApiResponses({
        @ApiResponse(
            code = 204,
            message = "No Content"
        ),
        @ApiResponse(
            code = 400,
            message = V1.Error.BAD_REQUEST_INVALID_PARAMETER
        ),
        @ApiResponse(
            code = 400,
            message = V1.Error.BAD_REQUEST_MISSING_PARAMETERS
        ),
        @ApiResponse(
            code = 422,
            message = V1.Error.UNPROCESSABLE_ENTITY_REQUEST_REJECTED
        )
    })
    public ResponseEntity<Object> deleteRoleAssignmentById(
        @RequestHeader(value = "assignerId", required = false)
            String assignerId,
        @ApiParam(value = "assignmentId", required = true)
        @PathVariable String assignmentId) throws IOException {

        if (!ldFlagChecker.verifyServiceAndFlag(securityUtils.getServiceName(), Constants.DELETE_BY_ASSIGNMENT_ID_FLAG)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Constants.ENDPOINT_NOT_AVAILABLE);
        }
        return deleteRoleAssignmentOrchestrator.deleteRoleAssignmentByAssignmentId(assignmentId);
    }
}
