package uk.gov.hmcts.reform.roleassignment.drool.prmconfig;

import uk.gov.hmcts.reform.roleassignment.drool.model.PrmConfigTestArguments;

import java.util.ArrayList;
import java.util.List;

public class PossessionsPrmConfigIT {

    public static List<PrmConfigTestArguments> getAllTestArguments() {
        List<PrmConfigTestArguments> arguments = new ArrayList<>();

        // generate tests for all PCS professional GA-Group roles
        arguments.add(createTestArguments("any-case-type", "claimant", "PCS:all-cases:111:11111"));
        arguments.add(createTestArguments("any-case-type", "claimant-solicitor", "PCS:all-cases:222:22222"));
        arguments.add(createTestArguments("any-case-type", "defendant-solicitor", "PCS:all-cases:333:33333"));

        // generate tests for all PCS professional GA-Org roles
        arguments.add(createTestArguments("any-case-type", "duty-advisor-request", null));

        return arguments;
    }

    @SuppressWarnings({"SameParameterValue"})
    private static PrmConfigTestArguments createTestArguments(String caseType,
                                                              String roleName,
                                                              String caseAccessGroupId) {
        return PrmConfigTestArguments.builder()
            // default test properties
            .serviceName("Possessions")
            .jurisdiction("PCS")
            // test specific properties
            .caseType(caseType)
            .roleName(roleName)
            .caseAccessGroupId(caseAccessGroupId)
            .build();
    }

}
