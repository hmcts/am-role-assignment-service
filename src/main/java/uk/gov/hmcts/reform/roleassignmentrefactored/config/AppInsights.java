
package uk.gov.hmcts.reform.roleassignmentrefactored.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppInsights {

    //Get the commented code from history
    private static final Logger LOG = LoggerFactory.getLogger(AppInsights.class);

    void sampleMethod() {
        String caseId = "1234567812345678";
        LOG.info(caseId);
    }
}

