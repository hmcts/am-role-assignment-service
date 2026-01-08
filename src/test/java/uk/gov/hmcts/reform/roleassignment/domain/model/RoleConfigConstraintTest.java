package uk.gov.hmcts.reform.roleassignment.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class RoleConfigConstraintTest {

    private RoleConfigConstraint<String> constraint;

    @Test
    void shouldMatchWhenMandatory() {
        constraint = new RoleConfigConstraint<>(true, Collections.singleton("ADMIN"));
        boolean result = constraint.matches("ADMIN");
        assertTrue(result);
        result = constraint.matches("SUPER");
        assertFalse(result);
    }


    @Test
    void shouldMatchWhenNotMandatory() {
        constraint = new RoleConfigConstraint<>(false, Collections.singleton("ADMIN"));
        boolean result = constraint.matches("ADMIN");
        assertTrue(result);
        result = constraint.matches(null);
        assertTrue(result);
        assertNotNull(constraint.getValues());
    }
}
