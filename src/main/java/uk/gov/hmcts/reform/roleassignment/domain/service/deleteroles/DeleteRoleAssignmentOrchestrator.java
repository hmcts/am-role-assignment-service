package uk.gov.hmcts.reform.roleassignment.domain.service.deleteroles;

import org.springframework.stereotype.Service;

@Service
public class DeleteRoleAssignmentOrchestrator {
    //1. call parse request service
    //2. Call persistence service to store the created records
    //3. Call Validation model service to create aggregation objects and apply drools validation rule
    //5. For Each: If success then call persistence service to update assignment record status
    //6. once all the assignment records are approved call persistence to update request status
    //7. Call persistence to move assignment records to Deleted status
    //8. Call the persistence to remove assignment records from RoleAssignmentLive table
}
