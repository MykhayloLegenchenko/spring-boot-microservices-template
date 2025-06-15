package com.example.client.users.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "The user update DTO")
public record UpdateUserRequest(
    @Email
        @NotBlank
        @Size(max = 100)
        @Schema(description = "Email address of the user", example = "john@example.com")
        String email,
    @NotBlank @Size(max = 50) @Schema(description = "Fist name of the user", example = "John")
        String firstName,
    @NotBlank @Size(max = 50) @Schema(description = "Last name of the user", example = "Doe")
        String lastName) {}
