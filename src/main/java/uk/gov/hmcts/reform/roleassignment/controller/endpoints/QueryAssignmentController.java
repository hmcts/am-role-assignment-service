
package uk.gov.hmcts.reform.roleassignment.controller.endpoints;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.roleassignment.auditlog.LogAudit;
import uk.gov.hmcts.reform.roleassignment.domain.model.QueryRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentRequestResource;
import uk.gov.hmcts.reform.roleassignment.domain.service.queryroles.QueryRoleAssignmentOrchestrator;
import uk.gov.hmcts.reform.roleassignment.util.SecurityUtils;
import uk.gov.hmcts.reform.roleassignment.v1.V1;

import static uk.gov.hmcts.reform.roleassignment.auditlog.AuditOperationType.SEARCH_ASSIGNMENTS;

@Api(value = "roles")
@RestController
public class QueryAssignmentController {

    private final QueryRoleAssignmentOrchestrator queryRoleAssignmentOrchestrator;

    @Autowired
    private SecurityUtils securityUtils;

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
    @LogAudit(operationType = SEARCH_ASSIGNMENTS,
        id = "T(uk.gov.hmcts.reform.roleassignment.util.AuditLoggerUtil).searchAssignmentIds(#result)",
        correlationId = "#correlationId")
    public ResponseEntity<Object> retrieveRoleAssignmentsByActorIdAndCaseId(@RequestHeader(value = "x-correlation-id",
        required = false) String correlationId,
                                                                            @ApiParam(value = "Actor Id", required = false)
                                                                            @RequestParam(value = "actorId", required = false) String actorId,
                                                                            @ApiParam(value = "Case Id", required = false)
                                                                            @RequestParam(value = "caseId", required = false) String caseId,
                                                                            @ApiParam(value = "Role Type", required = true)
                                                                            @RequestParam(value = "roleType", required = true) String roleType) {

        return queryRoleAssignmentOrchestrator.retrieveRoleAssignmentsByActorIdAndCaseId(actorId, caseId, roleType);
    }

    @GetMapping(path = "/am/role-assignments/ld/endpoint")
    public ResponseEntity<Object> getIdLdDemo(@RequestHeader(value = "x-correlation-id",
        required = false) String correlationId) {
        return ResponseEntity.status(HttpStatus.OK).body("Launch Darkly flag check is successful for the endpoint");
    }

    @PostMapping(
        path = "/am/role-assignments/query",
        produces = V1.MediaType.POST_ASSIGNMENTS
    )
    @ApiOperation("Fetch Role assignment records by QueryRequest.")
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
    @LogAudit(operationType = SEARCH_ASSIGNMENTS,
        id = "T(uk.gov.hmcts.reform.roleassignment.util.AuditLoggerUtil).searchAssignmentIds(#result)",
        correlationId = "#correlationId")
    public ResponseEntity<Object> retrieveRoleAssignmentsByQueryRequest(@RequestHeader(value = "x-correlation-id", required = false)
                                                                            String correlationId,
                                                                        @RequestHeader(value = "pageNumber", required = false)
                                                                            Integer pageNumber,
                                                                        @Validated
                                                                        @RequestBody(required = true) QueryRequest queryRequest) {

        return queryRoleAssignmentOrchestrator.retrieveRoleAssignmentsByQueryRequest(queryRequest,pageNumber);
    }
}
