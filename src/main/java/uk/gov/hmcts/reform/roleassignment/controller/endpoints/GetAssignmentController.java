
package uk.gov.hmcts.reform.roleassignment.controller.endpoints;

import io.swagger.annotations.Api;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ParseRequestService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.roleassignment.v1.V1;

import javax.validation.Valid;

@Api(value = "roles")
@RestController
public class GetAssignmentController {
    //getAssignmentsbyActorId

    private ParseRequestService parseRequestService;
    private PersistenceService persistenceService;

    public GetAssignmentController(ParseRequestService parseRequestService, PersistenceService persistenceService) {
        this.parseRequestService = parseRequestService;
        this.persistenceService = persistenceService;
    }

    @PostMapping("/processRequest")
    public ResponseEntity<String> processRequest(@Valid @RequestBody RoleAssignmentRequest roleAssignmentRequest) {
        if (!parseRequestService.parseRequest(roleAssignmentRequest)) {
            throw new BadRequestException(V1.Error.INVALID_REQUEST);
        }
        // service call to store request and requested roles in db for audit purpose.
        persistenceService.persistRequestAndRequestedRoles(roleAssignmentRequest);

        return ResponseEntity.ok("Success");

    }
}
