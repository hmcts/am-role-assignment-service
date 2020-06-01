
package uk.gov.hmcts.reform.roleassignment.controller.endpoints;

import io.swagger.annotations.Api;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ParseRequestService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.StoreRequestService;
import uk.gov.hmcts.reform.roleassignment.v1.V1;

import javax.validation.Valid;

@Api(value = "roles")
@RestController
public class RoleAssignmentController {

    private ParseRequestService parseRequestService;
    private StoreRequestService storeRequestService;

    public RoleAssignmentController(ParseRequestService parseRequestService, StoreRequestService storeRequestService) {
        this.parseRequestService = parseRequestService;
        this.storeRequestService = storeRequestService;
    }

    @PostMapping("/processRequest")
    private ResponseEntity<String> processRequest(@Valid @RequestBody RoleAssignmentRequest roleAssignmentRequest) {
        if (!parseRequestService.parseRequest(roleAssignmentRequest)) {
            throw new BadRequestException(V1.Error.INVALID_REQUEST);
        }
        // service call to store request and requested roles in db for audit purpose.
        storeRequestService.persistRequestAndRequestedRoles(roleAssignmentRequest);

        return ResponseEntity.ok("Success");

    }
}
