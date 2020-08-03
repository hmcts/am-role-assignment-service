package uk.gov.hmcts.reform.roleassignment.launchdarkly;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.hmcts.reform.roleassignment.util.SecurityUtils;

@Component
public class FeatureConditionEvaluation implements HandlerInterceptor {
    @Autowired
    FeatureToggle featureToggleService;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object arg2) throws Exception {
        String feature = request.getHeader("feature");
        boolean flagStatus = featureToggleService.isFlagEnabled(securityUtils.getServiceName(), feature);
        if (!flagStatus) {
            response.sendError(403, "Forbidden");
        }
        return flagStatus;
    }

    @Autowired
    private SecurityUtils securityUtils;
}
