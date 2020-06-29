
package uk.gov.hmcts.reform.roleassignment.controller.endpoints;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.Case;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentRequestResource;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ParseRequestService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.roleassignment.domain.service.createroles.CreateRoleAssignmentOrchestrator;
import uk.gov.hmcts.reform.roleassignment.feignclients.DataStoreFeignClient;
import uk.gov.hmcts.reform.roleassignment.util.ValidationUtil;
import uk.gov.hmcts.reform.roleassignment.v1.V1;

import java.text.ParseException;
import java.util.UUID;

@Api(value = "roles")
@RestController
public class GetAssignmentController {
    //getAssignmentsbyActorId

    private final ParseRequestService parseRequestService;
    private final PersistenceService persistenceService;
    private final DataStoreFeignClient dataStoreFeignClient;
    private CreateRoleAssignmentOrchestrator createRoleAssignmentService;

    public GetAssignmentController(ParseRequestService parseRequestService, PersistenceService persistenceService,
                                   DataStoreFeignClient dataStoreFeignClient,
                                   CreateRoleAssignmentOrchestrator createRoleAssignmentService) {
        this.parseRequestService = parseRequestService;
        this.persistenceService = persistenceService;
        this.dataStoreFeignClient = dataStoreFeignClient;
        this.createRoleAssignmentService = createRoleAssignmentService;
    }

    @GetMapping(
        path = "/role-assignment/actor-id/{actorId}",
        produces = {"application/json"
        })
    @ApiOperation("Retrieve JSON representation of a Role Assignment records.")
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
                          message = V1.Error.INVALID_REQUEST
                      )
                  })
    public ResponseEntity<Object> retrieveRoleAssignmentByActorId(
        @PathVariable("actorId") UUID actorId) throws Exception {
        ResponseEntity<?> responseEntity = createRoleAssignmentService.retrieveRoleAssignmentByActorId(actorId);
        long etag = createRoleAssignmentService.retrieveETag(actorId);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set(
            "ETag",
            String.valueOf(etag));

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

    @GetMapping("/getCaseDetails")
    public String getDatastoreHealthStatus() {
        return dataStoreFeignClient.getServiceStatus();
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
}
