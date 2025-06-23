package com.gary.common.aspect;

import com.gary.common.annotations.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class TimedAspect {

    private final MeterRegistry meterRegistry;

    @Around("@annotation(timed)")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint, Timed timed) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            return joinPoint.proceed();
        } finally {
            sample.stop(
                    Timer.builder("custom.method.execution.time")
                            .description("Execution time of annotated methods")
                            .tag("method", methodName)
                            .tag("action", timed.value())
                            .register(meterRegistry)
            );

            log.info("Timed method='{}' recorded in Prometheus", methodName);
        }
    }
}
