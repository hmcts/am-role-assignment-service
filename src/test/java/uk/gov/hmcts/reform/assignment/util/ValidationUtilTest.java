package uk.gov.hmcts.reform.assignment.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.assignment.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.assignment.domain.model.Role;
import uk.gov.hmcts.reform.assignment.helper.TestDataBuilder;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static uk.gov.hmcts.reform.assignment.apihelper.Constants.NUMBER_PATTERN;
import static uk.gov.hmcts.reform.assignment.apihelper.Constants.NUMBER_TEXT_PATTERN;
import static uk.gov.hmcts.reform.assignment.apihelper.Constants.TEXT_HYPHEN_PATTERN;
import static uk.gov.hmcts.reform.assignment.apihelper.Constants.TEXT_PATTERN;

class ValidationUtilTest {

    UUID uuid = UUID.fromString("21334a2b-79ce-44eb-9168-2d49a744be9c");

    @Test
    void shouldValidate() {
        try {
            ValidationUtil.validateInputParams(NUMBER_PATTERN, "1212121212121213");
        } catch (Exception e) {
            fail("failed");
        }
    }

    @Test
    void shouldThrow() {
        Assertions.assertThrows(BadRequestException.class, () -> {
            ValidationUtil.validateInputParams(NUMBER_PATTERN, "2323232323232");
        });
    }

    @Test
    void validateTextField() {
        try {
            ValidationUtil.validateInputParams(TEXT_PATTERN, "CREATE");
        } catch (Exception e) {
            fail("failed");
        }
    }

    @Test
    void throw_validateTextField() {
        Assertions.assertThrows(BadRequestException.class, () -> {
            ValidationUtil.validateInputParams(NUMBER_PATTERN,"1234");
        });
    }


    @Test
    void validateNumberTextField() {
        try {
            ValidationUtil.validateInputParams(NUMBER_TEXT_PATTERN,"request1");
        } catch (Exception e) {
            fail("failed");
        }
    }

    @Test
    void throw_validateNumberTextField() {
        Assertions.assertThrows(BadRequestException.class, () -> {
            ValidationUtil.validateInputParams(NUMBER_TEXT_PATTERN,"requ-est1");
        });
    }

    @Test
    void shouldValidateHyphenTextField() {
        try {
            ValidationUtil.validateInputParams(TEXT_HYPHEN_PATTERN,"north-west");
        } catch (Exception e) {
            fail("failed");
        }
    }

    @Test
    void should_ValidateHyphenTextField() {
        Assertions.assertThrows(BadRequestException.class, () -> {
            ValidationUtil.validateInputParams(TEXT_HYPHEN_PATTERN,"north-west1");
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

    //@Test
    void validateRequestedRoles() throws IOException, ParseException {
        try {
            ValidationUtil.validateRequestedRoles(TestDataBuilder.buildAssignmentRequest().getRequestedRoles());
        } catch (Exception e) {
            fail("failed");
        }
    }

    @Test
    void shouldThrowInvalidRequestException_ValidateLists() {
        Assertions.assertThrows(BadRequestException.class, () -> {
            ValidationUtil.validateLists(new ArrayList());
        });
    }

    @Test
    void testBuildRole() throws IOException {
        List<Role> roles = TestDataBuilder.buildRolesFromFile();
        assertNotNull(roles);
        assertTrue(roles.size() > 1);
    }
}
