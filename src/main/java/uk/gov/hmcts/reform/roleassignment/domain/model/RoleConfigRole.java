package uk.gov.hmcts.reform.roleassignment.domain.model;

import lombok.Getter;
import lombok.Value;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleCategory;

import java.util.LinkedHashSet;

@Value
@Getter
public class RoleConfigRole {

    private final String name;
    private final String label;
    private final String description;
    private final RoleCategory category;
    private final LinkedHashSet<RoleConfigPattern> patterns;
}
