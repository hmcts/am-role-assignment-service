package uk.gov.hmcts.reform.roleassignment.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.roleassignment.data.DatabaseChangelogLockEntity;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PersistenceService;

@RestController
public class WelcomeController {

    @Autowired
    PersistenceService persistenceService;

    @GetMapping(value = "/swagger")
    public String index() {
        return "redirect:swagger-ui.html";
    }

    @GetMapping(value = "/welcome")
    public String welcome() {
        return "welcome to role assignment service";
    }

    @GetMapping("/db/releaselock")
    public ResponseEntity<DatabaseChangelogLockEntity> dbReleaseLock() {
        DatabaseChangelogLockEntity entity = persistenceService.releaseDatabaseLock(1);
        return ResponseEntity.ok(entity);
    }
}
