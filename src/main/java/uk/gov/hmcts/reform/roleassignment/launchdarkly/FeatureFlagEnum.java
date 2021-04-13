package uk.gov.hmcts.reform.roleassignment.launchdarkly;

public enum FeatureFlagEnum {
    LD_FLAG("get-ld-flag"), IAC_FLAG("ras_drool_iac_flag_1_0"),
    JUDICIAL_FLAG("orm-jrd-org-role");

    private final String value;

    FeatureFlagEnum(String value) {
        this.value = value;
    }


    public String getValue() {
        return value;
    }



}
