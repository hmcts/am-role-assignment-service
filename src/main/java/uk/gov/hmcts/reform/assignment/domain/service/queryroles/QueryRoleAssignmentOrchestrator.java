package uk.gov.hmcts.reform.assignment.domain.service.queryroles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryRoleAssignmentOrchestrator {

    private static final Logger LOG = LoggerFactory.getLogger(QueryRoleAssignmentOrchestrator.class);

    //1. call parse request service
    //2. Call retrieve Data service to fetch all required objects
    //3. Call Validation model service to create aggregation objects and apply drools validation rule
    //4. Call persistence to fetch requested assignment records
    //5. Call prepare response to make HATEOUS based response.


    void sampleMethod() {
        String caseId = "1234567812345678";
        LOG.info(caseId);
    }
}
