package com.example.client.users.role.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "The role DTO")
public record RoleDto(
    @NotBlank
        @Size(max = 255)
        @Pattern(regexp = "^[A-Z][A-Z0-9]*(_[A-Z0-9]+){0,10}$")
        @Schema(description = "Role name", example = "MANAGER")
        String name) {}
