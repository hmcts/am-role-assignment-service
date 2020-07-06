package uk.gov.hmcts.reform.roleassignment.v1;

public final class V1 {

    private V1() {
    }

    public final class MediaType {
        private MediaType() {
        }

        // External API
        public static final String CREATE_ASSIGNMENTS = "application/vnd.uk.gov.hmcts.role-assignment-service"
                                                       + ".create-assignments+json;charset=UTF-8;version=1.0";
        public static final String DELETE_ASSIGNMENTS = "application/vnd.uk.gov.hmcts.role-assignment-service"
                                                       + ".delete-assignments+json;charset=UTF-8;version=1.0";
        public static final String GET_ASSIGNMENTS = "application/vnd.uk.gov.hmcts.role-assignment-service"
            + ".get-assignments+json;charset=UTF-8;version=1.0";
        public static final String GET_ROLES = "application/vnd.uk.gov.hmcts.role-assignment-service"
            + ".get-roles+json;charset=UTF-8;version=1.0";
    }

    public static final class Error {
        private Error() {
        }

        public static final String INVALID_REQUEST = "Request is not valid as per validation rule";
        public static final String INVALID_ROLE_NAME = "Invalid role name in the request";
        public static final String BAD_REQUEST_INVALID_PARAMETER = "Invalid Parameter";
        public static final String NO_RECORDS_FOUND_BY_ACTOR = "Role Assignment not found for Actor ";
        public static final String NO_RECORDS_FOUND_BY_PROCESS = "Role Assignment not found for process "
            + ": %s & reference : %s";
        public static final String BAD_REQUEST_MISSING_PARAMETERS = "Mandatory Parameters are missing";

    }

}
