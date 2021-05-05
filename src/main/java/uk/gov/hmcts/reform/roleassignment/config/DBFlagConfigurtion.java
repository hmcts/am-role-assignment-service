package uk.gov.hmcts.reform.roleassignment.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.FeatureFlagEnum;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PersistenceService;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class DBFlagConfigurtion implements CommandLineRunner {

    @Autowired
    PersistenceService persistenceService;

    @Value("${launchdarkly.sdk.environment}")
    private String environment;


    private static ConcurrentHashMap<String, Boolean> droolFlagStates = new ConcurrentHashMap<>();


    public static ConcurrentHashMap<String, Boolean> getDroolFlagStates() {
        return droolFlagStates;
    }

    @Override
    public void run(String... args) throws Exception {

        for (FeatureFlagEnum featureFlagEnum : FeatureFlagEnum.values()) {
            Boolean status = persistenceService.getStatusByParam(featureFlagEnum.getValue(), environment);
            droolFlagStates.put(featureFlagEnum.getValue(), status);


        }

    }
}
