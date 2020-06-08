
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
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.service.createroles.CreateRoleAssignmentOrchestrator;

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
        produces = {"application/json"}
    )
    @ApiOperation("creates a role/multiple role assignments")
    @ApiResponses({
        @ApiResponse(
            code = 201,
            message = "Created",
            response = Object.class //maybe not the correct thing
        ),
        @ApiResponse(
            code = 404,
            message = "Resource Not Found"
        )
    })
    public ResponseEntity<Object> createRoleAssignment(

        @Valid
        @NotNull(message = "Provide a role assignment body.")
        @RequestBody RoleAssignmentRequest createRoleAssignmentRequest

    ) {
        LOG.info("CreateAssignmentController : {}", createRoleAssignmentService);
        ResponseEntity<Object> responseEntity = createRoleAssignmentService.createRoleAssignment(createRoleAssignmentRequest);

        return responseEntity;
    }
}
