
package uk.gov.hmcts.reform.assignment.controller.endpoints;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.assignment.domain.service.deleteroles.DeleteRoleAssignmentOrchestrator;
import uk.gov.hmcts.reform.assignment.v1.V1;


@Api(value = "roles")
@RestController
public class DeleteAssignmentController {
    private static final Logger LOG = LoggerFactory.getLogger(DeleteAssignmentController.class);

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
            code = 404,
            message = V1.Error.NO_RECORDS_FOUND_BY_ACTOR
        ),
        @ApiResponse(
            code = 404,
            message = V1.Error.NO_RECORDS_FOUND_BY_PROCESS
        )
    })
    public ResponseEntity<Object> deleteRoleAssignment(@RequestHeader(value = "assignerId", required = false)
                                                           String assignerId,
                                                       @RequestParam(value = "process", required = false)
                                                           String process,
                                                       @RequestParam(value = "reference", required = false)
                                                           String reference) throws Exception {
        LOG.info("Request raised by assigner : {}", assignerId);
        return deleteRoleAssignmentOrchestrator.deleteRoleAssignment(null, process, reference, null);

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
                          code = 404,
                          message = V1.Error.NO_RECORD_FOUND_BY_ASSIGNMENT_ID
                      )
                  })
    public ResponseEntity<Object> deleteRoleAssignmentById(
        @RequestHeader(value = "assignerId", required = false)
            String assignerId,
        @ApiParam(value = "assignmentId", required = true)
        @PathVariable String assignmentId) throws Exception {
        return deleteRoleAssignmentOrchestrator.deleteRoleAssignment(null, null, null, assignmentId);
    }
}
