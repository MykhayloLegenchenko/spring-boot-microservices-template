package com.example.client.users.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "The user DTO")
public record UserDto(
    @Schema(description = "UUID of the user", example = "10012fa8-b4a0-430d-92ce-d69bc0aa6543")
        UUID uuid,
    @Schema(description = "Email address of the user", example = "john@example.com") String email,
    @Schema(description = "Fist name of the user", example = "John") String firstName,
    @Schema(description = "Last name of the user", example = "Doe") String lastName,
    @Schema(description = "User creation timestamp", example = "2023-11-11T06:19:11.018Z")
        Instant createdAt)
    implements UserData {}
