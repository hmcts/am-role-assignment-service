package uk.gov.hmcts.reform.roleassignment.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonCreator
    public RoleAssignment(@JsonProperty(value = "actorIdType") ActorIdType actorIdType,
                          @JsonProperty(value = "actorId") UUID actorId,
                          @JsonProperty(value = "roleType") RoleType roleType,
                          @JsonProperty(value = "roleName") String roleName,
                          @JsonProperty(value = "classification") Classification classification,
                          @JsonProperty(value = "grantType") GrantType grantType,
                          @JsonProperty(value = "status") Status status,
                          @JsonProperty(value = "readOnly") boolean readOnly,
                          @JsonProperty(value = "beginTime") LocalDateTime beginTime,
                          @JsonProperty(value = "endTime") LocalDateTime endTime,
                          @JsonProperty(value = "created") LocalDateTime created,
                          @JsonProperty(value = "attributes")Map<String, JsonNode> attributes) {
        this.actorIdType = actorIdType;
        this.actorId = actorId;
        this.roleType = roleType;
        this.roleName = roleName;
        this.classification = classification;
        this.grantType = grantType;
        this.status = status;
        this.readOnly = readOnly;
        this.beginTime = beginTime;
        this.endTime = endTime;
        this.created = created;
        this.attributes = attributes;

    }
}
