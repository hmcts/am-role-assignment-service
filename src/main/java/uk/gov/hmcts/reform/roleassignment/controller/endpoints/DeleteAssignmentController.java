
package uk.gov.hmcts.reform.roleassignment.controller.endpoints;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.roleassignment.auditlog.LogAudit;
import uk.gov.hmcts.reform.roleassignment.domain.model.MultipleQueryRequest;
import uk.gov.hmcts.reform.roleassignment.domain.service.deleteroles.DeleteRoleAssignmentOrchestrator;
import uk.gov.hmcts.reform.roleassignment.versions.V1;

import static uk.gov.hmcts.reform.roleassignment.auditlog.AuditOperationType.DELETE_ASSIGNMENTS_BY_ID;
import static uk.gov.hmcts.reform.roleassignment.auditlog.AuditOperationType.DELETE_ASSIGNMENTS_BY_PROCESS;
import static uk.gov.hmcts.reform.roleassignment.auditlog.AuditOperationType.DELETE_ASSIGNMENTS_BY_QUERY;


@Api(value = "roles")
@RestController
public class DeleteAssignmentController {

    private static final Logger logger = LoggerFactory.getLogger(DeleteAssignmentController.class);

    private DeleteRoleAssignmentOrchestrator deleteRoleAssignmentOrchestrator;


    public DeleteAssignmentController(@Autowired DeleteRoleAssignmentOrchestrator deleteRoleAssignmentOrchestrator
    ) {
        this.deleteRoleAssignmentOrchestrator = deleteRoleAssignmentOrchestrator;

    }

    @DeleteMapping(
        path = "am/role-assignments",
        produces = V1.MediaType.DELETE_ASSIGNMENTS
    )
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    @ApiOperation("Deletes multiple role assignments based on query parameters.")

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
    @LogAudit(operationType = DELETE_ASSIGNMENTS_BY_PROCESS,
        process = "#process",
        reference = "#reference",
        correlationId = "#correlationId")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResponseEntity<Void> deleteRoleAssignment(@RequestHeader(value = "x-correlation-id",
        required = false)
                                                         String correlationId,
                                                     @RequestParam(value = "process", required = false)
                                                         String process,
                                                     @RequestParam(value = "reference", required = false)
                                                         String reference) {
        long startTime = System.currentTimeMillis();
        ResponseEntity<Void> responseEntity = deleteRoleAssignmentOrchestrator
            .deleteRoleAssignmentByProcessAndReference(process, reference);
        logger.debug(
            " >> deleteRoleAssignmentByProcessAndReference execution finished at {} .Time taken = {} milliseconds",
            System.currentTimeMillis(),
            Math.subtractExact(System.currentTimeMillis(), startTime)
        );
        return responseEntity;
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
    @LogAudit(operationType = DELETE_ASSIGNMENTS_BY_ID,
        assignmentId = "#assignmentId",
        correlationId = "#correlationId"
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResponseEntity<Void> deleteRoleAssignmentById(@RequestHeader(
        value = "x-correlation-id",
        required = false)
                                                             String correlationId,
                                                         @ApiParam(value = "assignmentId", required = true)
                                                         @PathVariable String assignmentId) {
        return deleteRoleAssignmentOrchestrator.deleteRoleAssignmentByAssignmentId(assignmentId);
    }

    @PostMapping(
        path = "/am/role-assignments/query/delete",
        consumes = "application/json",
        produces = V1.MediaType.POST_DELETE_ASSIGNMENTS_BY_QUERY_REQUEST
    )
    @ResponseStatus(code = HttpStatus.OK)
    @ApiOperation("Deletes the role assignments by query.")
    @ApiResponses({
        @ApiResponse(
            code = 200,
            message = "The assignment records have been deleted."
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
    @LogAudit(operationType = DELETE_ASSIGNMENTS_BY_QUERY,
        requestPayload = "#auditContextWith.requestPayload",
        correlationId = "#correlationId"
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResponseEntity<Void> deleteRoleAssignmentsByQuery(@RequestHeader(
        value = "x-correlation-id",
        required = false)
                                                                 String correlationId,
                                                             @ApiParam(value = "multipleQueryRequest", required = true)
                                                             @Validated @RequestBody(required = true)
                                                                 MultipleQueryRequest multipleQueryRequest) {
        long startTime = System.currentTimeMillis();
        logger.info("Inside the Delete role assignment records by multiple query request method");
        ResponseEntity<Void> responseEntity = deleteRoleAssignmentOrchestrator
            .deleteRoleAssignmentsByQuery(multipleQueryRequest);
        logger.debug(
            " >> deleteRoleAssignmentsByQuery execution finished at {} .Time taken = {} milliseconds",
            System.currentTimeMillis(),
            Math.subtractExact(System.currentTimeMillis(), startTime)
        );
        return responseEntity;
    }
}
