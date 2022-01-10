package uk.gov.hmcts.reform.roleassignment.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.roleassignment.data.DatabaseChangelogLockEntity;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PersistenceService;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(MockitoJUnitRunner.class)
class WelcomeControllerTest {

    @Mock
    private PersistenceService persistenceService;

    @InjectMocks
    private final WelcomeController sut = new WelcomeController();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void index() {
        assertEquals("redirect:swagger-ui.html", sut.index());
    }

    @Test
    void welcome() {
        assertEquals("welcome to role assignment service", sut.welcome());
    }

    @Test
    void releaseDbLock() {
        DatabaseChangelogLockEntity databaseChangelogLockEntity = DatabaseChangelogLockEntity.builder().id(1).locked(
            false).lockedby(null).build();
        Mockito.when(persistenceService.releaseDatabaseLock(1)).thenReturn(databaseChangelogLockEntity);
        ResponseEntity<DatabaseChangelogLockEntity> responseEntity = sut.dbReleaseLock();
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertFalse(Objects.requireNonNull(responseEntity.getBody()).isLocked());
    }


    @Test
    void verifyUncoveredException() {
        ResponseEntity<String> responseEntity = sut.getException("unProcessableEntity");
        Assertions.assertNull(responseEntity,"");


    }
}
