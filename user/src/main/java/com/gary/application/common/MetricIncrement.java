package com.gary.application.common;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MetricIncrement {

    private final MeterRegistry meterRegistry;

    public void incrementMetric(String name, String status) {
        meterRegistry.counter(name, "status", status).increment();
    }

}
