package uk.gov.hmcts.reform.roleassignment.launchdarkly;

public enum FeatureFlagEnum {
    LDFLAG("get-ld-flag"), IACFLAG("ras_drool_iac_flag_1_0"),
    JUDICIALFLAG("ras_drool_judicial_flag_1_0");

    private final String value;

    FeatureFlagEnum(String value) {
        this.value = value;
    }


    public String getValue() {
        return value;
    }



}
