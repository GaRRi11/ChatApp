package com.gary.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Timed;

@Aspect
@Component
@Slf4j
public class TimedAspect {

    @Around("@annotation(timed)")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint, Timed timed) throws Throwable {
        long start = System.nanoTime();
        Object result = joinPoint.proceed();
        long duration = System.nanoTime() - start;

        String methodName = joinPoint.getSignature().toShortString();
        log.info("Timed method='{}' took {} ms", methodName, duration / 1_000_000);

        // Optionally push to Prometheus / Micrometer
        return result;
    }
}

