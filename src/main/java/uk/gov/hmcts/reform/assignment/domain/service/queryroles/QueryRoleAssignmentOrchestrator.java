package uk.gov.hmcts.reform.assignment.domain.service.queryroles;

public class QueryRoleAssignmentOrchestrator {
    //1. call parse request service
    //2. Call retrieve Data service to fetch all required objects
    //3. Call Validation model service to create aggregation objects and apply drools validation rule
    //4. Call persistence to fetch requested assignment records
    //5. Call prepare response to make HATEOUS based response.
}
