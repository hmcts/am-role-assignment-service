package uk.gov.hmcts.reform.roleassignment.domain.model;

import java.util.Set;

import lombok.Value;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleCategory;

@Value
public class RoleConfigRole {

	private final String name;
	private final String label;
	private final String description;
	private final RoleCategory category;
	private final Set<RoleConfigData> patterns;
}
