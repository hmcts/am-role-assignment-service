
package uk.gov.hmcts.reform.roleassignment.controller.endpoints;

import io.swagger.annotations.Api;
import org.kie.api.runtime.StatelessKieSession;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.roleassignment.domain.model.ValidationModel;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.GlobalService;
import java.util.ArrayList;
import java.util.List;


@Api(value = "roles")
@RestController
public class RoleAssignmentController  {

    private StatelessKieSession kieSession;

    private GlobalService globalService;

    public RoleAssignmentController(StatelessKieSession kieSession, GlobalService globalService) {
        this.kieSession = kieSession;
        this.globalService = globalService;
    }


    @PostMapping("/requestedRole")
    public String processRequest(@RequestBody ValidationModel validationModel) throws Exception {
        List<Object> facts = new ArrayList<>();
        facts.add(validationModel.request);
        facts.addAll(validationModel.assignmentsRequested);
        globalService.addExistingRoleAssignments(validationModel, facts);
        // Run the rules
        kieSession.setGlobal("services", globalService);
        kieSession.execute(facts);
        globalService.updateRequestStatus(validationModel);

        return "OK";

    }


}
