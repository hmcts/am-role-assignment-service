package uk.gov.hmcts.reform.roleassignment.domain.model.enums;

public enum FeatureFlagEnum {

    getIAC_1_0("iac_1_0"), getIAC_1_1("iac_1_1");


    private final String value;

    FeatureFlagEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
