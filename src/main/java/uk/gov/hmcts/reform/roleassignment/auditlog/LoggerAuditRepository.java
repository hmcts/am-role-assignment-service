package uk.gov.hmcts.reform.roleassignment.auditlog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LoggerAuditRepository implements AuditRepository {

    private static final Logger LOG = LoggerFactory.getLogger(LoggerAuditRepository.class);

    private AuditLogFormatter logFormatter;

    @Autowired
    public LoggerAuditRepository(AuditLogFormatter auditLogFormatter) {
        this.logFormatter = auditLogFormatter;
    }

    @Override
    public void save(final AuditEntry auditEntry) {
        var msg = logFormatter.format(auditEntry);
        LOG.info(msg);
    }
}
