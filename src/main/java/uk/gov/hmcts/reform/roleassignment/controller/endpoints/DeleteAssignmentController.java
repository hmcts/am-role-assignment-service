
package uk.gov.hmcts.reform.roleassignment.controller.endpoints;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.roleassignment.domain.service.deleteroles.DeleteRoleAssignmentOrchestrator;
import uk.gov.hmcts.reform.roleassignment.v1.V1;


@Api(value = "roles")
@RestController
public class DeleteAssignmentController {

    private DeleteRoleAssignmentOrchestrator deleteRoleAssignmentOrchestrator;

    public DeleteAssignmentController(DeleteRoleAssignmentOrchestrator deleteRoleAssignmentOrchestrator) {
        this.deleteRoleAssignmentOrchestrator = deleteRoleAssignmentOrchestrator;
    }

    @DeleteMapping(
        path = "am/role-assignments",
        produces = V1.MediaType.DELETE_ASSIGNMENT,
        consumes = {"application/json"}
    )
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
            message = V1.Error.NOT_FOUND_BY_ACTOR
        ),
        @ApiResponse(
            code = 404,
            message = V1.Error.NOT_FOUND_BY_PROCESS
        )
    })
    public ResponseEntity<Object> deleteRoleAssignment(@RequestParam(value = "actorId", required = false)
                                                               String actorId,
                                                       @RequestParam(value = "process", required = false)
                                                           String process,
                                                       @RequestParam(value = "reference", required = false)
                                                               String reference) throws Exception {
        return deleteRoleAssignmentOrchestrator.deleteRoleAssignment(actorId, process, reference);

    }


}
