package uk.gov.hmcts.reform.assignment.domain.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public  class IdamRole {
    private String userId;
    private String role;
}
