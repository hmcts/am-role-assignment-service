package uk.gov.hmcts.reform.roleassignment.befta;

import uk.gov.hmcts.befta.DefaultTestAutomationAdapter;
import uk.gov.hmcts.befta.dse.ccd.TestDataLoaderToDefinitionStore;
import uk.gov.hmcts.befta.player.BackEndFunctionalTestScenarioContext;

import java.util.Date;
import java.util.UUID;

public class RoleAssignmentTestAutomationAdapter extends DefaultTestAutomationAdapter {
    public static RoleAssignmentTestAutomationAdapter INSTANCE = new RoleAssignmentTestAutomationAdapter();
    private TestDataLoaderToDefinitionStore loader = new TestDataLoaderToDefinitionStore(this);

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
        long currentTime = new Date().getTime();
        String time = Long.toString(currentTime);
        return time + ("0000000000000000".substring(time.length()));
    }
}
