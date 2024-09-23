package com.example.autoconfigure.context.config;

import java.util.HashMap;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.ClassUtils;

/**
 * {@link EnvironmentPostProcessor} that provides default properties from
 * "classpath:/autoconfiguration/properties" directory.
 *
 * <p>p>Provides default active profile: "dev" for normal context or "test" for test context.
 */
public class AutoconfigurationEnvironmentPostProcessor
    implements EnvironmentPostProcessor, Ordered {

  /** Order of post processor, set to run before {@link ConfigDataEnvironmentPostProcessor}. */
  public static final int ORDER = ConfigDataEnvironmentPostProcessor.ORDER - 1;

  @Override
  public void postProcessEnvironment(
      ConfigurableEnvironment environment, SpringApplication application) {

    var properties = new HashMap<String, Object>();
    if (!environment.containsProperty("spring.profiles.active")) {
      var testContext =
          ClassUtils.isPresent(
              "org.junit.jupiter.api.Test",
              AutoconfigurationEnvironmentPostProcessor.class.getClassLoader());

      properties.put("spring.profiles.active", testContext ? "test" : "dev");
    }

    if (!environment.containsProperty("spring.config.location")) {
      properties.put(
          "spring.config.location", "classpath:/autoconfiguration/properties/;classpath:/");
    }

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
}
