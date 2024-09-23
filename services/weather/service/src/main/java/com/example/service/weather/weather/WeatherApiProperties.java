package com.example.service.weather.weather;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("api.weather")
@Validated
public record WeatherApiProperties(@NotNull @URL String url, @NotBlank String apiKey) {}
