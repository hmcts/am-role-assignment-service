package uk.gov.hmcts.reform.roleassignment.domain.model;

import java.time.LocalDateTime;

public class Request
{
	public enum Action
	{
		CREATE, DELETE
	}

	public enum Status
	{
		CREATED, APPROVED, REJECTED
	}

	public String id;
	public String correlationId;
	public String clientId;
	public String authenticatedUserId;
	public String requestorId;
	public Action action;
	public Status status;
	public String process;
	public String reference;
	public boolean replaceExisting;
	public String roleAssignmentId;
	public LocalDateTime timestamp;
}
