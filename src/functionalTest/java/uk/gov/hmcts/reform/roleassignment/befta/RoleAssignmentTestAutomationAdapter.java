package uk.gov.hmcts.reform.roleassignment.befta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.befta.DefaultTestAutomationAdapter;
import uk.gov.hmcts.befta.dse.ccd.TestDataLoaderToDefinitionStore;
import uk.gov.hmcts.befta.player.BackEndFunctionalTestScenarioContext;

import java.util.Date;
import java.util.UUID;

public class RoleAssignmentTestAutomationAdapter extends DefaultTestAutomationAdapter {

    private static final Logger logger = LoggerFactory.getLogger(RoleAssignmentTestAutomationAdapter.class);

    private TestDataLoaderToDefinitionStore loader = new TestDataLoaderToDefinitionStore(this);

    @Override
    public void doLoadTestData() {
    }

    @Override
    public Object calculateCustomValue(BackEndFunctionalTestScenarioContext scenarioContext, Object key) {
        //the docAMUrl is is referring the self link in PR
        switch (key.toString()) {
            case ("generateUUID"):
                return UUID.randomUUID();
            case ("generateCaseId"):
                return generateCaseId();
            default:
                return super.calculateCustomValue(scenarioContext, key);
        }
    }

    private Object generateCaseId() {
        Long currentTime = new Date().getTime();
        String time = currentTime.toString();
        String caseId = time + ("0000000000000000".substring(time.length()));
        return caseId;
    }
}
