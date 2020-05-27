package uk.gov.hmcts.reform.roleassignment.domain.model;

import java.time.LocalDateTime;
import java.util.Map;

public abstract class RoleAssignment
{
	public enum ActorIdType
	{
		IDAM_USER_ID
	}

	public enum RoleType
	{
		ORGANISATIONAL_ROLE, CASE_ROLE, IDAM_ROLE
	}

	public enum GrantType
	{
		SEARCH, STANDARD, SPECIFIC, CHALLENGED
	}

	public ActorIdType actorIdType = ActorIdType.IDAM_USER_ID;
	public String actorId;
	public RoleType roleType;
	public String roleName;
	public Classification classification;
	public GrantType grantType;
	public boolean readOnly;
	public LocalDateTime begin;
	public LocalDateTime end;
	public Map<String, String> attributes;
}
