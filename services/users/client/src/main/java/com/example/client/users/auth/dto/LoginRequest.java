package com.example.client.users.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "The login request DTO")
public record LoginRequest(
    @Email @NotBlank @Schema(description = "Email address", example = "john@example.com")
        String email,
    @NotBlank @Schema(description = "Password", example = "123456") String password) {}
