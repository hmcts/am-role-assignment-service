package uk.gov.hmcts.reform.roleassignment.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.ActorIdType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Classification;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoleAssignment {
    public String id;
    public ActorIdType actorIdType;
    public String actorId;
    public RoleType roleType;
    public String roleName;
    public Classification classification;
    public GrantType grantType;
    public boolean readOnly;
    public LocalDateTime beginTime;
    public LocalDateTime endTime;
    public LocalDateTime created;
    public Map<String, String> attributes;
}
