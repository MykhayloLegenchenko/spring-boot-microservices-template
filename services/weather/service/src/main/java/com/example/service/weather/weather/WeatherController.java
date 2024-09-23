package com.example.service.weather.weather;

import com.example.client.weather.WeatherReactiveClient;
import com.example.client.weather.dto.CurrentWeatherResult;
import com.example.client.weather.dto.WeatherRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class WeatherController implements WeatherReactiveClient {
  private final WeatherService weatherService;

  @Override
  public Mono<CurrentWeatherResult> current(WeatherRequest request) {
    return weatherService.current(request);
  }
}
