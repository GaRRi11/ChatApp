package com.gary.common.aspect;

import com.gary.common.annotations.LoggableAction;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Around("@annotation(loggableAction)")
    public Object logMethod(ProceedingJoinPoint joinPoint, LoggableAction loggableAction) throws Throwable {
        long start = System.currentTimeMillis();
        String timestamp = LocalDateTime.now().format(FORMATTER);

        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        Object result = null;
        Throwable thrown = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable t) {
            thrown = t;
            throw t;
        } finally {
            long duration = System.currentTimeMillis() - start;

            if (thrown == null) {
                log.info(
                        "Action='{}', Class='{}', Method='{}', Args={}, Timestamp='{}', ReturnValue={}, ExecutionTime={}ms",
                        loggableAction.value(),
                        className,
                        methodName,
                        Arrays.toString(args),
                        timestamp,
                        result,
                        duration
                );
            } else {
                log.error(
                        "Action='{}', Class='{}', Method='{}', Args={}, Timestamp='{}', Exception='{}', ExecutionTime={}ms",
                        loggableAction.value(),
                        className,
                        methodName,
                        Arrays.toString(args),
                        timestamp,
                        thrown.toString(),
                        duration
                );
            }
        }
    }
}
