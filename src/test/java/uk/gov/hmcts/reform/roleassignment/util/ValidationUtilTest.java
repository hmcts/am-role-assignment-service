package uk.gov.hmcts.reform.roleassignment.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.BadRequestException;

import static uk.gov.hmcts.reform.roleassignment.apihelper.Constants.*;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ValidationUtilTest {

    @Test
    void shouldValidate() {
        assertEquals(true, ValidationUtil.validate("1212121212121212", NUMBER_PATTERN));
        assertEquals(false, ValidationUtil.validate("2323232323232", NUMBER_PATTERN));
    }

    @Test
    void shouldThrowBadRequestException_isValidSecurityClassification() {
        Assertions.assertThrows(BadRequestException.class, () -> {
            ValidationUtil.isValidSecurityClassification("   PROTECTED");
        });
    }

    @Test
    void shouldThrowBadRequestException_ValidateLists() {
        Assertions.assertThrows(BadRequestException.class, () -> {
            ValidationUtil.validateLists(new ArrayList());
        });
    }

    @Test
    void shouldValidateTTL() {
        assertEquals(false, ValidationUtil.validateTTL("2021-12-31T10:10:10+"));
        assertEquals(false, ValidationUtil.validateTTL("2021-12-31T10:10:10+9999"));
        assertEquals(false, ValidationUtil.validateTTL("2021-12-31T10:10:10+999Z"));
    }
}
