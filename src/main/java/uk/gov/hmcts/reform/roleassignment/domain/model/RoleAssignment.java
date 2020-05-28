package uk.gov.hmcts.reform.roleassignment.domain.model;


import uk.gov.hmcts.reform.roleassignment.domain.model.enums.ActorIdType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Classification;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;

import java.time.LocalDateTime;
import java.util.Map;

public abstract class RoleAssignment {
    public Long id;
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
