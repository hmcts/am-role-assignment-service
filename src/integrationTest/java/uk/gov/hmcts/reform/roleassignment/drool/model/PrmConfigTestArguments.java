package uk.gov.hmcts.reform.roleassignment.drool.model;

import lombok.Builder;
import lombok.Getter;
import org.apache.logging.log4j.util.Strings;

@Builder
@Getter
public class PrmConfigTestArguments implements TestArguments {

    private String serviceName;
    private String jurisdiction;

    private String caseType;
    private String roleName;
    private String caseAccessGroupId;

    public boolean isGaGroupRole() {
        return Strings.isNotBlank(caseAccessGroupId);
    }

    public boolean isGaOrgRole() {
        return !isGaGroupRole();
    }

    public String getService() {
        return Strings.isNotBlank(serviceName) ? serviceName : jurisdiction;
    }

    public String getDescription() {
        return String.format(
            "Service: %s, CaseType: %s, Role: %s, GA Group Role: %s",
            this.getService(),
            caseType,
            roleName,
            isGaGroupRole()
        );
    }

    public String getGroup() {
        return String.format(
            "Service: %s, CaseType: %s",
            this.getService(),
            caseType
        );
    }

    public String getOutputSubFolder() {
        return String.format(
            "%s/CaseType_%s__Role_%s",
            this.getService(),
            caseType,
            roleName
        );
    }

}
