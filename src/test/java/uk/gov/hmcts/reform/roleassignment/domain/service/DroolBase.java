package uk.gov.hmcts.reform.roleassignment.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;
import org.mockito.Mock;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.Case;
import uk.gov.hmcts.reform.roleassignment.domain.model.Request;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleConfig;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RequestType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.RetrieveDataService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public abstract class DroolBase {

    StatelessKieSession kieSession;
    AssignmentRequest assignmentRequest;
    List<Object> facts;
    @Mock
    private RetrieveDataService retrieveDataService = mock(RetrieveDataService.class);

    @BeforeEach
    public void setUp() {

        //list of facts
        facts = new ArrayList<>();

        //build assignmentRequest
        assignmentRequest = getAssignmentRequest()
            .build();

        // facts must contain the request
//        facts.add(assignmentRequest.getRequest());

        // facts must contain the role config, for access to the patterns
        facts.add(RoleConfig.getRoleConfig());

        //mock the retrieveDataService to fetch the Case Object
        Case caseObj = Case.builder().id("1234567890123456")
            .caseTypeId("Asylum")
            .jurisdiction("IA")
            .build();
        doReturn(caseObj).when(retrieveDataService).getCaseById("1234567890123456");

        // Set up the rule engine for validation.
        KieServices ks = KieServices.Factory.get();
        KieContainer kieContainer = ks.getKieClasspathContainer();
        this.kieSession = kieContainer.newStatelessKieSession("role-assignment-validation-session");
        this.kieSession.setGlobal("DATA_SERVICE", retrieveDataService);

    }

    private AssignmentRequest.AssignmentRequestBuilder getAssignmentRequest() {
        return AssignmentRequest.builder().request(Request.builder()
                                                       .id(UUID.fromString("ab4e8c21-27a0-4abd-aed8-810fdce22adb"))
                                                       .authenticatedUserId("4772dc44-268f-4d0c-8f83-f0fb662aac84")
                                                       .correlationId("38a90097-434e-47ee-8ea1-9ea2a267f51d")
                                                       .assignerId("4772dc44-268f-4d0c-8f83-f0fb662aac84")
                                                       .requestType(RequestType.CREATE)
                                                       .reference("4772dc44-268f-4d0c-8f83-f0fb662aac84")
                                                       .process(("p2"))
                                                       .replaceExisting(true)
                                                       .status(Status.CREATED)
                                                       .created(LocalDateTime.now())
                                                       .build())
            //.requestedRoles(getRequestedRoles())
            ;
    }

    private List<RoleAssignment> getRequestedRoles() {
        return Arrays.asList();
    }

}
