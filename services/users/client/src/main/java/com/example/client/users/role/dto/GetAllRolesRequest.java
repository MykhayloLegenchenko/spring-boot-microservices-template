package com.example.client.users.role.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.springframework.lang.Nullable;

@Builder(toBuilder = true)
@Schema(description = "The find users request DTO")
public record GetAllRolesRequest(
    @Nullable @Schema(description = "Sort order. Fields: id, name", example = "name:desc")
        String sort) {}
