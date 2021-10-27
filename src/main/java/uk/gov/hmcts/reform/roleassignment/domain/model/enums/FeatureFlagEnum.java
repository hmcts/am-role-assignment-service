package uk.gov.hmcts.reform.roleassignment.domain.model.enums;

public enum FeatureFlagEnum {
    IAC_1_0("iac_1_0"), IAC_1_1("iac_1_1"), CCD_1_0("ccd_1_0"),IAC_JRD_1_0("iac_jrd_1_0"),
    CCD_BYPASS_1_0("ccd_bypass_1_0");

    private final String value;

    FeatureFlagEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
