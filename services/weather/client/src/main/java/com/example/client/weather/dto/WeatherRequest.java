package com.example.client.weather.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "The weather request DTO")
public record WeatherRequest(
    @NotBlank
        @JsonDeserialize()
        @Schema(
            description =
                "Pass US Zipcode, UK Postcode, Canada Postalcode, IP address, Latitude/Longitude (decimal degree) or city name.",
            example = "New York")
        String query) {}
