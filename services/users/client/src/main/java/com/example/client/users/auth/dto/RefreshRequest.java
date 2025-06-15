package com.example.client.users.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "The refresh request DTO")
public record RefreshRequest(
    @NotBlank
        @Schema(
            description = "JWT refresh token",
            example =
                "eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2V4YW1wbGUuY29tIiwic3ViIjoiMTAwMTJmYTgtYjRhMC00MzBkLTkyY2UtZDY5YmMwYWE2NTQzIiwiZXhwIjoxNzAyOTA3MzMyLCJzY29wZSI6InJlZnJlc2gifQ.MyFBM6k1XJai_ihbtvk0y-mkjHT7JgE6YgxTZv2ri6Y")
        String refreshToken) {}
