package uk.gov.hmcts.reform.roleassignment.domain.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.ActorIdType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Classification;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleAssignmentSubset {


    public ActorIdType actorIdType;
    public UUID actorId;
    public RoleType roleType;
    public String roleName;
    public Classification classification;
    public GrantType grantType;
    public RoleCategory roleCategory;
    public boolean readOnly;
    public Map<String, JsonNode> attributes;
    public JsonNode notes;
    public LocalDateTime beginTime;
    public LocalDateTime endTime;
}
