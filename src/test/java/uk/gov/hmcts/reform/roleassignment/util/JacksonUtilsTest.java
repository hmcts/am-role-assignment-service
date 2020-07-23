package uk.gov.hmcts.reform.roleassignment.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class JacksonUtilsTest {

    @Test
    void convertValueJsonNode() {
        assertNotNull(JacksonUtils.convertValueJsonNode("NodeMe"));
    }

    @Test
    void getHashMapTypeReference() {
        assertNotNull(JacksonUtils.getHashMapTypeReference());
    }
}
