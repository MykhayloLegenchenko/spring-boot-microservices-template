package com.example.service.weather.weather.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record ApiLocationDto(
    @NotNull String name,
    @NotNull String region,
    @NotNull String country,
    double lat,
    double lon,
    @NotNull @JsonProperty("tz_id") String tzId,
    @JsonProperty("localtime_epoch") int localtimeEpoch,
    @NotNull String localtime) {}
