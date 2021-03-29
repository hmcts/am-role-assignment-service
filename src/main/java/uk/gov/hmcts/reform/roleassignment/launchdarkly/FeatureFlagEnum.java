package uk.gov.hmcts.reform.roleassignment.launchdarkly;

public enum FeatureFlagEnum {
    getListOfRoles("get-list-of-roles"), createRoleAssignment("create-role-assignments");

    private final String value;

    FeatureFlagEnum( String value) {
        this.value = value;
    }


    public String getValue() {
        return value;
    }
}
