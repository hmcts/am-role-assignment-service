package uk.gov.hmcts.reform.roleassignment.befta;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.befta.TestAutomationAdapter;
import uk.gov.hmcts.befta.dse.ccd.CcdRoleConfig;
import uk.gov.hmcts.befta.dse.ccd.TestDataLoaderToDefinitionStore;

import java.io.IOException;

@Slf4j
public class TestDataLoaderToDefinitionStoreRAS extends TestDataLoaderToDefinitionStore {
    private static final Logger logger = LoggerFactory.getLogger(TestDataLoaderToDefinitionStoreRAS.class);

    public TestDataLoaderToDefinitionStoreRAS(TestAutomationAdapter adapter) {
        super(adapter);
    }

    protected void addNewCcdRole(CcdRoleConfig roleConfig){
        logger.info("\nAdding the role {}...", roleConfig);
        addCcdRole(roleConfig);
        logger.info("\nAdded the role {}...", roleConfig);
    }

    protected void importNewDefinition(String fileResourcePath) throws IOException {
        logger.info("\nImporting the file {}...", fileResourcePath);
        importDefinition(fileResourcePath);
        /*File file = new File(fileResourcePath);

        try {
            Response response = asAutoTestImporter().given().multiPart(file).when().post("/import");
            if (response.getStatusCode() != 201) {
                String message = "Import failed with response body: " + response.body().prettyPrint();
                message += "\nand http code: " + response.statusCode();
                throw new FunctionalTestException(message);
            }

        } finally {
            file.delete();
        }
        logger.info("\nImported the role {}...", fileResourcePath); */

    }
}
