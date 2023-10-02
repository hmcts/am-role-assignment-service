
package uk.gov.hmcts.reform.roleassignment.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.microsoft.applicationinsights.TelemetryClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.roleassignment.data.RoleAssignmentRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.roleassignment.util.Constants.SERVICE_AUTHORIZATION2;

@Slf4j
@RestController
public class UserCountController {

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
    public ResponseEntity<Map<String, Object>> getOrgUserCount() throws JsonProcessingException {
        List<RoleAssignmentRepository.JurisdictionRoleCategoryAndCount> orgUserCountByJurisdiction =
            roleAssignmentRepository.getOrgUserCountByJurisdiction();
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

        Map<String, String> properties = Map.of(
            "orgUserCountByJurisdiction", ow.writeValueAsString(orgUserCountByJurisdiction)
        );
        telemetryClient.trackEvent("orgUserCountByJurisdiction", properties,null);

        Map<String, Object> counts1 = new HashMap<>();
        counts1.put("OrgUserCountByJurisdiction", orgUserCountByJurisdiction);
        log.debug(ow.writeValueAsString(counts1));



        List<RoleAssignmentRepository.JurisdictionRoleCategoryNameAndCount> orgUserCountByJurisdictionAndRoleName =
            roleAssignmentRepository.getOrgUserCountByJurisdictionAndRoleName();
        //"IA","SSCS", "CIVIL","PRIVATELAW","PUBLICLAW","EMPLOYMENT","ST_CIC"
        List<RoleAssignmentRepository.JurisdictionRoleCategoryNameAndCount> orgUserCountByJurisdictionIA =
            orgUserCountByJurisdictionAndRoleName.stream()
                .filter(row -> row.getJurisdiction() != null && row.getJurisdiction().equals("IA"))
                .collect(
                Collectors.toList());

        List<RoleAssignmentRepository.JurisdictionRoleCategoryNameAndCount> orgUserCountByJurisdictionNull =
            orgUserCountByJurisdictionAndRoleName.stream()
                .filter(row -> row.getJurisdiction() == null)
                .collect(
                    Collectors.toList());

        properties = Map.of(
            "orgUserCountByJurisdictionIA", ow.writeValueAsString(orgUserCountByJurisdictionIA),
            "orgUserCountByJurisdictionNull", ow.writeValueAsString(orgUserCountByJurisdictionNull)
        );

        telemetryClient.trackEvent("orgUserCountByJurisdiction", properties,null);

        Map<String, Object> counts2 = new HashMap<>();
        counts2.put("OrgUserCountByJurisdictionAndRoleName", orgUserCountByJurisdictionAndRoleName);
        log.debug(ow.writeValueAsString(counts2));

        return ResponseEntity.status(HttpStatus.OK).body(Map.of(
            "OrgUserCountByJurisdiction", orgUserCountByJurisdiction,
        "OrgUserCountByJurisdictionAndRoleName", orgUserCountByJurisdictionAndRoleName));
    }
}
