package com.example.shopping.shared.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObservabilityConfig {

    public ObservabilityConfig(MeterRegistry meterRegistry) {
        meterRegistry.config().commonTags("application", "shopping-service");
    }
}

