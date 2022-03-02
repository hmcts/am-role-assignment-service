package uk.gov.hmcts.reform.roleassignment.domain.service.drools;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.Case;
import uk.gov.hmcts.reform.roleassignment.domain.model.ExistingRoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.FeatureFlag;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleConfig;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Classification;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.RetrieveDataService;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.roleassignment.util.JacksonUtils.convertValueJsonNode;
import static uk.gov.hmcts.reform.roleassignment.domain.model.Case.REGION;
import static uk.gov.hmcts.reform.roleassignment.domain.model.Case.BASE_LOCATION;
import static uk.gov.hmcts.reform.roleassignment.domain.model.Case.CASE_MANAGEMENT_LOCATION;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class DroolBase {

    StatelessKieSession kieSession;
    AssignmentRequest assignmentRequest;
    List<Object> facts;
    List<FeatureFlag> featureFlags;

    private final RetrieveDataService retrieveDataService = mock(RetrieveDataService.class);

    @BeforeEach
    public void setUp() {

        //list of facts
        facts = new ArrayList<>();
        featureFlags = new ArrayList<>();

        //build assignmentRequest
        assignmentRequest = TestDataBuilder.getAssignmentRequest()
            .build();

        //mock the retrieveDataService to fetch the Case Object
        Case caseObj = Case.builder().id("1234567890123456")
            .caseTypeId("Asylum")
            .jurisdiction("IA")
            .securityClassification(Classification.PUBLIC)
            .build();
        doReturn(caseObj).when(retrieveDataService).getCaseById("1234567890123456");

        Case caseObj0 = Case.builder().id("9234567890123456")
            .caseTypeId("Asylum")
            .jurisdiction("IA")
            .securityClassification(Classification.PRIVATE)
            .build();
        doReturn(caseObj0).when(retrieveDataService).getCaseById("9234567890123456");

        //mock the retrieveDataService to fetch the Case Object with incorrect type ID
        Case caseObj1 = Case.builder().id("1234567890123457")
            .caseTypeId("Not Asylum")
            .jurisdiction("IA")
            .securityClassification(Classification.PUBLIC)
            .build();
        doReturn(caseObj1).when(retrieveDataService).getCaseById("1234567890123457");

        //mock the retrieveDataService to fetch the Case Object with incorrect Jurisdiction ID
        Case caseObj2 = Case.builder().id("1234567890123458")
            .caseTypeId("Asylum")
            .jurisdiction("Not IA")
            .securityClassification(Classification.PUBLIC)
            .build();
        doReturn(caseObj2).when(retrieveDataService).getCaseById("1234567890123458");

        Case caseObj3 = Case.builder().id("1234567890123459")
            .jurisdiction("CMC")
            .caseTypeId("Asylum")
            .securityClassification(Classification.PUBLIC)
            .build();
        doReturn(caseObj3).when(retrieveDataService).getCaseById("1234567890123459");

        HashMap<String, JsonNode> caseAttributes = new HashMap<>();
        caseAttributes.put(REGION, convertValueJsonNode("south-east"));
        caseAttributes.put(BASE_LOCATION, convertValueJsonNode("London"));

        Case caseObj4 = Case.builder().id("1616161616161616")
            .caseTypeId("Asylum")
            .jurisdiction("IA")
            .securityClassification(Classification.PUBLIC)
            .data(Map.of(CASE_MANAGEMENT_LOCATION, convertValueJsonNode(caseAttributes)))
            .build();
        doReturn(caseObj4).when(retrieveDataService).getCaseById("1616161616161616");

        // Set up the rule engine for validation.
        KieServices ks = KieServices.Factory.get();
        KieContainer kieContainer = ks.getKieClasspathContainer();
        this.kieSession = kieContainer.newStatelessKieSession("role-assignment-validation-session");
        this.kieSession.setGlobal("DATA_SERVICE", retrieveDataService);

    }

    void buildExecuteKieSession() {
        // facts must contain the role config, for access to the patterns
        facts.add(RoleConfig.getRoleConfig());
        // facts must contain the request
        facts.add(assignmentRequest.getRequest());
        facts.addAll(featureFlags);
        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());
        // Run the rules
        kieSession.execute(facts);

        facts.clear();
        featureFlags.clear();
    }

    void executeDroolRules(List<ExistingRoleAssignment> existingRoleAssignments) {
        // facts must contain the role config, for access to the patterns
        facts.add(RoleConfig.getRoleConfig());
        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());

        // facts must contain all existing role assignments
        facts.addAll(existingRoleAssignments);

        // facts must contain the request
        facts.add(assignmentRequest.getRequest());

        facts.addAll(featureFlags);

        // Run the rules
        kieSession.execute(facts);

        //flush the facts/flags so parameterised tests can run multiple executions
        facts.clear();
        featureFlags.clear();
    }


}
