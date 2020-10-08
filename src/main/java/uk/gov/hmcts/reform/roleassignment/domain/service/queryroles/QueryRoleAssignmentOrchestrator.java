package uk.gov.hmcts.reform.roleassignment.domain.service.queryroles;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignment.domain.model.QueryRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PersistenceService;

import java.util.Date;
import java.util.List;

@Service
@AllArgsConstructor
public class QueryRoleAssignmentOrchestrator {

    private static final Logger logger = LoggerFactory.getLogger(QueryRoleAssignmentOrchestrator.class);
    private final PersistenceService persistenceService;


    public ResponseEntity<Object> retrieveRoleAssignmentsByQueryRequest(QueryRequest queryRequest, Integer pageNumber,
                                                                        Integer size, String sort, String direction) {

        long startTime = new Date().getTime();
        logger.info(String.format("retrieveRoleAssignmentsByQueryRequest execution started at %s", startTime));

        List<RoleAssignment> assignmentList =
            persistenceService.retrieveRoleAssignmentsByQueryRequest(queryRequest, pageNumber, size, sort, direction);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Total-Records",
                            Long.toString(persistenceService.getTotalRecords()));
        logger.info(String.format(
            "retrieveRoleAssignmentsByQueryRequest execution finished at %s . Time taken = %s milliseconds",
            new Date().getTime(),
            new Date().getTime() - startTime
        ));
        return new ResponseEntity<>(assignmentList, responseHeaders, HttpStatus.OK);

    }
}
