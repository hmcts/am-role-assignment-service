
package uk.gov.hmcts.reform.roleassignment.controller.endpoints;

import io.swagger.annotations.Api;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ParseRequestService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.roleassignment.feignclients.DataStoreFeignClient;
import uk.gov.hmcts.reform.roleassignment.util.ValidationUtil;
import uk.gov.hmcts.reform.roleassignment.v1.V1;

@Api(value = "roles")
@RestController
public class GetAssignmentController {
    //getAssignmentsbyActorId

    private final ParseRequestService parseRequestService;
    private final PersistenceService persistenceService;
    private final DataStoreFeignClient dataStoreFeignClient;

    public GetAssignmentController(ParseRequestService parseRequestService, PersistenceService persistenceService,
                                   DataStoreFeignClient dataStoreFeignClient) {
        this.parseRequestService = parseRequestService;
        this.persistenceService = persistenceService;
        this.dataStoreFeignClient = dataStoreFeignClient;
    }

    @PostMapping("/processRequest")
    public ResponseEntity<String> processRequest(@Validated @RequestBody AssignmentRequest assignmentRequest) {
        if (!ValidationUtil.validateAssignmentRequest(assignmentRequest)) {
            throw new BadRequestException(V1.Error.INVALID_REQUEST);
        }
        // service call to store request and requested roles in db for audit purpose.
        persistenceService.persistRequest(assignmentRequest);

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
    public String getCaseDataV2(@PathVariable("caseId") String caseId) {
        return dataStoreFeignClient.getCaseDataV2(caseId);
    }
}
