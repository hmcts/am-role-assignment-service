package uk.gov.hmcts.reform.roleassignment.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Classification;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Role {
    String name;
    String id;
    String description;
    RoleType roleType;
    Classification classification;
}
