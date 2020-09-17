package uk.gov.hmcts.reform.roleassignment.oidc;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class OIdcAdminConfiguration {

    private String userId;
    private String password;
    private String scope;

    @Autowired
    public OIdcAdminConfiguration(
        @Value("${idam.client.admin.userId:}") String userId,
        @Value("${idam.client.admin.scope:}") String scope
    ) {
        this.userId = userId;
        this.password = System.getenv("ROLE_ASSIGNMENT_IDAM_ADMIN_PASSWORD");
        this.scope = scope;
    }
}
