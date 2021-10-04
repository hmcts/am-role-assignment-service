package uk.gov.hmcts.reform.roleassignment.auditlog;

import com.launchdarkly.shaded.org.jetbrains.annotations.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import uk.gov.hmcts.reform.roleassignment.ApplicationParams;
import uk.gov.hmcts.reform.roleassignment.auditlog.aop.AuditContext;
import uk.gov.hmcts.reform.roleassignment.auditlog.aop.AuditContextHolder;
import uk.gov.hmcts.reform.roleassignment.domain.model.MutableHttpServletRequest;
import uk.gov.hmcts.reform.roleassignment.util.ValidationUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class AuditInterceptor extends HandlerInterceptorAdapter {

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
        long startTime = System.currentTimeMillis();
        try {
            ValidationUtil.compareHttpMethods(request.getMethod());
        } catch (Exception e) {  // Ignoring audit failures
            log.error("Error while auditing the request data:{}", e.getMessage());
        }
        if (applicationParams.isAuditLogEnabled() && hasAuditAnnotation(handler)) {
            log.debug("afterCompletion execution started at {}", startTime);
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
        log.debug(
            " >> afterCompletion execution finished at {} . Time taken = {} milliseconds",
            System.currentTimeMillis(),
            Math.subtractExact(System.currentTimeMillis(), startTime)
        );
    }

    private boolean hasAuditAnnotation(Object handler) {
        return handler instanceof HandlerMethod && ((HandlerMethod) handler).hasMethodAnnotation(LogAudit.class);
    }

    private AuditContext populateHttpSemantics(AuditContext auditContext,
                                               HttpServletRequest request, HttpServletResponse response) {
        AuditContext context = (auditContext != null) ? auditContext : new AuditContext();
        context.setHttpStatus(response.getStatus());
        String httpMethod = ValidationUtil.compareHttpMethods(request.getMethod());
        context.setHttpMethod(httpMethod);
        context.setRequestPath(request.getRequestURI());
        if (log.isDebugEnabled()) {
            context.setRequestPayload(new MutableHttpServletRequest(request).getBodyAsString());
        }
        return context;
    }
}
