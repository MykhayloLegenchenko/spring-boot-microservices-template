package com.example.client.weather;

import com.example.annotation.annotation.ClientInterface;
import com.example.client.weather.dto.CurrentWeatherResult;
import com.example.client.weather.dto.WeatherRequest;
import com.example.common.web.bind.annotation.RequestParamObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import reactor.core.publisher.Mono;

@ClientInterface
@HttpExchange(url = "/api/v1/weather")
@Tag(name = "weather", description = "Operations about weather")
@SecurityRequirement(name = "default")
public interface WeatherReactiveClient {

  @GetExchange("/current")
  @Operation(summary = "Get current weather", description = "Returns current weather")
  Mono<CurrentWeatherResult> current(@Valid @RequestParamObject WeatherRequest request);
}
