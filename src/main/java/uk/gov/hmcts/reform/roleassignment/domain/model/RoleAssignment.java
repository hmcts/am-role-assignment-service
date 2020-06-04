package uk.gov.hmcts.reform.roleassignment.domain.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.ActorIdType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Classification;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public  class RoleAssignment {
    public Long id;
    public ActorIdType actorIdType;
    public UUID actorId;
    public RoleType roleType;
    public String roleName;
    public Classification classification;
    public GrantType grantType;
    public Status status;
    public boolean readOnly;
    public LocalDateTime beginTime;
    public LocalDateTime endTime;
    public LocalDateTime created;
    public Map<String, JsonNode> attributes;
    public RoleRequest roleRequest;
}
