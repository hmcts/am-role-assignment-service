package uk.gov.hmcts.reform.roleassignment.util;

import org.apache.commons.lang3.Validate;

public class EnvironmentVariableUtils {
    private EnvironmentVariableUtils(){

    }

    public static String getRequiredVariable(String name) {
        return Validate.notNull(System.getenv(name), "Environment variable `%s` is required", name);
    }
}
