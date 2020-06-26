package uk.gov.hmcts.reform.roleassignment.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.fail;

class ValidationUtilTest {

    UUID uuid = UUID.fromString("21334a2b-79ce-44eb-9168-2d49a744be9c");

    @Test
    void shouldValidate() {
        try {
            ValidationUtil.validateCaseId("1212121212121213");
        } catch (Exception e) {
            fail("failed");
        }
    }

    @Test
    void shouldThrow() {
        Assertions.assertThrows(BadRequestException.class, () -> {
            ValidationUtil.validateCaseId("2323232323232");
        });
    }

    @Test
    void validateTextField() {
        try {
            ValidationUtil.validateTextField("CREATE");
        } catch (Exception e) {
            fail("failed");
        }
    }

    @Test
    void throw_validateTextField() {
        Assertions.assertThrows(BadRequestException.class, () -> {
            ValidationUtil.validateTextField("1234");
        });
    }


    @Test
    void validateNumberTextField() {
        try {
            ValidationUtil.validateNumberTextField("request1");
        } catch (Exception e) {
            fail("failed");
        }
    }

    @Test
    void throw_validateNumberTextField() {
        Assertions.assertThrows(BadRequestException.class, () -> {
            ValidationUtil.validateNumberTextField("requ-est1");
        });
    }

    @Test
    void shouldValidateHyphenTextField() {
        try {
            ValidationUtil.validateTextHyphenField("north-west");
        } catch (Exception e) {
            fail("failed");
        }
    }

    @Test
    void should_ValidateHyphenTextField() {
        Assertions.assertThrows(BadRequestException.class, () -> {
            ValidationUtil.validateTextHyphenField("north-west1");
        });
    }

    @Test
    void validateRoleRequest() throws IOException {
        try {
            ValidationUtil.validateRoleRequest(TestDataBuilder.buildAssignmentRequest().getRequest());
        } catch (Exception e) {
            fail("failed");
        }
    }

    @Test
    void validateRequestedRoles() throws IOException, ParseException {
        try {
            ValidationUtil.validateRequestedRoles(TestDataBuilder.buildAssignmentRequest().getRequestedRoles());
        } catch (Exception e) {
            fail("failed");
        }
    }

    @Test
    void shouldThrowInvalidException_isValidSecurityClassification() {
        Assertions.assertThrows(BadRequestException.class, () -> {
            ValidationUtil.isValidSecurityClassification("   PROTECTED");
        });
    }

    @Test
    void shouldThrowInvalidRequestException_ValidateLists() {
        Assertions.assertThrows(BadRequestException.class, () -> {
            ValidationUtil.validateLists(new ArrayList());
        });
    }
}
