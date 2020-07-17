
package uk.gov.hmcts.reform.roleassignment.controller.endpoints;

import static uk.gov.hmcts.reform.roleassignment.apihelper.Constants.ROLES_JSON;
import static uk.gov.hmcts.reform.roleassignment.apihelper.Constants.ROLE_JSON_PATTERNS_FIELD;

import java.io.InputStream;
import java.text.ParseException;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.Case;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentRequestResource;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ParseRequestService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.roleassignment.domain.service.createroles.CreateRoleAssignmentOrchestrator;
import uk.gov.hmcts.reform.roleassignment.domain.service.getroles.RetrieveRoleAssignmentOrchestrator;
import uk.gov.hmcts.reform.roleassignment.feignclients.DataStoreFeignClient;
import uk.gov.hmcts.reform.roleassignment.util.ValidationUtil;
import uk.gov.hmcts.reform.roleassignment.v1.V1;

@Slf4j
@Api(value = "roles")
@RestController
public class GetAssignmentController {

    private RetrieveRoleAssignmentOrchestrator retrieveRoleAssignmentService;
    private final ParseRequestService parseRequestService;
    private final PersistenceService persistenceService;
    private final DataStoreFeignClient dataStoreFeignClient;
    private CreateRoleAssignmentOrchestrator createRoleAssignmentService;

    public GetAssignmentController(ParseRequestService parseRequestService, PersistenceService persistenceService,
                                   DataStoreFeignClient dataStoreFeignClient,
                                   CreateRoleAssignmentOrchestrator createRoleAssignmentService,
                                   RetrieveRoleAssignmentOrchestrator retrieveRoleAssignmentService) {
        this.parseRequestService = parseRequestService;
        this.persistenceService = persistenceService;
        this.dataStoreFeignClient = dataStoreFeignClient;
        this.createRoleAssignmentService = createRoleAssignmentService;
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
        @PathVariable("actorId") String actorId) throws Exception {

        log.info("ifNoneMatch :::: {}", ifNoneMatch);
        log.info("actorId :::: {}", actorId);
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

    @PostMapping("/processRequest")
    public ResponseEntity<String> processRequest(@Validated @RequestBody AssignmentRequest assignmentRequest)
        throws ParseException {
        ValidationUtil.validateAssignmentRequest(assignmentRequest);
        // service call to store request and requested roles in db for audit purpose.
        persistenceService.persistRequest(assignmentRequest.getRequest());

        return ResponseEntity.ok("Success");

    }

    @GetMapping(value = "/caseworkers/{uid}/jurisdictions/{jid}/case-types/{ctid}/cases/{cid}",
        produces = "application/json")
    public String getCaseData(@PathVariable("uid") String uid, @PathVariable("jid") String jurisdictionId,
                              @PathVariable("ctid") String caseTypeId, @PathVariable("cid") String caseId) {
        return dataStoreFeignClient.getCaseDataV1(uid, jurisdictionId, caseTypeId, caseId);
    }

    @GetMapping(value = "/cases/{caseId}", produces = "application/json")
    public Case getCaseDataV2(@PathVariable("caseId") String caseId) {
        return dataStoreFeignClient.getCaseDataV2(caseId);
    }

    //**************** Get Roles API ***************

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
    public ResponseEntity<Object> getListOfRoles() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode;
        try (InputStream input = GetAssignmentController.class.getClassLoader().getResourceAsStream(ROLES_JSON)) {
            assert input != null;
            rootNode = mapper.readTree(input);
            for (JsonNode roleNode: rootNode) {
                ObjectNode obj = (ObjectNode) roleNode;
                obj.remove(ROLE_JSON_PATTERNS_FIELD);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.status(HttpStatus.OK).body(rootNode);
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
                          message = V1.Error.NO_RECORDS_FOUND_BY_ACTOR
                      ),
                      @ApiResponse(
                          code = 404,
                          message = V1.Error.NO_RECORDS_FOUND_FOR_CASE_ID
                      )
                  })
    public ResponseEntity<Object> retrieveRoleAssignmentsByActorIdAndCaseId(
        @ApiParam(value = "Actor Id", required = false)
        @RequestParam(value = "actorId", required = false) String actorId,
        @ApiParam(value = "Case Id", required = false)
        @RequestParam(value = "caseId", required = false) String caseId,
        @ApiParam(value = "Role Type", required = true)
        @RequestParam(value = "roleType", required = true) String roleType) {
        if (StringUtils.isEmpty(roleType) || !roleType.equals(RoleType.CASE.name())) {
            throw new BadRequestException(V1.Error.INVALID_ROLE_TYPE);
        }
        return retrieveRoleAssignmentService.retrieveRoleAssignmentsByActorIdAndCaseId(actorId, caseId, roleType);
    }
}
