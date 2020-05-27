package uk.gov.hmcts.reform.roleassignment.domain.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class Case
{
	public String id;
	public String jurisdiction;
	public String type;
	public Classification classification;
	public String region;
	public String location;
	public String organisationId;
}
