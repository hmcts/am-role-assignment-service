package uk.gov.hmcts.reform.roleassignment.drool.challengedaccess;

import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.roleassignment.drool.model.ChallengedAccessTestArguments;

import java.util.ArrayList;
import java.util.List;

public class FrChallengedAccessIT {

    public static List<ChallengedAccessTestArguments> getAllTestArguments() {
        List<ChallengedAccessTestArguments> arguments = new ArrayList<>();

        List<String> caseTypes = List.of("FinancialRemedyMVP2");

        // generate tests for all FR org roles

        arguments.addAll(getTestArguments(
            RoleCategory.JUDICIAL,
            caseTypes,
            List.of(
                "judge",
                "leadership-judge"
            ),
            List.of(
                "fee-paid-judge",
                "case-allocator",
                "task-supervisor",
                "specific-access-approver-judiciary"
            )
        ));
        arguments.addAll(getTestArguments(
            RoleCategory.CTSC,
            caseTypes,
            List.of(
                "ctsc",
                "ctsc-team-leader"
            ),
            List.of(
                "case-allocator",
                "task-supervisor",
                "specific-access-approver-ctsc"
            )
        ));
        arguments.addAll(getTestArguments(
            RoleCategory.ADMIN,
            caseTypes,
            List.of(
                "hearing-centre-admin",
                "hearing-centre-team-leader",
                "national-business-centre",
                "nbc-team-leader"
            ),
            List.of(
                "case-allocator",
                "task-supervisor",
                "specific-access-approver-admin",
                "specific-access-approver-legal-ops"
            )
        ));

        return arguments;
    }

    private static List<ChallengedAccessTestArguments> getTestArguments(RoleCategory roleCategory,
                                                                        List<String> caseTypes,
                                                                        List<String> successRoles,
                                                                        List<String> failureRoles) {
        List<ChallengedAccessTestArguments> arguments = new ArrayList<>();

        // success scenarios
        caseTypes.forEach(caseType -> successRoles.forEach(
            roleName -> arguments.add(
                createChallengedAccessTestArguments(
                    roleCategory,
                    caseType,
                    roleName,
                    true
                )
            )
        ));

        // failure scenarios
        caseTypes.forEach(caseType -> failureRoles.forEach(
            roleName -> arguments.add(
                createChallengedAccessTestArguments(
                    roleCategory,
                    caseType,
                    roleName,
                    false
                )
            )
        ));

        return arguments;
    }

    private static ChallengedAccessTestArguments createChallengedAccessTestArguments(RoleCategory roleCategory,
                                                                                     String caseType,
                                                                                     String existingRoleName,
                                                                                     boolean expectSuccess) {
        return ChallengedAccessTestArguments.builder()
            // default test properties
            .serviceName("FinancialRemedy")
            .jurisdiction("DIVORCE")
            // test specific properties
            .roleCategory(roleCategory)
            .caseType(caseType)
            .existingRoleName(existingRoleName)
            .existingRoleCaseType(caseType) // NB: Financial Remedy Org Roles must always use the CaseType
            .expectSuccess(expectSuccess)
            .build();
    }

}
