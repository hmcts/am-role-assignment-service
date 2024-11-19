package uk.gov.hmcts.reform.roleassignment.befta;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.befta.DefaultTestAutomationAdapter;
import uk.gov.hmcts.befta.player.BackEndFunctionalTestScenarioContext;

import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

@Slf4j
public class RoleAssignmentTestAutomationAdapter extends DefaultTestAutomationAdapter {
    public static RoleAssignmentTestAutomationAdapter INSTANCE = new RoleAssignmentTestAutomationAdapter();

    @Override
    public Object calculateCustomValue(BackEndFunctionalTestScenarioContext scenarioContext, Object key) {
        return switch (key.toString()) {
            case ("generateUUID") -> UUID.randomUUID();
            case ("generateCaseId") -> generateCaseId();
            case ("generateS2STokenForCcd") -> super.getNewS2SToken("ccd_data");
            case ("generateS2STokenForOrm") -> super.getNewS2SToken("am_org_role_mapping_service");
            case ("generateS2STokenForXui") -> super.getNewS2SToken("xui_webapp");
            case ("tomorrow") -> LocalDate.now().plusDays(1);
            case ("today") -> LocalDate.now();
            default -> super.calculateCustomValue(scenarioContext, key);
        };
    }

    private Object generateCaseId() {
        var currentTime = new Date().getTime();
        var time = Long.toString(currentTime);
        return time + ("0000000000000000".substring(time.length()));
    }

}
