
package uk.gov.hmcts.reform.roleassignment.controller.endpoints;

import javax.validation.Valid;

import io.swagger.annotations.Api;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ParseRequestService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.StoreRequestService;
import uk.gov.hmcts.reform.roleassignment.feignclients.DataStoreFeignClient;
import uk.gov.hmcts.reform.roleassignment.v1.V1;

@Api(value = "roles")
@RestController
public class RoleAssignmentController {

    private final ParseRequestService parseRequestService;
    private final StoreRequestService storeRequestService;
    private final DataStoreFeignClient dataStoreFeignClient;

    public RoleAssignmentController(ParseRequestService parseRequestService, StoreRequestService storeRequestService,
                                    DataStoreFeignClient dataStoreFeignClient) {
        this.parseRequestService = parseRequestService;
        this.storeRequestService = storeRequestService;
        this.dataStoreFeignClient = dataStoreFeignClient;
    }

    @PostMapping("/processRequest")
    public ResponseEntity<String> processRequest(@Valid @RequestBody RoleAssignmentRequest roleAssignmentRequest) {
        if (!parseRequestService.parseRequest(roleAssignmentRequest)) {
            throw new BadRequestException(V1.Error.INVALID_REQUEST);
        }
        // service call to store request and requested roles in db for audit purpose.
        storeRequestService.persistRequestAndRequestedRoles(roleAssignmentRequest);
        return ResponseEntity.ok("Success");
    }

    @GetMapping("/getCaseDetails")
    public String getCaseDetails() {
        return dataStoreFeignClient.getServiceStatus();
    }

    @GetMapping(value = "/caseworkers/{uid}/jurisdictions/{jid}/case-types/{ctid}/cases/{cid}",
                produces = "application/json")
    public String getCaseData(@PathVariable("uid") String uid, @PathVariable("jid") String jurisdictionId,
                              @PathVariable("ctid") String caseTypeId, @PathVariable("cid") String caseId) {
        return dataStoreFeignClient.getCaseData(uid, jurisdictionId, caseTypeId, caseId);
    }
}
