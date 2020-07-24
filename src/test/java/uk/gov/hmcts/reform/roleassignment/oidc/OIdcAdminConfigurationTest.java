package uk.gov.hmcts.reform.roleassignment.oidc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OIdcAdminConfigurationTest {

    OIdcAdminConfiguration sut = new OIdcAdminConfiguration("userId", "pass", "scope");

    @Test
    void getUserId() {
        assertNotNull(sut.getUserId());
    }

    @Test
    void getPassword() {
        assertNotNull(sut.getPassword());
    }

    @Test
    void getScope() {
        assertNotNull(sut.getScope());
    }
}
