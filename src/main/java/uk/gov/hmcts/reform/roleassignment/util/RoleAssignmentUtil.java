package uk.gov.hmcts.reform.roleassignment.util;

import javax.inject.Named;
import javax.inject.Singleton;

@Named
@Singleton
public class RoleAssignmentUtil {

    private RoleAssignmentUtil() {

    }

    public static boolean isEmpty(String input) {
        return (input == null || input.isEmpty());
    }

    public static boolean notEmpty(String input) {
        return (input != null || !input.isEmpty());
    }


}
