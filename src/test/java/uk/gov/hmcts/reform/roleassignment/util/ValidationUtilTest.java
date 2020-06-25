package uk.gov.hmcts.reform.roleassignment.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.InvalidRequest;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.UUID;

public class ValidationUtilTest {

    UUID uuid = UUID.fromString("21334a2b-79ce-44eb-9168-2d49a744be9c");

    @Test
    void shouldValidate() {
        assertEquals(true, ValidationUtil.validateCaseNumber("1212121212121212"));
        assertEquals(false, ValidationUtil.validateCaseNumber("2323232323232"));
    }

    @Test
    void validateTextField() {
        assertTrue(ValidationUtil.validateTextField("CREATE"));
    }

    @Test
    void throw_validateTextField() {
        Assertions.assertThrows(InvalidRequest.class, () -> {
            ValidationUtil.validateTextField("1234");
        });
    }

    @Test
    void validateUuidField() {
        assertTrue(ValidationUtil.validateUuidField(uuid));
    }

    @Test
    void validateNumberTextField() {
        assertTrue(ValidationUtil.validateNumberTextField("request1"));
    }

    @Test
    void throw_validateNumberTextField() {
        Assertions.assertThrows(InvalidRequest.class, () -> {
            ValidationUtil.validateNumberTextField("requ-est1");
        });
    }

    @Test
    void shouldValidateHyphenTextField() {
        assertTrue(ValidationUtil.validateTextHyphenField("north-west"));
    }

    @Test
    void should_ValidateHyphenTextField() {
        Assertions.assertThrows(InvalidRequest.class, () -> {
            ValidationUtil.validateTextHyphenField("north-west1");
        });
    }

    @Test
    void validateRoleRequest() throws IOException {
        assertTrue(ValidationUtil.validateRoleRequest(TestDataBuilder.buildAssignmentRequest().getRequest()));
    }

    @Test
    void validateRequestedRoles() throws IOException, ParseException {
        assertTrue(ValidationUtil.validateRequestedRoles(TestDataBuilder.buildAssignmentRequest().getRequestedRoles()));
    }

    @Test
    void shouldThrowInvalidException_isValidSecurityClassification() {
        Assertions.assertThrows(InvalidRequest.class, () -> {
            ValidationUtil.isValidSecurityClassification("   PROTECTED");
        });
    }

    @Test
    void shouldThrowInvalidRequestException_ValidateLists() {
        Assertions.assertThrows(InvalidRequest.class, () -> {
            ValidationUtil.validateLists(new ArrayList());
        });
    }

    @Test
    void shouldValidateTTL() {
        //assertTrue(ValidationUtil.validateTTL("2013-09-29T18:46:19Z"));
        assertFalse(ValidationUtil.validateTTL("2021-12-31T10:10:10+"));
        assertEquals(false, ValidationUtil.validateTTL("2021-12-31T10:10:10+9999"));
        assertEquals(false, ValidationUtil.validateTTL("2021-12-31T10:10:10+999Z"));
    }

    @Test
    void validateDateTime() {

    }

    @Test
    void validateDateOrder_Exception() throws ParseException {
        Assertions.assertThrows(BadRequestException.class, () -> {
            ValidationUtil.validateDateOrder("2023-01-01T00:00", "2022-01-01T00:00", "2020-01-01T00:00");
        });
    }
}
