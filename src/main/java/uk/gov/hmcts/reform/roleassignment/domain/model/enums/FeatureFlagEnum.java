package uk.gov.hmcts.reform.roleassignment.domain.model.enums;

public enum FeatureFlagEnum {
    IAC_1_0("iac_1_0"),
    IAC_1_1("iac_1_1"),
    CCD_1_0("ccd_1_0"),
    IAC_JRD_1_0("iac_jrd_1_0"),
    CCD_BYPASS_1_0("ccd_bypass_1_0"),
    IAC_SPECIFIC_1_0("iac_specific_1_0"),
    IAC_CHALLENGED_1_0("iac_challenged_1_0"),
    WA_BYPASS_1_0("wa_bypass_1_0"),
    SSCS_WA_1_0("sscs_wa_1_0");

    private final String value;

    FeatureFlagEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
