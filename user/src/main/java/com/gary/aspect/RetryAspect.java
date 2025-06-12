package com.gary.aspect;

import com.gary.annotations.RetryableOperation;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class RetryAspect {

    @Around("@annotation(retryable)")
    public Object retry(ProceedingJoinPoint joinPoint, RetryableOperation retryable) throws Throwable {
        int attempts = 0;
        Throwable lastError = null;

        while (attempts < retryable.maxAttempts()) {
            try {
                return joinPoint.proceed();
            } catch (Throwable ex) {
                boolean shouldRetry = Arrays.stream(retryable.retryOn()).anyMatch(t -> t.isAssignableFrom(ex.getClass()));
                if (!shouldRetry) throw ex;

                lastError = ex;
                attempts++;
                log.warn("Retry {}/{} for {} after exception: {}", attempts, retryable.maxAttempts(), joinPoint.getSignature(), ex.getMessage());
                Thread.sleep(retryable.delay());
            }
        }
        throw lastError;
    }
}

