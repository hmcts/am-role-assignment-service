package uk.gov.hmcts.reform.roleassignment.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QueryRequest {
    private List<String> actorId;
    private List<String> roleType;
    private List<String> roleName;
    private List<String> classification;
    private List<String> grantType;
    private ZonedDateTime validAt;
    private List<String> roleCategory;
    private Map<String, List<String>> attributes;
    private List<String> authorisations;

}
