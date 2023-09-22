
package uk.gov.hmcts.reform.roleassignment.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.microsoft.applicationinsights.TelemetryClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.roleassignment.data.RoleAssignmentRepository;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.roleassignment.util.Constants.SERVICE_AUTHORIZATION2;

@Slf4j
@RestController
public class UserCountController {

    private static final Logger logger = LoggerFactory.getLogger(UserCountController.class);

    @Autowired
    private TelemetryClient telemetryClient;

    @Autowired
    private RoleAssignmentRepository roleAssignmentRepository;

    @GetMapping(
        path = "/am/role-assignments/user-count"
    )
    @Operation(summary = "Get User Count",
        security =
        {
            @SecurityRequirement(name = AUTHORIZATION),
            @SecurityRequirement(name = SERVICE_AUTHORIZATION2)
        })
    public ResponseEntity<Map<String, Object>> getUserCount() throws SQLException, JsonProcessingException {
        List<RoleAssignmentRepository.JurisdictionRoleCategoryAndCount> userCount =
            roleAssignmentRepository.getUserCount();
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        Map<String, Object> counts = new HashMap<>();
        counts.put("UserCount1", userCount);

        List<RoleAssignmentRepository.JurisdictionRoleCategoryNameAndCount> userCount2 =
            roleAssignmentRepository.getUserCount2();
        counts.put("UserCount2", userCount2);
        logger.warn(ow.writeValueAsString(counts));
        telemetryClient.trackEvent(ow.writeValueAsString(counts));
        return ResponseEntity.status(HttpStatus.OK).body(counts);
    }
}
