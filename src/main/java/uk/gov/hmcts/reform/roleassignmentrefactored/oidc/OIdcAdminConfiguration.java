package uk.gov.hmcts.reform.roleassignmentrefactored.oidc;

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
        @Value("${idam.admin.userId:}") String userId,
        @Value("${idam.admin.password:}") String password,
        @Value("${idam.admin.scope:}") String scope
    ) {
        this.userId = userId;
        this.password = password;
        this.scope = scope;
    }

}
