package uk.gov.hmcts.reform.roleassignmentrefactored.domain.service.queryroles;

import java.util.List;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignmentrefactored.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignmentrefactored.domain.service.common.ParseRequestService;
import uk.gov.hmcts.reform.roleassignmentrefactored.domain.service.common.PersistenceService;

@Service
@AllArgsConstructor
public class QueryRoleAssignmentOrchestrator {
    //1. call parse request service
    //2. Call retrieve Data service to fetch all required objects
    //3. Call Validation model service to create aggregation objects and apply drools validation rule
    //4. Call persistence to fetch requested assignment records
    //5. Call prepare response to make HATEOAS based response.

    private final PersistenceService persistenceService;
    private final ParseRequestService parseRequestService;

    public ResponseEntity<Object> retrieveRoleAssignmentsByActorIdAndCaseId(String actorId, String caseId,
                                                                            String roleType) {
        parseRequestService.validateGetAssignmentsByActorIdAndCaseId(actorId, caseId, roleType);

        List<RoleAssignment> assignmentList =
            persistenceService.getAssignmentsByActorAndCaseId(actorId, caseId, roleType);
        return ResponseEntity.status(HttpStatus.OK).body(assignmentList);
    }
}
