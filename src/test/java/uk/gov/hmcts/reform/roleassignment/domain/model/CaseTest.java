package uk.gov.hmcts.reform.roleassignment.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(MockitoJUnitRunner.class)
class CaseTest {


    private Case caseData = Case.builder().id("1234")
        .jurisdiction("IA")
        .caseTypeId("Asylum")
        .build();


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testToString() {
        assertNotNull(caseData.toString());
        assertTrue(caseData.toString().contains("1234"));
    }

}
