package uk.gov.hmcts.reform.roleassignment.launchdarkly;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.ForbiddenException;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.roleassignment.util.SecurityUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FeatureConditionEvaluationTest {

    @Mock
    FeatureToggleService featureToggleService = mock(FeatureToggleService.class);

    @Mock
    SecurityUtils securityUtils = mock(SecurityUtils.class);

    @Mock
    HttpServletRequest request = mock(HttpServletRequest.class);

    @Mock
    HttpServletResponse response = mock(HttpServletResponse.class);

    Map<String, String> launchDarklyMap;

    Object object = new Object();

    @Before
    public void initializeMocks() {
        launchDarklyMap = new HashMap<>();
        launchDarklyMap.put("/am/role-assignments/ld/endpoint", "get-ld-flag");

        when(featureToggleService.getLaunchDarklyMap()).thenReturn(launchDarklyMap);
    }

    @InjectMocks
    FeatureConditionEvaluation featureConditionEvaluation = new FeatureConditionEvaluation(
        featureToggleService,
        securityUtils
    );

    @Test
    public void getPositiveResponseForFlag() throws Exception {
        when(request.getRequestURI()).thenReturn("/am/role-assignments/ld/endpoint");
        when(featureToggleService.isFlagEnabled(any(), any())).thenReturn(true);
        when(featureToggleService.isValidFlag(any())).thenReturn(true);
        Assertions.assertTrue(featureConditionEvaluation.preHandle(request, response, new Object()));
    }

    @Test
    public void getNegativeResponseForFlag() throws Exception {
        when(request.getRequestURI()).thenReturn("/am/role-assignments/ld/endpoint1");
        when(featureToggleService.isFlagEnabled(any(), any())).thenReturn(false);
        Assertions.assertThrows(ForbiddenException.class, () -> {
            featureConditionEvaluation.preHandle(request, response, object);
        });
    }

    @Test
    public void expectExceptionForNonRegisteredURI() {
        when(request.getRequestURI()).thenReturn("");
        Assertions.assertThrows(ForbiddenException.class, () -> {
            featureConditionEvaluation.preHandle(request, response, object);
        });
    }

    @Test
    public void expectExceptionForInvalidFlagName() {
        when(request.getRequestURI()).thenReturn("/am/role-assignments/ld/endpoint");
        when(featureToggleService.isValidFlag(any())).thenReturn(false);
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            featureConditionEvaluation.preHandle(request, response, object);
        });
    }

}
