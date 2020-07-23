package uk.gov.hmcts.reform.roleassignmentrefactored.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.roleassignmentrefactored.domain.model.Case;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

@RunWith(MockitoJUnitRunner.class)
class CaseTest {


    private Case caseData = new Case("1234", (long)1, 1, "", "",
                                     LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), "",
                                     null, null, null,
                                     null, null, null);

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testToString() {
        assertNotNull(caseData.toString());
        assertTrue(caseData.toString().contains("1234"));
    }

    @Test
    void hasCaseReference() {
        assertTrue(caseData.hasCaseReference());
        assertEquals(1L, caseData.getReference());
    }

    @Test
    void hasCaseReferenceFalse() {
        caseData.setReference(null);
        assertFalse(caseData.hasCaseReference());
    }
}
