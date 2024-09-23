package com.example.client.weather.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "The current weather result DTO")
public record CurrentWeatherResult(
    @Schema(description = "Location") LocationDto location,
    @Schema(description = "Current weather") WeatherDto weather) {}
