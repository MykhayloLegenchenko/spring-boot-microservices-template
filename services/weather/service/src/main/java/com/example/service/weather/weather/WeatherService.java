package com.example.service.weather.weather;

import com.example.client.weather.dto.CurrentWeatherResult;
import com.example.client.weather.dto.WeatherRequest;
import com.example.service.weather.weather.api.WeatherComApiClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@EnableConfigurationProperties(WeatherApiProperties.class)
public class WeatherService {
  private final WeatherComApiClient weatherComClient;
  private final WeatherMapper weatherMapper;

  public WeatherService(
      WeatherApiProperties weatherApiProperties,
      WeatherMapper weatherMapper,
      WebClient.Builder webClientBuilder) {

    weatherComClient =
        WeatherComApiClient.create(
            webClientBuilder, weatherApiProperties.url(), weatherApiProperties.apiKey());

    this.weatherMapper = weatherMapper;
  }

  public Mono<CurrentWeatherResult> current(WeatherRequest request) {
    return weatherComClient.current(request.query()).map(weatherMapper::toCurrentWeatherResult);
  }
}
