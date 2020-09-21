package uk.gov.hmcts.reform.roleassignment.oidc;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
@Slf4j
public class OIdcAdminConfiguration {

    public static final String ROLE_ASSIGNMENT_IDAM_ADMIN_PASSWORD = "ROLE_ASSIGNMENT_IDAM_ADMIN_PASSWORD";
    private final String userId;
    private final String password = System.getenv(ROLE_ASSIGNMENT_IDAM_ADMIN_PASSWORD);
    private final String scope;

    @Autowired
    public OIdcAdminConfiguration(
        @Value("${idam.client.admin.userId:}") String userId,
        @Value("${idam.client.admin.scope:}") String scope
    ) {
        this.userId = userId;
        System.out.println("should_receive_response_for_add_role_assignment ROLE_ASSIGNMENT_IDAM_ADMIN_PASSWORD is"
                               + System.getenv(ROLE_ASSIGNMENT_IDAM_ADMIN_PASSWORD));
        log.error("should_receive_response_for_add_role_assignment ROLE_ASSIGNMENT_IDAM_ADMIN_PASSWORD is"
                      + System.getenv(ROLE_ASSIGNMENT_IDAM_ADMIN_PASSWORD));
        log.error("password is :" + password);
        this.scope = scope;
    }
}
