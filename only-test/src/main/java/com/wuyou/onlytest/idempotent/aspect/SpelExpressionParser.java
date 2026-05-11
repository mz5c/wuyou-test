package com.wuyou.onlytest.idempotent.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SpelExpressionParser {

    private final org.springframework.expression.spel.standard.SpelExpressionParser parser =
            new org.springframework.expression.spel.standard.SpelExpressionParser();
    private final Map<String, Expression> cache = new ConcurrentHashMap<>();

    public String parse(String expression, ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        if (paramNames == null) {
            throw new IllegalArgumentException(
                    "Cannot resolve parameter names. Compile with -parameters flag.");
        }
        Object[] paramValues = joinPoint.getArgs();
        StandardEvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < paramNames.length; i++) {
            context.setVariable(paramNames[i], paramValues[i]);
        }
        Expression exp = cache.computeIfAbsent(expression, parser::parseExpression);
        return exp.getValue(context, String.class);
    }
}
