
package uk.gov.hmcts.reform.assignment.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppInsights {
    private static final String MODULE = "ROLE_ASSIGNMENT_SERVICE";

    //Get the commented code from history
    private static final Logger LOG = LoggerFactory.getLogger(AppInsights.class);

    void sampleMethod() {
        String caseId = "1234567812345678";
        LOG.info(caseId);
    }
}

