package uk.gov.hmcts.reform.roleassignment.domain.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.ActorIdType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Classification;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public  class RoleAssignment {
    public UUID id;
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
    public Request request;
}
