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
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ParseRequestService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.roleassignment.util.Constants;
import uk.gov.hmcts.reform.roleassignment.util.ValidationUtil;

import java.util.List;

@Service
@AllArgsConstructor
public class QueryRoleAssignmentOrchestrator {

    private static final Logger logger = LoggerFactory.getLogger(QueryRoleAssignmentOrchestrator.class);
    private final PersistenceService persistenceService;
    private final ParseRequestService parseRequestService;


    public ResponseEntity<Object> retrieveRoleAssignmentsByQueryRequest(QueryRequest queryRequest, Integer pageNumber,
                                                                        Integer size, String sort, String direction) {

        long startTime = System.currentTimeMillis();
        logger.info(String.format("retrieveRoleAssignmentsByQueryRequest execution started at %s", startTime));


        ValidationUtil.validateId(Constants.NUMBER_TEXT_HYPHEN_PATTERN,
                                  parseRequestService.getCorrelationId());
        String correlationId = parseRequestService.getCorrelationId();

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Total-Records",
                            Long.toString(persistenceService.getTotalRecords()));
        responseHeaders.add(Constants.CORRELATION_ID_HEADER_NAME, correlationId);
        logger.info(String.format(
            "retrieveRoleAssignmentsByQueryRequest execution finished at %s . Time taken = %s milliseconds",
            System.currentTimeMillis(),
            System.currentTimeMillis() - startTime
        ));
        List<RoleAssignment> assignmentList =
            persistenceService.retrieveRoleAssignmentsByQueryRequest(queryRequest, pageNumber, size, sort, direction);
        return new ResponseEntity<>(assignmentList, responseHeaders, HttpStatus.OK);

    }
}
