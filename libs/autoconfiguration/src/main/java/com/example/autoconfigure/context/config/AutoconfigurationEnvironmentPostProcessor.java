package com.example.autoconfigure.context.config;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.ClassUtils;

/**
 * Autoconfiguration {@link EnvironmentPostProcessor}:
 *
 * <ul>
 *   <li>provides default active profile: "dev" for normal context or "test" for test context.
 *   <li>provides default config location {@code
 *       "classpath:/autoconfiguration/properties/;classpath:/"}
 *   <li>makes OpenTelemetry global autoconfiguration enabled by default.
 * </ul>
 */
public class AutoconfigurationEnvironmentPostProcessor
    implements EnvironmentPostProcessor, Ordered {

  private static final String GLOBAL_AUTOCONFIGURE_ENABLED_PROPERTY =
      "otel.java.global-autoconfigure.enabled";

  /** Run before {@link ConfigDataEnvironmentPostProcessor}. */
  public static final int ORDER = ConfigDataEnvironmentPostProcessor.ORDER - 1;

  @Override
  public void postProcessEnvironment(
      ConfigurableEnvironment environment, SpringApplication application) {

    var properties = new HashMap<String, Object>();
    setupActiveProfile(environment, properties);
    setupConfigLocation(environment, properties);
    setupOpenTelemetry(environment);

    if (!properties.isEmpty()) {
      environment
          .getPropertySources()
          .addLast(new MapPropertySource("autoconfiguration", properties));
    }
  }

  @Override
  public int getOrder() {
    return ORDER;
  }

  private static void setupActiveProfile(
      ConfigurableEnvironment environment, Map<String, Object> properties) {

    if (!environment.containsProperty("spring.profiles.active")) {
      var testContext =
          ClassUtils.isPresent(
              "org.junit.jupiter.api.Test",
              AutoconfigurationEnvironmentPostProcessor.class.getClassLoader());

      properties.put("spring.profiles.active", testContext ? "test" : "dev");
    }
  }

  private static void setupConfigLocation(
      ConfigurableEnvironment environment, Map<String, Object> properties) {

    if (!environment.containsProperty("spring.config.location")) {
      properties.put(
          "spring.config.location", "classpath:/autoconfiguration/properties/;classpath:/");
    }
  }

  private static void setupOpenTelemetry(ConfigurableEnvironment environment) {
    if (!environment.containsProperty(GLOBAL_AUTOCONFIGURE_ENABLED_PROPERTY)) {
      System.setProperty(GLOBAL_AUTOCONFIGURE_ENABLED_PROPERTY, "true");
    }
  }
}
