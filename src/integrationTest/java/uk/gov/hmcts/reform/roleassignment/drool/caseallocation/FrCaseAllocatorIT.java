package uk.gov.hmcts.reform.roleassignment.drool.caseallocation;

import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.roleassignment.drool.model.CaseAllocatorTestArguments;

import java.util.ArrayList;
import java.util.List;

public class FrCaseAllocatorIT {

    public static List<CaseAllocatorTestArguments> getAllTestArguments() {
        List<CaseAllocatorTestArguments> arguments = new ArrayList<>();

        // generate tests for all FR case types and each CA Role Category they use
        // NB: There is no case-allocator role for LEGAL_OPERATIONS in FR, so no tests for that category

        arguments.addAll(getTestArguments("FinancialRemedyMVP2", RoleCategory.JUDICIAL));
        arguments.addAll(getTestArguments("FinancialRemedyMVP2", RoleCategory.CTSC));
        arguments.addAll(getTestArguments("FinancialRemedyMVP2", RoleCategory.ADMIN));

        return arguments;
    }

    private static List<CaseAllocatorTestArguments> getTestArguments(String caseType, RoleCategory caRoleCategory) {
        List<CaseAllocatorTestArguments> arguments = new ArrayList<>();

        // JUDICIAL:allocated-judge
        arguments.addAll(
            getTestArgumentsForCaseRole(
                "allocated-judge",
                List.of("judge", "fee-paid-judge"),
                RoleCategory.JUDICIAL,
                caseType,
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
                caseType,
                caRoleCategory,
                "Y"
            )
        );

        // JUDICIAL:lead-judge
        arguments.addAll(
            getTestArgumentsForCaseRole(
                "lead-judge",
                List.of("judge"), // NB: only available for judge
                RoleCategory.JUDICIAL,
                caseType,
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
                caseType,
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
                caseType,
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
                caseType,
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
                    "nbc-team-leader",
                    "national-business-centre"
                ),
                RoleCategory.ADMIN,
                caseType,
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
                caseType,
                caRoleCategory,
                "N"
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
                .serviceName("FinancialRemedy")
                .jurisdiction("DIVORCE")
                .caAlwaysUseCaseType(true) // NB: Financial Remedy Org Roles must always use the CaseType
                // test specific properties
                .roleCategory(roleCategory)
                .caseType(caseType)
                .caseRoleName(caseRoleName)
                .existingRoleName(existingRoleName)
                .existingRoleCaseType(caseType) // NB: Financial Remedy Org Roles must always use the CaseType
                .caRoleCategory(caRoleCategory)
                .expectingSubstantive(expectingSubstantive)
                .build())
            .toList();
    }

}
