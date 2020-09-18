package uk.gov.hmcts.reform.roleassignment.oidc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class OIdcAdminConfigurationTest {

    OIdcAdminConfiguration sut = new OIdcAdminConfiguration("userId",  "scope");

    @Test
    void getUserId() {
        assertNotNull(sut.getUserId());
    }

    @Test
    void getScope() {
        assertNotNull(sut.getScope());
    }
}
