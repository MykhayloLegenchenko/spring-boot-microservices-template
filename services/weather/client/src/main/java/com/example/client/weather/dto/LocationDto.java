package com.example.client.weather.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "The location DTO")
public record LocationDto(
    @Schema(description = "Name", example = "New York") String name,
    @Schema(description = "Region", example = "New York") String region,
    @Schema(description = "Country", example = "United States of America") String country,
    @Schema(description = "Latitude", example = "40.71") double lat,
    @Schema(description = "Longitude", example = "-74.01") double lon,
    @Schema(description = "Timezone ID", example = "America/New_York") String tzId) {}
