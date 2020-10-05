package uk.gov.hmcts.reform.roleassignment.domain.service.queryroles;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignment.domain.model.QueryRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PersistenceService;

import java.util.List;

@Service
@AllArgsConstructor
public class QueryRoleAssignmentOrchestrator {

    private final PersistenceService persistenceService;


    public ResponseEntity<Object> retrieveRoleAssignmentsByQueryRequest(QueryRequest queryRequest, Integer pageNumber,
                                                                        Integer size, String sort, String direction) {

        List<RoleAssignment> assignmentList =
            persistenceService.retrieveRoleAssignmentsByQueryRequest(queryRequest, pageNumber, size, sort, direction);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Total-Records",
                            Long.toString(persistenceService.roleAssignmentEntities.getTotalElements()));

        return new ResponseEntity<>(assignmentList, responseHeaders, HttpStatus.OK);

    }
}
