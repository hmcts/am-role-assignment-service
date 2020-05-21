package uk.gov.hmcts.reform.roleassignment.model.enums;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.roleassignment.domain.model.SecurityClassification;

class SecurityClassificationTest {

    @Test
    void shoudGetFromHierarchy() {
        assertNotNull(SecurityClassification.fromHierarchy(1));
    }
}
