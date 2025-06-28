package com.example.client.users.auth;

import com.example.annotation.annotation.ClientInterface;
import com.example.client.users.auth.dto.AuthResponse;
import com.example.client.users.auth.dto.LoginRequest;
import com.example.client.users.auth.dto.Oauth2LoginRequest;
import com.example.client.users.auth.dto.RefreshRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@ClientInterface
@HttpExchange(url = "/api/v1/auth")
@Tag(name = "auth", description = "Authentication operations")
public interface AuthBlockingClient {

  @PostExchange("/login")
  @Operation(summary = "Logs user in", description = "Returns auth tokens")
  AuthResponse login(@Valid @RequestBody LoginRequest request);

  @PostExchange("/refresh")
  @Operation(summary = "Refresh auth tokens", description = "Returns auth tokens")
  AuthResponse refresh(@Valid @RequestBody RefreshRequest request);

  @GetExchange("/{clientRegistrationId}/redirect")
  @ResponseStatus(HttpStatus.FOUND)
  @Operation(
      summary = "Redirect to OAuth 2.0 authorization URL",
      description = "Redirects to OAuth 2.0 authorization URL")
  ResponseEntity<Void> oauth2Redirect(
      @PathVariable("clientRegistrationId")
          @Parameter(description = "Client registration ID", example = "google")
          @NotBlank
          String clientRegistrationId,
      @RequestParam("redirectUri")
          @Parameter(description = "Redirect URL", example = "http://localhost/oauth2/code/google")
          @URL
          String redirectUri);

  @PostExchange("/{clientRegistrationId}/redirect")
  @Operation(summary = "OAuth 2.0 login", description = "Returns auth tokens")
  AuthResponse oauth2Login(
      @PathVariable("clientRegistrationId")
          @Parameter(description = "Client registration ID", example = "google")
          @NotBlank
          String clientRegistrationId,
      @RequestBody @Valid Oauth2LoginRequest request);
}
