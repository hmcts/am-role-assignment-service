package uk.gov.hmcts.reform.roleassignment.auditlog;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LoggerAuditRepository implements AuditRepository {

    private AuditLogFormatter logFormatter;

    @Autowired
    public LoggerAuditRepository(AuditLogFormatter auditLogFormatter) {
        this.logFormatter = auditLogFormatter;
    }

    @Override
    public void save(final AuditEntry auditEntry) {
        log.info(logFormatter.format(auditEntry));
    }
}
