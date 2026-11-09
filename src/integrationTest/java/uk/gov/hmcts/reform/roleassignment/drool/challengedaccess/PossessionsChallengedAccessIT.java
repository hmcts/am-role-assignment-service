package uk.gov.hmcts.reform.roleassignment.drool.challengedaccess;

import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.roleassignment.drool.model.ChallengedAccessTestArguments;

import java.util.ArrayList;
import java.util.List;


public class PossessionsChallengedAccessIT {

    public static List<ChallengedAccessTestArguments> getAllTestArguments() {
        List<ChallengedAccessTestArguments> arguments = new ArrayList<>();


        // NB: PCS: ChallengedAccess is configured to work on any of their case-types
        List<String> caseTypes = List.of("any");

        // generate tests for all Possession org roles

        arguments.addAll(getTestArguments(
            RoleCategory.JUDICIAL,
            caseTypes,
            List.of(
                "judge",
                "circuit-judge",
                "leadership-judge"
            ),
            List.of(
                "fee-paid-judge",
                "case-allocator",
                "task-supervisor",
                "specific-access-approver-judiciary",
                "specific-access-approver-legal-ops"
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
                "wlu-admin",
                "wlu-team-leader",
                "bailiff-admin"
            ),
            List.of(
                "case-allocator",
                "task-supervisor",
                "specific-access-approver-admin"
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
            .serviceName("Possessions")
            .jurisdiction("PCS")
            // test specific properties
            .roleCategory(roleCategory)
            .caseType(caseType)
            .existingRoleName(existingRoleName)
            .existingRoleCaseType(null) // NB: PCS do not use caseType filtering on their Org roles
            .expectSuccess(expectSuccess)
            .build();
    }

}
