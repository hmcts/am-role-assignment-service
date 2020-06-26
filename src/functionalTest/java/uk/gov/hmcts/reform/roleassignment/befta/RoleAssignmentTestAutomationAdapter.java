package uk.gov.hmcts.reform.roleassignment.befta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.befta.DefaultTestAutomationAdapter;
import uk.gov.hmcts.befta.dse.ccd.TestDataLoaderToDefinitionStore;

public class RoleAssignmentTestAutomationAdapter extends DefaultTestAutomationAdapter {

    private static final Logger logger = LoggerFactory.getLogger(RoleAssignmentTestAutomationAdapter.class);

    private TestDataLoaderToDefinitionStore loader = new TestDataLoaderToDefinitionStore(this);

    @Override
    public void doLoadTestData() {
        super.registerApiClientWithEnvVariable("API_CLIENT_BOOKING_SERVICE");
        super.registerApiClientWithEnvVariable("API_CLIENT_ORG_MAPPING_SERVICE");
        super.registerApiClientWithEnvVariable("API_CLIENT_DATA_STORE");
    }
}
