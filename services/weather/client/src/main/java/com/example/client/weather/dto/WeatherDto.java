package com.example.client.weather.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "The weather DTO")
public record WeatherDto(
    @Schema(description = "Temperature in Celsius", example = "34.4") double tempC,
    @Schema(description = "Wind speed in kilometers per hour", example = "25.9") double windKph,
    @Schema(description = "Wind direction", example = "S") String windDir) {}
