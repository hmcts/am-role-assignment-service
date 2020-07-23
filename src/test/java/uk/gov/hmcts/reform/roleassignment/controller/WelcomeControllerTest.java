package uk.gov.hmcts.reform.roleassignment.controller;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.assignment.controller.WelcomeController;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(MockitoJUnitRunner.class)
class WelcomeControllerTest {

    @InjectMocks
    private WelcomeController sut = new WelcomeController();

    @Test
    void index() {
        assertEquals("redirect:swagger-ui.html", sut.index());
    }

    @Test
    void welcome() {
        assertEquals("welcome to role assignment service", sut.welcome());
    }
}
