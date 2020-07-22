
package uk.gov.hmcts.reform.roleassignment.controller.endpoints;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.roleassignment.auditlog.LogAudit;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentRequestResource;
import uk.gov.hmcts.reform.roleassignment.domain.service.createroles.CreateRoleAssignmentOrchestrator;
import uk.gov.hmcts.reform.roleassignment.v1.V1;

import java.text.ParseException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.roleassignment.auditlog.AuditOperationType.CREATE_ASSIGNMENTS;

@Api(value = "roles")
@RestController
public class CreateAssignmentController {

    private static final Logger LOG = LoggerFactory.getLogger(CreateAssignmentController.class);
    private CreateRoleAssignmentOrchestrator createRoleAssignmentService;

    @Autowired
    public CreateAssignmentController(CreateRoleAssignmentOrchestrator createRoleAssignmentService) {
        this.createRoleAssignmentService = createRoleAssignmentService;
    }

    //**************** Create Role Assignment  API ***************

    @PostMapping(
        path = "/am/role-assignments",
        produces = V1.MediaType.CREATE_ASSIGNMENTS,
        consumes = {"application/json"}
    )
    @ResponseStatus(code = HttpStatus.CREATED)
    @ApiOperation("creates multiple role assignments")
    @ApiResponses({
        @ApiResponse(
            code = 201,
            message = "Created",
            response = Object.class //need to replace with resource class
        ),
        @ApiResponse(
            code = 400,
            message = V1.Error.INVALID_ROLE_NAME
        ),
        @ApiResponse(
            code = 400,
            message = V1.Error.INVALID_REQUEST
        )
    })
    @LogAudit(operationType = CREATE_ASSIGNMENTS,
        process = "T(uk.gov.hmcts.reform.roleassignment.controller.endpoints.CreateAssignmentController).buildProcess(#result)",
        reference = "T(uk.gov.hmcts.reform.roleassignment.controller.endpoints.CreateAssignmentController).buildReference(#result)",
        id = "T(uk.gov.hmcts.reform.roleassignment.controller.endpoints.CreateAssignmentController).buildAssignmentIds(#result)",
        actorId = "#result.body.actorId",
        roleName = "#result.body.roleName",
        caseId = "#result.body.caseId",
        assignerId = "#result.body.assignerId")

    public ResponseEntity<Object> createRoleAssignment(
        @Validated
        @RequestBody(required = true) AssignmentRequest assignmentRequest) throws ParseException {

        LOG.info("CreateAssignmentController : {}", createRoleAssignmentService);
        return createRoleAssignmentService.createRoleAssignment(assignmentRequest);
    }

    public static List<UUID> buildAssignmentIds(ResponseEntity<RoleAssignmentRequestResource> response) {
        return response.getBody().getRoleAssignmentRequest().getRequestedRoles().stream().limit(10)
            .map(RoleAssignment::getId)
            .collect(Collectors.toList());
    }

    public static String buildProcess(ResponseEntity<RoleAssignmentRequestResource> response) {
        return response.getBody().getRoleAssignmentRequest().getRequest().getProcess();
    }

    public static String buildReference(ResponseEntity<RoleAssignmentRequestResource> response) {
        return response.getBody().getRoleAssignmentRequest().getRequest().getReference();
    }
}
