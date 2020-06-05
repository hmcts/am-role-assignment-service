package uk.gov.hmcts.reform.roleassignment.domain.model;

import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Classification;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;

public class Role {
    String name;
    String id;
    String description;
    RoleType roleType;
    Classification classification;
}
