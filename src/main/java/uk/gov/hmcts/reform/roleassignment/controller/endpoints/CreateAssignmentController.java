
package uk.gov.hmcts.reform.roleassignment.controller.endpoints;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.service.createroles.CreateRoleAssignmentOrchestrator;
import uk.gov.hmcts.reform.roleassignment.v1.V1;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

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
        path = "/role-assignment",
        produces = V1.MediaType.CREATE_ASSIGNMENT,
        consumes = {"application/json"}
    )
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
        @Valid
        @NotNull(message = "Provide role assignment body.")
        @RequestBody AssignmentRequest assignmentRequest) throws Exception {

        LOG.info("CreateAssignmentController : {}", createRoleAssignmentService);
        return createRoleAssignmentService.createRoleAssignment(assignmentRequest);
    }
}
