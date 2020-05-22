package uk.gov.hmcts.reform.roleassignment.befta;

import uk.gov.hmcts.befta.BeftaMain;

public class RoleAssignmentAmBeftaMain {

    private RoleAssignmentAmBeftaMain() {
    }

    public static void main(String[] args) {

        BeftaMain.main(args, new RoleAssignmentAmTestAutomationAdapter());
    }
}
