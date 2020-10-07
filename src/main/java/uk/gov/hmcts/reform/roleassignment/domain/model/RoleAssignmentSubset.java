package uk.gov.hmcts.reform.roleassignment.domain.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.ActorIdType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Classification;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;

import java.time.LocalDateTime;
import java.util.Map;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class RoleAssignmentSubset {

    private ActorIdType actorIdType;
    private String actorId;
    private RoleType roleType;
    private String roleName;
    private Classification classification;
    private GrantType grantType;
    private RoleCategory roleCategory;
    private boolean readOnly;
    private Map<String, JsonNode> attributes;

    @EqualsAndHashCode.Exclude
    private JsonNode notes;

    private LocalDateTime beginTime;
    private LocalDateTime endTime;

}
