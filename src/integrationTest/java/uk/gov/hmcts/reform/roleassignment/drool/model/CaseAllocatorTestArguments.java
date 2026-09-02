package uk.gov.hmcts.reform.roleassignment.drool.model;

import lombok.Builder;
import lombok.Getter;
import org.apache.logging.log4j.util.Strings;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleCategory;

@Builder
@Getter
public class CaseAllocatorTestArguments implements TestArguments {

    private String serviceName;
    private String jurisdiction;

    private RoleCategory roleCategory;
    private String caseType;

    private String caseRoleName;

    // existing role properties
    private String existingRoleName;
    private String existingRoleCaseType;

    // case-allocator role properties
    @Builder.Default
    private boolean caAlwaysUseCaseType = false;
    private RoleCategory caRoleCategory;

    private String expectingSubstantive;

    public String getService() {
        return Strings.isNotBlank(serviceName) ? serviceName : jurisdiction;
    }

    public String getDescription() {
        return String.format(
            "Service: %s, RoleCategory: %s, CaseType: %s, CaseRole: %s, "
                + "ExistingRoleName: %s, ExistingRoleCaseType: %s, "
                + "CaUseCaseType: %b, CaRoleCategory: %s, Substantive: %s",
            this.getService(),
            roleCategory.name(),
            caseType,
            caseRoleName,
            existingRoleName,
            Strings.isNotBlank(existingRoleCaseType) ? existingRoleCaseType : "NULL",
            caAlwaysUseCaseType,
            caRoleCategory.name(),
            expectingSubstantive
        );
    }

    public String getGroup() {
        return String.format(
            "Service: %s, RoleCategory: %s, CaseType: %s, CaseRole: %s, ExistingRoleName: %s",
            this.getService(),
            roleCategory.name(),
            caseType,
            caseRoleName,
            existingRoleName
        );
    }

    public String getOutputSubFolder() {
        return String.format(
            "%s/Category_%s__CaseType_%s__CaseRole_%s__"
                + "ExistRole_%s__ExistCaseType_%s__"
                + "CaUseCaseType_%b__CaCategory_%s",
            this.getService(),
            roleCategory.name(),
            caseType,
            caseRoleName,
            existingRoleName,
            Strings.isNotBlank(existingRoleCaseType) ? existingRoleCaseType : "NULL",
            caAlwaysUseCaseType,
            caRoleCategory.name()
        );
    }

}
