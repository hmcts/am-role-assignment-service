package uk.gov.hmcts.reform.roleassignment.domain.service.drools;

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
import uk.gov.hmcts.reform.roleassignment.feignclients.configuration.DataStoreApiFallback;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.roleassignment.feignclients.configuration.DataStoreApiFallback.EMPLOYMENT_CASE_ID;
import static uk.gov.hmcts.reform.roleassignment.feignclients.configuration.DataStoreApiFallback.EMPLOYMENT_SCTL_CASE_ID;
import static uk.gov.hmcts.reform.roleassignment.feignclients.configuration.DataStoreApiFallback.PRIVATE_LAW_CASE_ID;
import static uk.gov.hmcts.reform.roleassignment.feignclients.configuration.DataStoreApiFallback.SSCS_CASE_ID;
import static uk.gov.hmcts.reform.roleassignment.feignclients.configuration.DataStoreApiFallback.CIVIL_CASE_ID;
import static uk.gov.hmcts.reform.roleassignment.feignclients.configuration.DataStoreApiFallback.PUBLIC_LAW_CASE_ID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


public abstract class DroolBase {

    StatelessKieSession kieSession;
    AssignmentRequest assignmentRequest;
    List<Object> facts;
    List<FeatureFlag> featureFlags;

    public static final String IA_CASE_ID = "1234567890123450";

    private final RetrieveDataService retrieveDataService = mock(RetrieveDataService.class);
    Map<String, Case> caseMap = Map.of("IA", Case.builder()
                                           .id(IA_CASE_ID)
                                           .jurisdiction("IA")
                                           .caseTypeId("Asylum")
                                           .build(),

                                       "SSCS", Case.builder()
                                           .id(SSCS_CASE_ID)
                                           .jurisdiction("SSCS")
                                           .caseTypeId("Benefit")
                                           .build(),

                                       "CIVIL", Case.builder()
                                           .id(CIVIL_CASE_ID)
                                           .jurisdiction("CIVIL")
                                           .caseTypeId("CIVIL")
                                           .build(),

                                       "PRIVATELAW", Case.builder()
                                           .id(PRIVATE_LAW_CASE_ID)
                                           .jurisdiction("PRIVATELAW")
                                           .caseTypeId("PRLAPPS")
                                           .build(),

                                       "PUBLICLAW", Case.builder()
                                           .id(PUBLIC_LAW_CASE_ID)
                                           .jurisdiction("PUBLICLAW")
                                           .caseTypeId("CARE_SUPERVISION_EPO")
                                           .build(),

                                       "EMPLOYMENT", Case.builder()
                                           .id(EMPLOYMENT_CASE_ID)
                                           .jurisdiction("EMPLOYMENT")
                                           .caseTypeId("ET_EnglandWales")
                                           .build(),

                                       "EMPLOYMENT|ET_Scotland", Case.builder()
                                           .id(EMPLOYMENT_SCTL_CASE_ID)
                                           .jurisdiction("EMPLOYMENT")
                                           .caseTypeId("ET_Scotland")
                                           .build()
    );

    @BeforeEach
    public void setUp() {

        //list of facts
        facts = new ArrayList<>();
        featureFlags = new ArrayList<>();

        //build assignmentRequest
        assignmentRequest = TestDataBuilder.getAssignmentRequest().build();

        //mock the retrieveDataService to fetch the Case Object
        DataStoreApiFallback dummyCases = new DataStoreApiFallback();
        //IA
        doReturn(dummyCases.getCaseDataV2(IA_CASE_ID))
            .when(retrieveDataService).getCaseById(IA_CASE_ID);

        //SSCS
        doReturn(dummyCases.getCaseDataV2(SSCS_CASE_ID))
            .when(retrieveDataService).getCaseById(SSCS_CASE_ID);

        //CIVIL
        doReturn(dummyCases.getCaseDataV2(CIVIL_CASE_ID))
            .when(retrieveDataService).getCaseById(CIVIL_CASE_ID);

        //PRIVATELAW
        doReturn(dummyCases.getCaseDataV2(PRIVATE_LAW_CASE_ID))
            .when(retrieveDataService).getCaseById(PRIVATE_LAW_CASE_ID);

        //PUBLICLAW
        doReturn(dummyCases.getCaseDataV2(PUBLIC_LAW_CASE_ID))
            .when(retrieveDataService).getCaseById(PUBLIC_LAW_CASE_ID);

        //EMPLOYMENT
        doReturn(dummyCases.getCaseDataV2(EMPLOYMENT_CASE_ID))
            .when(retrieveDataService).getCaseById(EMPLOYMENT_CASE_ID);
        doReturn(dummyCases.getCaseDataV2(EMPLOYMENT_SCTL_CASE_ID))
            .when(retrieveDataService).getCaseById(EMPLOYMENT_SCTL_CASE_ID);

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

        Case caseObj3 = Case.builder().id("1234567890123459")
            .jurisdiction("CMC")
            .caseTypeId("Asylum")
            .securityClassification(Classification.PUBLIC)
            .build();
        doReturn(caseObj3).when(retrieveDataService).getCaseById("1234567890123459");

        // Set up the rule engine for validation.
        KieServices ks = KieServices.Factory.get();
        KieContainer kieContainer = ks.getKieClasspathContainer();
        this.kieSession = kieContainer.newStatelessKieSession("role-assignment-validation-session");
        this.kieSession.setGlobal("DATA_SERVICE", retrieveDataService);

    }

    Case getCaseFromMap(String jurisdiction, String caseType) {
        String key = jurisdiction + "|" + caseType;
        if (!caseMap.containsKey(key)) {
            // fallback to search on jurisdiction only
            key = jurisdiction;
        }
        return caseMap.get(key);
    }

    void buildExecuteKieSession() {
        executeDroolRules(Collections.emptyList());
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
