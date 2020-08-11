package uk.gov.hmcts.reform.roleassignment.launchdarkly;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.hmcts.reform.roleassignment.util.SecurityUtils;

@Component
public class FeatureConditionEvaluation implements HandlerInterceptor {

    @Autowired
    FeatureToggleService featureToggleService;

    @Autowired
    private SecurityUtils securityUtils;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             @NotNull HttpServletResponse response, @NotNull Object arg2) throws Exception {

        Map<String, String> launchDarklyUrlMap = featureToggleService.getLaunchDarklyMap();

        if (launchDarklyUrlMap.get(request.getRequestURI()).isEmpty()) {
            response.sendError(403, "The endpoint is not configured in Launch Darkly");
        }

        boolean flagStatus = featureToggleService
            .isFlagEnabled(securityUtils.getServiceName(), launchDarklyUrlMap.get(request.getRequestURI()));
        if (!flagStatus) {
            response.sendError(403, "Forbidden");
        }
        return flagStatus;
    }

}
