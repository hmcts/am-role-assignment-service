package uk.gov.hmcts.reform.roleassignment.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QueryRequest {
    private List<UUID> actorId;
    private List<String> roleType;
    private List<String> roleName;
    private List<String> classification;
    private List<String> grantType;
    private LocalDateTime validAt;
    private List<String> roleCategorie;
    private Map<String, List<String>> attributes;
    private List<String> authorisations;

}
