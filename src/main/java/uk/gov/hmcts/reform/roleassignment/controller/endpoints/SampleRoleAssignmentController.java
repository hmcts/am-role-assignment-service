package uk.gov.hmcts.reform.roleassignment.controller.endpoints;

import org.kie.api.runtime.StatelessKieSession;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.RoleAssignmentService;

import java.util.ArrayList;
import java.util.List;

public class SampleRoleAssignmentController {
    private StatelessKieSession kieSession;

    private RoleAssignmentService roleAssignmentService;

    public SampleRoleAssignmentController(StatelessKieSession kieSession, RoleAssignmentService roleAssignmentService) {
        this.kieSession = kieSession;
        this.roleAssignmentService = roleAssignmentService;
    }


    @PostMapping("/requestedRole")
    public String processRequest(@RequestBody RoleAssignmentRequest roleAssignmentRequest) throws Exception {
        List<Object> facts = new ArrayList<>();
        facts.add(roleAssignmentRequest.roleRequest);
        facts.addAll(roleAssignmentRequest.requestedRoles);
        roleAssignmentService.addExistingRoleAssignments(roleAssignmentRequest, facts);
        // Run the rules
        kieSession.setGlobal("services", roleAssignmentService);
        kieSession.execute(facts);
        roleAssignmentService.updateRequestStatus(roleAssignmentRequest);

        return "OK";

    }
}
