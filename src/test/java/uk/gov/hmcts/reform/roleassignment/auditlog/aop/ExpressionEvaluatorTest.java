package uk.gov.hmcts.reform.roleassignment.auditlog.aop;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.util.ReflectionUtils;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExpressionEvaluatorTest {

    private ExpressionEvaluator evaluator;

    @Before
    public void setup() {
        evaluator = new ExpressionEvaluator();
    }

    @Test
    public void shouldCreateEvaluationContext() {

        Method method = ReflectionUtils.findMethod(EvaluateMethods.class, "evaluate", String.class, Boolean.class);
        EvaluationContext context = evaluator.createEvaluationContext(this, EvaluateMethods.class, method, new Object[]{
            "test", true});

        assertThat(context.lookupVariable("a0")).isEqualTo("test");
        assertThat(context.lookupVariable("p0")).isEqualTo("test");
        assertThat(context.lookupVariable("foo")).isEqualTo("test");

        assertThat(context.lookupVariable("a1")).isEqualTo(true);
        assertThat(context.lookupVariable("p1")).isEqualTo(true);
        assertThat(context.lookupVariable("flag")).isEqualTo(true);

        assertThat(context.lookupVariable("a2")).isNull();
        assertThat(context.lookupVariable("p2")).isNull();
    }

    @Test
    public void shouldParseValidExpressions() {

        Method method = ReflectionUtils.findMethod(EvaluateMethods.class, "evaluate", String.class, Boolean.class);
        EvaluationContext context = evaluator.createEvaluationContext(this, EvaluateMethods.class, method, new Object[]{
            "test", true});
        AnnotatedElementKey elementKey = new AnnotatedElementKey(method, EvaluateMethods.class);

        assertThat(evaluator.condition("#foo", elementKey, context, String.class)).isEqualTo("test");
        assertThat(evaluator.condition("#flag", elementKey, context, Boolean.class)).isTrue();
        assertThat(evaluator.condition("#unknownProperty", elementKey, context, String.class)).isNull();
    }

    @Test
    public void shouldParseBeanExpressions() {
        Method method = ReflectionUtils.findMethod(
            EvaluateMethods.class,
            "evaluate",
            String.class,
            RoleAssignment.class
        );
        RoleAssignment roleAssignment = new RoleAssignment();
        roleAssignment.setRoleName("judge");
        EvaluationContext context = evaluator.createEvaluationContext(this, EvaluateMethods.class, method, new Object[]{
            "test", roleAssignment});
        AnnotatedElementKey elementKey = new AnnotatedElementKey(method, EvaluateMethods.class);

        assertThat(evaluator.condition("#roleAssignment.roleName",
                                       elementKey, context,
                                       String.class
        )).isEqualTo("judge");
    }


    @Test
    public void shouldThrowErrorWhenPropertyNotFound() {

        Method method = ReflectionUtils.findMethod(
            EvaluateMethods.class,
            "evaluate",
            String.class,
            RoleAssignment.class
        );
        RoleAssignment roleAssignment = new RoleAssignment();
        roleAssignment.setRoleName("judge");
        EvaluationContext context = evaluator.createEvaluationContext(this, EvaluateMethods.class, method, new Object[]{
            "test", roleAssignment});
        AnnotatedElementKey elementKey = new AnnotatedElementKey(method, EvaluateMethods.class);

        Exception exception = assertThrows(SpelEvaluationException.class, () ->
            evaluator.condition("#roleAssignment.unknownProperty", elementKey, context, String.class)
        );

        assertThat(exception.getMessage().contains("EL1008E: Property or field 'unknownProperty' cannot be found"))
            .toString();


    }

    @SuppressWarnings("unused")
    private static class EvaluateMethods {

        private void evaluate(String foo, Boolean flag) {
        }

        private void evaluate(String foo, RoleAssignment roleAssignment) {
        }
    }
}
