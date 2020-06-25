package uk.gov.hmcts.reform.roleassignment.v1;

public final class V1 {

    private V1() {
    }

    public final class MediaType {
        private MediaType() {
        }

        // External API
        public static final String CREATE_ASSIGNMENT = "application/vnd.uk.gov.hmcts.role-assignment-service"
                                                       + ".create-role+json;charset=UTF-8;version=1.0";
    }

    public static final class Error {
        private Error() {
        }

        public static final String INVALID_REQUEST = "Request is not valid as per validation rule";
        public static final String INVALID_ROLE_NAME = "Invalid role name in the request";


    }

}
