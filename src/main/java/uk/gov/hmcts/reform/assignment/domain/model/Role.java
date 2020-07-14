package uk.gov.hmcts.reform.assignment.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.assignment.domain.model.enums.RoleCategory;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Role {
    String name;
    String label;
    String description;
    RoleCategory category;
    public List<Pattern> patterns;

}
