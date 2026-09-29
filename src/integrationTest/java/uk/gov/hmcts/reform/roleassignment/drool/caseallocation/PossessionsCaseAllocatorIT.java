package uk.gov.hmcts.reform.roleassignment.drool.caseallocation;

import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.roleassignment.drool.model.CaseAllocatorTestArguments;

import java.util.ArrayList;
import java.util.List;


@SuppressWarnings({"java:S125", "java:S1135"}) // TODO: ENFORCEMENT config coming in PCS WA 1.1
public class PossessionsCaseAllocatorIT {

    public static List<CaseAllocatorTestArguments> getAllTestArguments() {
        List<CaseAllocatorTestArguments> arguments = new ArrayList<>();

        // generate tests for each CA Role Category used in PCS
        // NB: There is no case-allocator role for LEGAL_OPERATIONS in PCS, so no tests for that category

        arguments.addAll(getTestArguments(RoleCategory.JUDICIAL));
        arguments.addAll(getTestArguments(RoleCategory.CTSC));
        arguments.addAll(getTestArguments(RoleCategory.ADMIN));
        /* TODO: ENFORCEMENT config coming in PCS WA 1.1
        arguments.addAll(getTestArguments(RoleCategory.ENFORCEMENT));
         */

        return arguments;
    }

    private static List<CaseAllocatorTestArguments> getTestArguments(RoleCategory caRoleCategory) {
        List<CaseAllocatorTestArguments> arguments = new ArrayList<>();

        // JUDICIAL:allocated-judge
        arguments.addAll(
            getTestArgumentsForCaseRole(
                "allocated-judge",
                List.of("judge", "fee-paid-judge"),
                RoleCategory.JUDICIAL,
                caRoleCategory,
                "Y"
            )
        );

        // JUDICIAL:hearing-judge
        arguments.addAll(
            getTestArgumentsForCaseRole(
                "hearing-judge",
                List.of("judge", "fee-paid-judge"),
                RoleCategory.JUDICIAL,
                caRoleCategory,
                "Y"
            )
        );

        // JUDICIAL:case-allocator
        arguments.addAll(
            getTestArgumentsForCaseRole(
                "case-allocator",
                List.of("case-allocator"),
                RoleCategory.JUDICIAL,
                caRoleCategory,
                "N"
            )
        );

        // CTSC:allocated-ctsc-caseworker
        arguments.addAll(
            getTestArgumentsForCaseRole(
                "allocated-ctsc-caseworker",
                List.of("ctsc-team-leader", "ctsc"),
                RoleCategory.CTSC,
                caRoleCategory,
                "Y"
            )
        );

        // CTSC:case-allocator
        arguments.addAll(
            getTestArgumentsForCaseRole(
                "case-allocator",
                List.of("case-allocator"),
                RoleCategory.CTSC,
                caRoleCategory,
                "N"
            )
        );

        // ADMIN:allocated-admin-caseworker
        arguments.addAll(
            getTestArgumentsForCaseRole(
                "allocated-admin-caseworker",
                List.of(
                    "hearing-centre-team-leader",
                    "hearing-centre-admin",
                    "bailiff-admin"
                ),
                RoleCategory.ADMIN,
                caRoleCategory,
                "Y"
            )
        );

        // ADMIN:allocated-wlu-caseworker
        arguments.addAll(
            getTestArgumentsForCaseRole(
                "allocated-wlu-caseworker",
                List.of(
                    "wlu-team-leader",
                    "wlu-admin"
                ),
                RoleCategory.ADMIN,
                caRoleCategory,
                "Y"
            )
        );

        // ADMIN:case-allocator
        arguments.addAll(
            getTestArgumentsForCaseRole(
                "case-allocator",
                List.of("case-allocator"),
                RoleCategory.ADMIN,
                caRoleCategory,
                "N"
            )
        );


        /* TODO: ENFORCEMENT config coming in PCS WA 1.1

        // ENFORCEMENT:allocated-wlu-caseworker
        arguments.addAll(
            getTestArgumentsForCaseRole(
                "allocated-bailiff",
                List.of(
                    "bailiff-manager",
                    "bailiff"
                ),
                RoleCategory.ENFORCEMENT,
                caRoleCategory,
                "Y"
            )
        );

        // ENFORCEMENT:case-allocator
        arguments.addAll(
            getTestArgumentsForCaseRole(
                "case-allocator",
                List.of("case-allocator"),
                RoleCategory.ENFORCEMENT,
                caRoleCategory,
                "N"
            )
        );

         */

        return arguments;
    }

    private static List<CaseAllocatorTestArguments> getTestArgumentsForCaseRole(String caseRoleName,
                                                                                List<String> existingRoleNames,
                                                                                RoleCategory roleCategory,
                                                                                RoleCategory caRoleCategory,
                                                                                String expectingSubstantive) {

        return existingRoleNames.stream()
            .map(existingRoleName -> CaseAllocatorTestArguments.builder()
                // default test properties
                .serviceName("Possessions")
                .jurisdiction("PCS")
                .caAlwaysUseCaseType(false) // NB: PCS do not use caseType filtering on their Org roles
                // test specific properties
                .roleCategory(roleCategory)
                .caseType("any") // NB: PCS: Case-Roles are configured to work on any of their case-types
                .caseRoleName(caseRoleName)
                .existingRoleName(existingRoleName)
                .existingRoleCaseType(null) // NB: PCS do not use caseType filtering on their Org roles
                .caRoleCategory(caRoleCategory)
                .expectingSubstantive(expectingSubstantive)
                .build())
            .toList();
    }

}
