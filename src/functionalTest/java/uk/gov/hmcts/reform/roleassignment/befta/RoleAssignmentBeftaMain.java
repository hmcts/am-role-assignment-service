package uk.gov.hmcts.reform.roleassignment.befta;

import uk.gov.hmcts.befta.BeftaMain;

public class RoleAssignmentBeftaMain {

    private RoleAssignmentBeftaMain() {
    }

    public static void main(String[] args) {

        BeftaMain.main(args, new RoleAssignmentTestAutomationAdapter());
    }
}
