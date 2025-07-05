package com.example.client.weather;

import com.example.common.aot.RuntimeHintsUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

/** Registers runtime hints for native image compatibility of the Weather API client. */
public class WeatherClientRuntimeHints implements RuntimeHintsRegistrar {
  @Override
  public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {
    RuntimeHintsUtils.registerClientApi(
        hints,
        WeatherBlockingClient.class,
        WeatherReactiveClient.class,
        "com.example.client.weather.dto");
  }
}
