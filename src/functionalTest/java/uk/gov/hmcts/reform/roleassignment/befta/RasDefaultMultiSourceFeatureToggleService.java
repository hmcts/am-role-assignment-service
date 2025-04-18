package uk.gov.hmcts.reform.roleassignment.befta;

import io.cucumber.java.Scenario;
import uk.gov.hmcts.befta.featuretoggle.DefaultMultiSourceFeatureToggleService;
import uk.gov.hmcts.befta.featuretoggle.FeatureToggleService;
import uk.gov.hmcts.befta.featuretoggle.ScenarioFeatureToggleInfo;

import java.util.stream.Stream;

public class RasDefaultMultiSourceFeatureToggleService extends DefaultMultiSourceFeatureToggleService {

    private static final String FEATURE_TOGGLE = "FeatureToggle";
    public static final RasDefaultMultiSourceFeatureToggleService INSTANCE
        = new RasDefaultMultiSourceFeatureToggleService();

    @Override
    @SuppressWarnings("unchecked")
    public ScenarioFeatureToggleInfo getToggleStatusFor(Scenario toggleable) {
        ScenarioFeatureToggleInfo scenarioFeatureToggleInfo = new ScenarioFeatureToggleInfo();
        // @FeatureToggle(RAS:feature_id_2=off)
        toggleable.getSourceTagNames().stream().filter(tag -> tag.contains(FEATURE_TOGGLE)).forEach(tag -> {

            String id = null;
            var domain = tag.contains(COLON) ? tag.substring(tag.indexOf("(") + 1, tag.indexOf(COLON)) : "LD";
            FeatureToggleService service = getToggleService(domain);

            if (!tag.contains(COLON) && !tag.contains(STRING_EQUALS)) {
                id = tag.substring(tag.indexOf("(") + 1, tag.indexOf(")"));
            } else if (tag.contains(COLON) && !tag.contains(STRING_EQUALS)) {
                id = tag.substring(tag.indexOf(COLON) + 1, tag.indexOf(")"));
            } else if (tag.contains(COLON) && tag.contains(STRING_EQUALS)) {
                id = tag.substring(tag.indexOf(COLON) + 1, tag.indexOf(STRING_EQUALS));
            }

            if (tag.contains(STRING_EQUALS)) {
                var expectedStatusString = tag.substring(tag.indexOf(STRING_EQUALS) + 1, tag.indexOf(")"));
                var expectedStatus = expectedStatusString.equalsIgnoreCase("on");
                scenarioFeatureToggleInfo.addExpectedStatus(id, expectedStatus);
            }
            var actualStatus = (Boolean) service.getToggleStatusFor(id);
            scenarioFeatureToggleInfo.addActualStatus(id, actualStatus);
        });
        return scenarioFeatureToggleInfo;
    }

    @Override
    protected FeatureToggleService getToggleService(String toggleDomain) {
        if (Stream.of("IAC", "RAS", "DB").anyMatch(toggleDomain::equalsIgnoreCase)) {
            return new RasFeatureToggleService();
        } else if (Stream.of("EV").anyMatch(toggleDomain::equalsIgnoreCase)) {
            return new RasEnvironmentVariableToggleService();
        } else {
            return super.getToggleService(toggleDomain);
        }
    }
}
