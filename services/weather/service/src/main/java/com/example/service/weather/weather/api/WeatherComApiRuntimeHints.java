package com.example.service.weather.weather.api;

import com.example.common.aot.RuntimeHintsUtils;
import com.example.service.weather.weather.api.dto.ApiCurrentWeatherResult;
import com.example.service.weather.weather.api.dto.ApiLocationDto;
import com.example.service.weather.weather.api.dto.ApiWeatherDto;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

/** Registers runtime hints for native image compatibility of the weather.com API client. */
public class WeatherComApiRuntimeHints implements RuntimeHintsRegistrar {
  @Override
  public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {
    RuntimeHintsUtils.registerDto(
        hints, ApiCurrentWeatherResult.class, ApiLocationDto.class, ApiWeatherDto.class);
    RuntimeHintsUtils.registerClientInterface(hints, WeatherComApiClient.class);
  }
}
