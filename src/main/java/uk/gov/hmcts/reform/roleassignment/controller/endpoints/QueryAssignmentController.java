
package uk.gov.hmcts.reform.roleassignment.controller.endpoints;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.roleassignment.auditlog.LogAudit;
import uk.gov.hmcts.reform.roleassignment.domain.model.QueryRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentResource;
import uk.gov.hmcts.reform.roleassignment.domain.service.queryroles.QueryRoleAssignmentOrchestrator;
import uk.gov.hmcts.reform.roleassignment.v1.V1;

import static uk.gov.hmcts.reform.roleassignment.auditlog.AuditOperationType.SEARCH_ASSIGNMENTS;

@Api(value = "roles")
@RestController
public class QueryAssignmentController {

    private static final Logger logger = LoggerFactory.getLogger(QueryAssignmentController.class);
    private final QueryRoleAssignmentOrchestrator queryRoleAssignmentOrchestrator;

    public QueryAssignmentController(QueryRoleAssignmentOrchestrator queryRoleAssignmentOrchestrator) {
        this.queryRoleAssignmentOrchestrator = queryRoleAssignmentOrchestrator;
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
            response = RoleAssignmentResource.class
        ),
        @ApiResponse(
            code = 400,
            message = V1.Error.BAD_REQUEST_INVALID_PARAMETER
        ),

    })
    @LogAudit(operationType = SEARCH_ASSIGNMENTS,
        id = "T(uk.gov.hmcts.reform.roleassignment.util.AuditLoggerUtil).searchAssignmentIds(#result)",
        correlationId = "#corsrelationId")
    public ResponseEntity<RoleAssignmentResource> retrieveRoleAssignmentsByQueryRequest(
                                 @RequestHeader(value = "x-correlation-id",
                                  required = false) String correlationId,
                                  @RequestHeader(value = "pageNumber", required = false) Integer pageNumber,
                                  @RequestHeader(value = "size", required = false) Integer size,
                                  @RequestHeader(value = "sort", required = false) String sort,
                                  @RequestHeader(value = "direction", required = false) String direction,
                                  @Validated @RequestBody(required = true) QueryRequest queryRequest) {
        long startTime = System.currentTimeMillis();
        ResponseEntity<RoleAssignmentResource> response = queryRoleAssignmentOrchestrator
            .retrieveRoleAssignmentsByQueryRequest(queryRequest, pageNumber, size, sort, direction);
        logger.info(
            " >> retrieveRoleAssignmentsByQueryRequest execution finished at {} . Time taken = {} milliseconds",
            System.currentTimeMillis(),
            Math.subtractExact(System.currentTimeMillis(), startTime)
        );
        return response;
    }
}
