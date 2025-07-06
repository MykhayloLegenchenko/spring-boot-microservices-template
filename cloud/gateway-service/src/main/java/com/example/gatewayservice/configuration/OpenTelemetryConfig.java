package com.example.gatewayservice.configuration;

import io.opentelemetry.instrumentation.spring.autoconfigure.OpenTelemetryAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** OpenTelemetry configuration. */
@Configuration
@ConditionalOnClass(OpenTelemetryAutoConfiguration.class)
@ConditionalOnProperty(name = "otel.sdk.disabled", havingValue = "false", matchIfMissing = true)
public class OpenTelemetryConfig {

  @Bean
  TraceContextPropagationFilter traceContextPropagationFilter() {
    return new TraceContextPropagationFilter();
  }
}
