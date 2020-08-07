
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.roleassignment.domain.service.createroles.CreateRoleAssignmentOrchestrator;
import uk.gov.hmcts.reform.roleassignment.v1.V1;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;


import java.text.ParseException;

@Api(value = "roles")
@RestController
public class CreateAssignmentController {

    private static final Logger LOG = LoggerFactory.getLogger(CreateAssignmentController.class);

    private final CreateRoleAssignmentOrchestrator createRoleAssignmentOrchestrator;

    public CreateAssignmentController(CreateRoleAssignmentOrchestrator createRoleAssignmentOrchestrator) {
        this.createRoleAssignmentOrchestrator = createRoleAssignmentOrchestrator;
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
    public ResponseEntity<Object> createRoleAssignment(
        @Validated
        @RequestBody(required = true) AssignmentRequest assignmentRequest) throws ParseException {

        LOG.info("CreateAssignmentController : {}", createRoleAssignmentOrchestrator);
        return createRoleAssignmentOrchestrator.createRoleAssignment(assignmentRequest);
    }
}
