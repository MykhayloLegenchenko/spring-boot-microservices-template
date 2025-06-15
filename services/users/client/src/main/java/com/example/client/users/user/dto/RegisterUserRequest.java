package com.example.client.users.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "The user registration DTO")
public record RegisterUserRequest(
    @Email
        @NotBlank
        @Size(max = 100)
        @Schema(description = "Email address of the user", example = "john@example.com")
        String email,
    @NotBlank @Size(max = 50) @Schema(description = "Fist name of the user", example = "John")
        String firstName,
    @NotBlank @Size(max = 50) @Schema(description = "Last name of the user", example = "Doe")
        String lastName,
    @NotBlank @Size(min = 6) @Schema(description = "User password", example = "123456")
        String password) {}
