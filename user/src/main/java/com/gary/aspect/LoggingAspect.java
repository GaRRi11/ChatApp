package com.gary.aspect;

import com.gary.annotations.LoggableAction;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Around("@annotation(loggableAction)")
    public Object logMethod(ProceedingJoinPoint joinPoint, LoggableAction loggableAction) throws Throwable {
        long start = System.currentTimeMillis();

        Object result = joinPoint.proceed();

        long time = System.currentTimeMillis() - start;
        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();

        log.info("Action='{}', Method={}, Args={}, ExecutionTime={}ms", loggableAction.value(), methodName, Arrays.toString(args), time);
        return result;
    }
}

