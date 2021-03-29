/*
package uk.gov.hmcts.reform.roleassignment.launchdarkly;


import org.springframework.stereotype.Service;

@Service
public class FeatureFlagService {

  private  FeatureToggleService  featureToggleService;

    String flagName;
    boolean isEnabled;
    private LDFeatureFlag ldFeatureFlag;



    public FeatureFlagService(FeatureToggleService featureToggleService, LDFeatureFlag ldFeatureFlag, LDFeatureFlag ldFeatureFlag1) {
        this.featureToggleService = featureToggleService;

        this.ldFeatureFlag = ldFeatureFlag1;
    }

    public void isEnabled(){
       boolean flag = featureToggleService.isFlagEnabled(ldFeatureFlag.getFlagName());
       this.isEnabled = flag;

    }


}
*/
