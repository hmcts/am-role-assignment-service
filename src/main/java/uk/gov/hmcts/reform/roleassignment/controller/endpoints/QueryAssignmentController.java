
package uk.gov.hmcts.reform.roleassignment.controller.endpoints;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentRequestResource;
import uk.gov.hmcts.reform.roleassignment.domain.service.queryroles.QueryRoleAssignmentOrchestrator;
import uk.gov.hmcts.reform.roleassignment.v1.V1;

@Api(value = "roles")
@RestController
public class QueryAssignmentController {

    private final QueryRoleAssignmentOrchestrator queryRoleAssignmentOrchestrator;

    public QueryAssignmentController(QueryRoleAssignmentOrchestrator queryRoleAssignmentOrchestrator) {
        this.queryRoleAssignmentOrchestrator = queryRoleAssignmentOrchestrator;

    }

    @GetMapping(
        path = "/am/role-assignments",
        produces = V1.MediaType.GET_ASSIGNMENTS
    )
    @ApiOperation("Get Role assignment records by Case Id and Actor Id for RoleType as a CASE.")
    @ApiResponses({
                      @ApiResponse(
                          code = 200,
                          message = "Success",
                          response = RoleAssignmentRequestResource.class
                      ),
                      @ApiResponse(
                          code = 400,
                          message = V1.Error.INVALID_REQUEST
                      ),
                      @ApiResponse(
                          code = 400,
                          message = V1.Error.INVALID_ROLE_TYPE
                      ),
                      @ApiResponse(
                          code = 404,
                          message = V1.Error.INVALID_ACTOR_AND_CASE_ID
                      ),
                      @ApiResponse(
                          code = 400,
                          message = V1.Error.INVALID_CASE_ID
                      ),
                      @ApiResponse(
                          code = 404,
                          message = V1.Error.NO_RECORDS_FOUND_BY_ACTOR
                      ),
                      @ApiResponse(
                          code = 404,
                          message = V1.Error.NO_RECORDS_FOUND_FOR_CASE_ID
                      ),
                      @ApiResponse(
                          code = 404,
                          message = V1.Error.ASSIGNMENT_RECORDS_NOT_FOUND
                      )
                  })
    public ResponseEntity<Object> retrieveRoleAssignmentsByActorIdAndCaseId(
        @ApiParam(value = "Actor Id", required = false)
        @RequestParam(value = "actorId", required = false) String actorId,
        @ApiParam(value = "Case Id", required = false)
        @RequestParam(value = "caseId", required = false) String caseId,
        @ApiParam(value = "Role Type", required = true)
        @RequestParam(value = "roleType", required = true) String roleType) {

        return queryRoleAssignmentOrchestrator.retrieveRoleAssignmentsByActorIdAndCaseId(actorId, caseId, roleType);
    }
}
