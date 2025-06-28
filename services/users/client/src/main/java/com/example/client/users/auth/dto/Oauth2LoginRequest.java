package com.example.client.users.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;
import org.jspecify.annotations.Nullable;

@Schema(description = "OAuth 2.0 login request payload")
public record Oauth2LoginRequest(
    @NotNull
        @URL
        @Schema(description = "Redirect URL", example = "http://localhost/oauth2/code/google")
        String redirectUri,
    @NotBlank @Schema(description = "Authorization code") String code,
    @Nullable @Schema(description = "Authorization state") String state) {}
