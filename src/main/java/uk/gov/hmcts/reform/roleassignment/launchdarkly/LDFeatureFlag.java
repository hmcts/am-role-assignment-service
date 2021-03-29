package uk.gov.hmcts.reform.roleassignment.launchdarkly;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LDFeatureFlag {

    String flagName;
    boolean status;





}
