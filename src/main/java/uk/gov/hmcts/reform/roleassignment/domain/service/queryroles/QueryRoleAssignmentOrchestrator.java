package uk.gov.hmcts.reform.roleassignment.domain.service.queryroles;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignment.domain.model.Assignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.QueryRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentResource;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PersistenceService;

import java.util.List;

@Service
@AllArgsConstructor
public class QueryRoleAssignmentOrchestrator {

    private static final Logger logger = LoggerFactory.getLogger(QueryRoleAssignmentOrchestrator.class);
    private final PersistenceService persistenceService;


    public  ResponseEntity<RoleAssignmentResource> retrieveRoleAssignmentsByQueryRequest(QueryRequest queryRequest,
                                                                                   Integer pageNumber,
                                                                        Integer size, String sort, String direction) {

        long startTime = System.currentTimeMillis();

        List<Assignment> assignmentList =
            persistenceService.retrieveRoleAssignmentsByQueryRequest(
                queryRequest,
                pageNumber,
                size,
                sort,
                direction,
                false
            );
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(
            "Total-Records",
            Long.toString(persistenceService.getTotalRecords())
        );
        logger.debug(
            " >> retrieveRoleAssignmentsByQueryRequest execution finished at {} . Time taken = {} milliseconds",
            System.currentTimeMillis(),
            Math.subtractExact(System.currentTimeMillis(), startTime)
        );
        return ResponseEntity.status(HttpStatus.OK).headers(responseHeaders).body(
            new RoleAssignmentResource(assignmentList));

    }
}
