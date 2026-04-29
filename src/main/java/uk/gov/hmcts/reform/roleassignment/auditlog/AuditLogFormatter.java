package uk.gov.hmcts.reform.roleassignment.auditlog;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.roleassignment.util.JacksonUtils;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
public class AuditLogFormatter {

    public static final String TAG = "LA-AM-RAS";

    public String format(AuditEntry entry) {
        Map<String, Object> logEntry = new LinkedHashMap<>();
        logEntry.put("tag", TAG);
        add(logEntry, "dateTime", entry.getDateTime());
        add(logEntry, "operationType", entry.getOperationType());
        add(logEntry, "assignerId", entry.getAssignerId());
        add(logEntry, "assignmentId", entry.getAssignmentId());
        add(logEntry, "assignmentSize", entry.getAssignmentSize());
        add(logEntry, "invokingService", entry.getInvokingService());
        add(logEntry, "endpointCalled", entry.getPath());
        add(logEntry, "operationalOutcome", entry.getHttpStatus());
        add(logEntry, "actorId", entry.getActorId());
        add(logEntry, "process", entry.getProcess());
        add(logEntry, "reference", entry.getReference());
        add(logEntry, "roleName", entry.getRoleName());
        add(logEntry, "authenticatedUserId", entry.getAuthenticatedUserId());
        add(logEntry, "correlationId", entry.getCorrelationId());
        add(logEntry, "requestPayloadHash", entry.getRequestPayloadHash());
        add(logEntry, "responseTime", entry.getResponseTime());
        try {
            return JacksonUtils.MAPPER.writeValueAsString(logEntry);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to format audit log entry", e);
        }
    }

    private void add(Map<String, Object> logEntry, String label, @Nullable Object value) {
        if (value instanceof String string && isBlank(string)) {
            return;
        }
        if (value instanceof Collection<?> collection && collection.isEmpty()) {
            return;
        }
        if (value != null) {
            logEntry.put(label, value);
        }
    }
}
