package uk.gov.hmcts.reform.roleassignment.drool.model;

import lombok.Builder;
import lombok.Getter;
import org.apache.logging.log4j.util.Strings;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleCategory;

@Builder
@Getter
public class ChallengedAccessTestArguments implements TestArguments {

    private String serviceName;
    private String jurisdiction;

    private RoleCategory roleCategory;
    private String caseType;

    // existing role properties
    private String existingRoleName;
    private String existingRoleCaseType;

    @Builder.Default
    private boolean expectSuccess = true;

    public String getService() {
        return Strings.isNotBlank(serviceName) ? serviceName : jurisdiction;
    }

    public String getDescription() {
        return String.format(
            "Service: %s, RoleCategory: %s, ExistingRoleCaseType: %s, ExistingRoleName: %s, "
                + "expectSuccess: %b",
            this.getService(),
            roleCategory.name(),
            existingRoleCaseType,
            existingRoleName,
            expectSuccess
        );
    }

    public String getGroup() {
        return String.format(
            "Service: %s, RoleCategory: %s, ExistingRoleCaseType: %s, expectSuccess: %b",
            this.getService(),
            roleCategory.name(),
            existingRoleCaseType,
            expectSuccess
        );
    }

    public String getOutputSubFolder() {
        return String.format(
            "%s/Category_%s__CaseType_%s__ExistRole_%s",
            this.getService(),
            roleCategory.name(),
            existingRoleCaseType,
            existingRoleName
        );
    }

}
