package uk.gov.hmcts.reform.roleassignment.drool.caseallocation;

import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.roleassignment.drool.model.CaseAllocatorTestArguments;

import java.util.ArrayList;
import java.util.List;


@SuppressWarnings("SameParameterValue")
public class ProbateCaseAllocatorIT {

    public static List<CaseAllocatorTestArguments> getAllTestArguments() {
        List<CaseAllocatorTestArguments> arguments = new ArrayList<>();

        // generate tests for all Probate case types and each CA Role Category they use
        // NB: There is no case-allocator role for ADMIN or JUDICIAL in Probate, so no tests for those categories

        arguments.addAll(getTestArguments("GrantOfRepresentation", RoleCategory.CTSC));
        arguments.addAll(getTestArguments("GrantOfRepresentation", RoleCategory.LEGAL_OPERATIONS));

        return arguments;
    }

    private static List<CaseAllocatorTestArguments> getTestArguments(String caseType, RoleCategory caRoleCategory) {
        List<CaseAllocatorTestArguments> arguments = new ArrayList<>();

        // CTSC:allocated-ctsc-caseworker
        arguments.addAll(
            getTestArgumentsForCaseRole(
                "allocated-ctsc-caseworker",
                List.of("ctsc-team-leader", "ctsc"),
                RoleCategory.CTSC,
                caseType,
                caRoleCategory,
                "Y"
            )
        );

        // ADMIN:allocated-tribunal-caseworker
        arguments.addAll(
            getTestArgumentsForCaseRole(
                "allocated-tribunal-caseworker",
                List.of("senior-tribunal-caseworker"),
                RoleCategory.LEGAL_OPERATIONS,
                caseType,
                caRoleCategory,
                "Y"
            )
        );

        return arguments;
    }

    private static List<CaseAllocatorTestArguments> getTestArgumentsForCaseRole(String caseRoleName,
                                                                                List<String> existingRoleNames,
                                                                                RoleCategory roleCategory,
                                                                                String caseType,
                                                                                RoleCategory caRoleCategory,
                                                                                String expectingSubstantive) {

        return existingRoleNames.stream()
            .map(existingRoleName -> CaseAllocatorTestArguments.builder()
                // default test properties
                .serviceName("Probate")
                .jurisdiction("PROBATE")
                .caAlwaysUseCaseType(false) // NB: Probate do not use caseType filtering on their Org roles
                // test specific properties
                .roleCategory(roleCategory)
                .caseType(caseType)
                .caseRoleName(caseRoleName)
                .existingRoleName(existingRoleName)
                .existingRoleCaseType(null) // NB: Probate do not use caseType filtering on their Org roles
                .caRoleCategory(caRoleCategory)
                .expectingSubstantive(expectingSubstantive)
                .build())
            .toList();
    }

}
