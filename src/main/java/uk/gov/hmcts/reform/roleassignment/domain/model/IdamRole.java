package uk.gov.hmcts.reform.roleassignment.domain.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public  class IdamRole
{
    public String userId;
    public String role;
}
