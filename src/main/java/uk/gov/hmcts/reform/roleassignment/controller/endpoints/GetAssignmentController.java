
package uk.gov.hmcts.reform.roleassignment.controller.endpoints;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.ServiceException;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentRequestResource;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.roleassignment.domain.service.getroles.RetrieveRoleAssignmentOrchestrator;
import uk.gov.hmcts.reform.roleassignment.feignclients.DataStoreFeignClient;
import uk.gov.hmcts.reform.roleassignment.util.Constants;
import uk.gov.hmcts.reform.roleassignment.v1.V1;

import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Api(value = "roles")
@RestController
public class GetAssignmentController {

    private RetrieveRoleAssignmentOrchestrator retrieveRoleAssignmentService;
    private final PersistenceService persistenceService;
    private final DataStoreFeignClient dataStoreFeignClient;

    public GetAssignmentController(PersistenceService persistenceService,
                                   DataStoreFeignClient dataStoreFeignClient,
                                   RetrieveRoleAssignmentOrchestrator retrieveRoleAssignmentService) {
        this.persistenceService = persistenceService;
        this.dataStoreFeignClient = dataStoreFeignClient;
        this.retrieveRoleAssignmentService = retrieveRoleAssignmentService;
    }

    @GetMapping(
        path = "/am/role-assignments/actors/{actorId}",
        produces = V1.MediaType.GET_ASSIGNMENTS
    )
    @ApiOperation("Retrieve JSON representation of multiple Role Assignment records.")
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
            code = 404,
            message = V1.Error.NO_RECORDS_FOUND_BY_ACTOR
        )
    })
    public ResponseEntity<Object> retrieveRoleAssignmentsByActorId(

        @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch,

        @ApiParam(value = "Actor Id ", required = true)
        @PathVariable("actorId") String actorId) {

        ResponseEntity<?> responseEntity = retrieveRoleAssignmentService.getAssignmentsByActor(
            actorId
        );
        long etag = retrieveRoleAssignmentService.retrieveETag(UUID.fromString(actorId));
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set(
            "ETag",
            String.valueOf(etag)
        );

        return ResponseEntity
            .status(HttpStatus.OK)
            .headers(responseHeaders)
            .body(responseEntity.getBody());
    }

    //**************** Get Roles  API ***************

    @GetMapping(
        path = "/am/role-assignments/roles",
        produces = V1.MediaType.GET_ROLES
    )
    @ResponseStatus(code = HttpStatus.OK)
    @ApiOperation("retrieves a list of roles available in role assignment service")
    @ApiResponses({
        @ApiResponse(
            code = 200,
            message = "Ok",
            response = Object.class
        )
    })
    public ResponseEntity<Object> getListOfRoles() {

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode;
        try (InputStream input = GetAssignmentController.class.getClassLoader()
            .getResourceAsStream(Constants.ROLES_JSON)) {
            assert input != null;
            rootNode = mapper.readTree(input);
            for (JsonNode roleNode : rootNode) {
                ObjectNode obj = (ObjectNode) roleNode;
                obj.remove(Constants.ROLE_JSON_PATTERNS_FIELD);
            }
        } catch (Exception e) {
            throw new ServiceException("Service Exception", e);
        }
        return ResponseEntity.status(HttpStatus.OK).body(rootNode);
    }
}
