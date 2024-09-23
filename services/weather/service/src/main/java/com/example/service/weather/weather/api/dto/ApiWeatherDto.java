package com.example.service.weather.weather.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record ApiWeatherDto(
    @JsonProperty("last_updated_epoch") int lastUpdatedEpoch,
    @NotNull @JsonProperty("last_updated") String lastUpdated,
    @JsonProperty("temp_c") double tempC,
    @JsonProperty("temp_f") double tempF,
    @JsonProperty("wind_kph") double windMph,
    @JsonProperty("wind_mph") double windKph,
    @NotNull @JsonProperty("wind_dir") String windDir) {}
