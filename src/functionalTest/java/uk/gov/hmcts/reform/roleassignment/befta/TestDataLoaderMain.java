package uk.gov.hmcts.reform.roleassignment.befta;

public class TestDataLoaderMain {

    private TestDataLoaderMain() {
    }

    public static void main(String[] args) {
        new RoleAssignmentTestAutomationAdapter().doLoadTestData();
    }

}
