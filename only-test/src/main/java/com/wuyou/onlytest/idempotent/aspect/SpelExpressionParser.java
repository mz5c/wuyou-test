package com.wuyou.onlytest.idempotent.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Component
public class SpelExpressionParser {
    private final org.springframework.expression.ExpressionParser parser =
            new org.springframework.expression.spel.standard.SpelExpressionParser();

    public String parse(String expression, ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] paramValues = joinPoint.getArgs();
        org.springframework.expression.spel.support.StandardEvaluationContext context =
                new org.springframework.expression.spel.support.StandardEvaluationContext();
        for (int i = 0; i < paramNames.length; i++) {
            context.setVariable(paramNames[i], paramValues[i]);
        }
        return parser.parseExpression(expression).getValue(context, String.class);
    }
}
