package uk.gov.hmcts.reform.roleassignment.launchdarkly;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.ForbiddenException;
import uk.gov.hmcts.reform.roleassignment.util.SecurityUtils;

@Component
@AllArgsConstructor
public class FeatureConditionEvaluation implements HandlerInterceptor {

    @Autowired
    private final FeatureToggleService featureToggleService;

    @Autowired
    private final SecurityUtils securityUtils;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             @NotNull HttpServletResponse response, @NotNull Object arg2) throws Exception {

        Map<String, String> launchDarklyUrlMap = featureToggleService.getLaunchDarklyMap();

        if (launchDarklyUrlMap.get(request.getRequestURI()) == null
            || launchDarklyUrlMap.get(request.getRequestURI()).isEmpty()) {
            throw new ForbiddenException("The endpoint is not configured in Launch Darkly");
        }

        boolean flagStatus = featureToggleService
            .isFlagEnabled(securityUtils.getServiceName(), launchDarklyUrlMap.get(request.getRequestURI()));
        if (!flagStatus) {
            throw new ForbiddenException("Forbidden");
        }
        return flagStatus;
    }

}
