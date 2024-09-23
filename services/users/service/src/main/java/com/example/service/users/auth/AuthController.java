package com.example.service.users.auth;

import com.example.client.users.auth.AuthBlockingClient;
import com.example.client.users.auth.dto.AuthResponse;
import com.example.client.users.auth.dto.LoginRequest;
import com.example.client.users.auth.dto.RefreshRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController implements AuthBlockingClient {
  private final AuthService authService;

  @Override
  public AuthResponse login(LoginRequest request) {
    return authService.login(request);
  }

  @Override
  public AuthResponse refresh(RefreshRequest request) {
    return authService.refresh(request);
  }
}
