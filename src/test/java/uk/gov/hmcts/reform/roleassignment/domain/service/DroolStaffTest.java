package uk.gov.hmcts.reform.roleassignment.domain.service;

import org.junit.jupiter.api.Test;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleConfig;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RequestType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.RetrieveDataService;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.CREATE_REQUESTED;
import static uk.gov.hmcts.reform.roleassignment.util.JacksonUtils.convertValueJsonNode;

public class DroolStaffTest {



    @Test
    public void shouldValidateStaff() throws IOException {
        // Set up the rule engine for validation.
        KieServices ks = KieServices.Factory.get();
        KieContainer kContainer = ks.getKieClasspathContainer();
        StatelessKieSession kieSession = kContainer.newStatelessKieSession("role-assignment-validation-session");

        List<Object> facts = new ArrayList<>();
        AssignmentRequest assignmentRequest = TestDataBuilder.buildAssignmentRequest(Status.CREATED, CREATE_REQUESTED, false );
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment ->{
            roleAssignment.setRoleName("tribunal-caseworker");
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleCategory(RoleCategory.STAFF);
            roleAssignment.getAttributes().put("jurisdiction", convertValueJsonNode("IA"));
            roleAssignment.getAttributes().put("primaryLocation", convertValueJsonNode("abc"));
            roleAssignment.getAttributes().put("caseId", null);


        } );
         assignmentRequest.getRequestedRoles().stream().findFirst().get().setRoleName("senior-tribunal-caseworker");

        // facts must contain the request
        facts.add(assignmentRequest.getRequest());
        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());
        // facts must contain the role config, for access to the patterns
        facts.add(RoleConfig.getRoleConfig());



        // Run the rules
        kieSession.execute(facts);
        assignmentRequest.getRequestedRoles().stream().forEach(roleAssignment -> {
            assertEquals(Status.APPROVED,roleAssignment.getStatus());
        });

        System.out.println("Success execute drool rules");

    }
}
