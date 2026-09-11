package uk.gov.hmcts.reform.roleassignment.auditlog;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.hmcts.reform.roleassignment.ApplicationParams;
import uk.gov.hmcts.reform.roleassignment.auditlog.aop.AuditContext;
import uk.gov.hmcts.reform.roleassignment.auditlog.aop.AuditContextHolder;
import uk.gov.hmcts.reform.roleassignment.domain.model.MutableHttpServletRequest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Slf4j
public class AuditInterceptor implements HandlerInterceptor {

    public static final String REQUEST_ID = "request-id";

    private final AuditService auditService;
    private final ApplicationParams applicationParams;

    public AuditInterceptor(AuditService auditService, ApplicationParams applicationParams) {
        this.auditService = auditService;
        this.applicationParams = applicationParams;
    }

    @Override
    public void afterCompletion(@NotNull HttpServletRequest request,
                                @NotNull HttpServletResponse response, @NotNull Object handler,
                                @Nullable Exception ex) {
        if (applicationParams.isAuditLogEnabled() && hasAuditAnnotation(handler)) {
            if (!applicationParams.getAuditLogIgnoreStatuses().contains(response.getStatus())) {
                var auditContext = AuditContextHolder.getAuditContext();
                auditContext = populateHttpSemantics(auditContext, request, response);
                try {
                    auditService.audit(auditContext);
                } catch (Exception e) {  // Ignoring audit failures
                    log.error("Error while auditing the request data:{}", e.getMessage());
                }
            }
            AuditContextHolder.remove();
        }
    }

    private boolean hasAuditAnnotation(Object handler) {
        return handler instanceof HandlerMethod && ((HandlerMethod) handler).hasMethodAnnotation(LogAudit.class);
    }

    private AuditContext populateHttpSemantics(AuditContext auditContext,
                                               HttpServletRequest request, HttpServletResponse response) {
        AuditContext context = (auditContext != null) ? auditContext : new AuditContext();
        context.setHttpStatus(response.getStatus());
        context.setHttpMethod(request.getMethod());
        context.setRequestPath(request.getRequestURI());
        if ((context.getResponseTime() != null && context.getResponseTime() > 500)
            || context.getHttpStatus() == 422
            || log.isDebugEnabled()) {
            context.setRequestPayloadHash(hash(new MutableHttpServletRequest(request).getBodyAsString()));
        }
        return context;
    }

    private String hash(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
