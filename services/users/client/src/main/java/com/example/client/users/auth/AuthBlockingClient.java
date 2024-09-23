package com.example.client.users.auth;

import com.example.annotation.annotation.ClientInterface;
import com.example.client.users.auth.dto.AuthResponse;
import com.example.client.users.auth.dto.LoginRequest;
import com.example.client.users.auth.dto.RefreshRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@ClientInterface
@HttpExchange(url = "/api/v1/auth")
@Tag(name = "auth", description = "Authentication operations")
public interface AuthBlockingClient {

  @PostExchange("/login")
  @Operation(summary = "Log user in", description = "Returns auth tokens")
  AuthResponse login(@Valid @RequestBody LoginRequest request);

  @PostExchange("/refresh")
  @Operation(summary = "Refresh auth tokens", description = "Returns auth tokens")
  AuthResponse refresh(@Valid @RequestBody RefreshRequest request);
}
