package uk.gov.hmcts.reform.roleassignment.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.roleassignment.domain.model.Role;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static uk.gov.hmcts.reform.roleassignment.util.Constants.NUMBER_PATTERN;
import static uk.gov.hmcts.reform.roleassignment.util.Constants.NUMBER_TEXT_PATTERN;
import static uk.gov.hmcts.reform.roleassignment.util.Constants.TEXT_HYPHEN_PATTERN;
import static uk.gov.hmcts.reform.roleassignment.util.Constants.TEXT_PATTERN;

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
    void validateRoleRequest() {
        try {
            ValidationUtil.validateRoleRequest(TestDataBuilder.buildRequest(Status.APPROVED));
        } catch (Exception e) {
            fail("failed");
        }
    }

    @Test
    void validateRequestedRoles() {
        try {
            ValidationUtil.validateRequestedRoles(TestDataBuilder.buildRequestedRoleCollection(Status.LIVE));
        } catch (Exception e) {
            fail("failed");
        }
    }

    @Test
    void shouldThrowInvalidRequestException_ValidateLists() {
        List list = new ArrayList();
        Assertions.assertThrows(BadRequestException.class, () -> {
            ValidationUtil.validateLists(list);
        });
    }

    @Test
    void testBuildRole() throws IOException {
        List<Role> roles = TestDataBuilder.buildRolesFromFile();
        assertNotNull(roles);
        assertTrue(roles.size() > 1);
    }
}
