package com.gary.common.metric;

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

    public void incrementMetric(String name) {
        meterRegistry.counter(name).increment();
    }

}
