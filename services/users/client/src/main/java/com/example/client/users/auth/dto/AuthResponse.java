package com.example.client.users.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "The authentication response DTO")
public record AuthResponse(
    @Schema(description = "UUID of the user", example = "10012fa8-b4a0-430d-92ce-d69bc0aa6543")
        UUID uuid,
    @Schema(
            description = "JWT access token",
            example =
                "eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2V4YW1wbGUuY29tIiwic3ViIjoiMTAwMTJmYTgtYjRhMC00MzBkLTkyY2UtZDY5YmMwYWE2NTQzIiwiZXhwIjoxNzAyOTA1ODMyLCJzY29wZSI6InVzZXIifQ.Ybcq5da-8OXuzFjRXiS8KBPIp1bkC2KelPqJkp4qZEc")
        String accessToken,
    @Schema(description = "The type of token", example = "Bearer") String tokenType,
    @Schema(description = "Access token expiration time in seconds", example = "600") int expiresIn,
    @Schema(
            description = "JWT refresh token",
            example =
                "eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2V4YW1wbGUuY29tIiwic3ViIjoiMTAwMTJmYTgtYjRhMC00MzBkLTkyY2UtZDY5YmMwYWE2NTQzIiwiZXhwIjoxNzAyOTA3MzMyLCJzY29wZSI6InJlZnJlc2gifQ.MyFBM6k1XJai_ihbtvk0y-mkjHT7JgE6YgxTZv2ri6Y")
        String refreshToken,
    @Schema(description = "Token scope", example = "user") String scope) {}
