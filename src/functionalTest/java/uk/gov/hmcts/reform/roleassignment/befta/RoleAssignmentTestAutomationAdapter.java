package uk.gov.hmcts.reform.roleassignment.befta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.befta.DefaultTestAutomationAdapter;
import uk.gov.hmcts.befta.player.BackEndFunctionalTestScenarioContext;

import java.util.Date;
import java.util.UUID;

public class RoleAssignmentTestAutomationAdapter extends DefaultTestAutomationAdapter {

    private static final Logger logger = LoggerFactory.getLogger(RoleAssignmentTestAutomationAdapter.class);

    //private TestDataLoaderToDefinitionStore loader = new TestDataLoaderToDefinitionStore(this);
    private TestDataLoaderToDefinitionStoreRAS loader = new TestDataLoaderToDefinitionStoreRAS(this);

    @Override
    public void doLoadTestData() {
        //Needed for the BEFTA implementation
        /*
        //CcdRoleConfig ccdRoleConfig = new CcdRoleConfig("caseworker-ia", "PUBLIC");
        //loader.addNewCcdRole(ccdRoleConfig);

        Resource resource = new ClassPathResource("ccd-iac-integration-dev.xlsx");
        String fileResourcePath = null;
        try {
            fileResourcePath = resource.getURL().getPath();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            loader.importNewDefinition(fileResourcePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
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
        long currentTime = new Date().getTime();
        String time = Long.toString(currentTime);
        return time + ("0000000000000000".substring(time.length()));
    }
}
