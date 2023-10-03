package uk.gov.hmcts.reform.roleassignment.domain.service.common;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.microsoft.applicationinsights.TelemetryClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignment.data.RoleAssignmentRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserCountService {

    @Autowired
    private TelemetryClient telemetryClient;

    @Autowired
    private RoleAssignmentRepository roleAssignmentRepository;

    public Map<String, Object> getOrgUserCount() throws JsonProcessingException {

        String timestamp = LocalDateTime.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

        List<RoleAssignmentRepository.JurisdictionRoleCategoryAndCount> orgUserCountByJurisdiction =
            roleAssignmentRepository.getOrgUserCountByJurisdiction();

        Map<String, String> properties = Map.of(
            "result", ow.writeValueAsString(orgUserCountByJurisdiction),
            "timestamp", timestamp
        );

        telemetryClient.trackEvent("orgUserCountByJurisdiction", properties,null);

        Map<String, Object> counts1 = new HashMap<>();
        counts1.put("OrgUserCountByJurisdiction", orgUserCountByJurisdiction);
        log.debug(ow.writeValueAsString(counts1));



        List<RoleAssignmentRepository.JurisdictionRoleCategoryNameAndCount> orgUserCountByJurisdictionAndRoleName =
            roleAssignmentRepository.getOrgUserCountByJurisdictionAndRoleName();

        List<Map<String, String>> eventList = getEventMapList(orgUserCountByJurisdictionAndRoleName, timestamp);
        eventList.stream().forEach(
            event -> telemetryClient.trackEvent("orgUserCountByJurisdictionAndRoleName", event,null));

        Map<String, Object> counts2 = new HashMap<>();
        counts2.put("OrgUserCountByJurisdictionAndRoleName", orgUserCountByJurisdictionAndRoleName);
        log.debug(ow.writeValueAsString(counts2));

        return Map.of(
            "OrgUserCountByJurisdiction", orgUserCountByJurisdiction,
            "OrgUserCountByJurisdictionAndRoleName", orgUserCountByJurisdictionAndRoleName);
    }

    public List<Map<String, String>> getEventMapList(
        List<RoleAssignmentRepository.JurisdictionRoleCategoryNameAndCount> rows,  String timestamp)
        throws JsonProcessingException {

        List<String> jurisdictions = getDistinctJurisdictions(rows);
        List<Map<String, String>> eventList = new ArrayList<>();
        ObjectWriter ow = new ObjectMapper().writer();

        for (String jurisdiction : jurisdictions) {
            List<RoleAssignmentRepository.JurisdictionRoleCategoryNameAndCount> jurisdictionRows =
                filterRowsByJurisdiction(rows, jurisdiction);
            String results = ow.writeValueAsString(jurisdictionRows);

            eventList.add(getEventMap(jurisdiction,results,timestamp));
        }

        return eventList;
    }

    private Map<String, String> getEventMap(String jurisdiction, String results, String timestamp) {
        Map<String, String> eventMap;
        if (jurisdiction != null) {
            eventMap = Map.of(
                "jurisdictionFilter", jurisdiction,
                "results", results,
                "timestamp", timestamp);
        } else {
            eventMap = Map.of(
                "jurisdictionFilter", "NULL",
                "results", results,
                "timestamp", timestamp);
        }
        return eventMap;
    }

    public List<RoleAssignmentRepository.JurisdictionRoleCategoryNameAndCount> filterRowsByJurisdiction(
        List<RoleAssignmentRepository.JurisdictionRoleCategoryNameAndCount> rows, String jurisdiction) {
        return jurisdiction != null ? rows.stream().filter(r -> r.getJurisdiction() != null
            && r.getJurisdiction().equals(jurisdiction)).collect(Collectors.toList()) :
            rows.stream().filter(r -> r.getJurisdiction() == null).collect(Collectors.toList());
    }

    public List<String> getDistinctJurisdictions(
        List<RoleAssignmentRepository.JurisdictionRoleCategoryNameAndCount> rows) {
        Set<String> jurisdictions = new HashSet<>(rows.size());
        rows.stream().forEach(r -> jurisdictions.add(r.getJurisdiction()));
        return new ArrayList<>(jurisdictions);
    }
}
