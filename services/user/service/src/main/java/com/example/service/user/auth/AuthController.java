package com.example.service.user.auth;

import com.example.client.user.auth.AuthBlockingClient;
import com.example.client.user.auth.AuthClientRuntimeHints;
import com.example.client.user.auth.dto.AuthResponse;
import com.example.client.user.auth.dto.LoginRequest;
import com.example.client.user.auth.dto.Oauth2LoginRequest;
import com.example.client.user.auth.dto.RefreshRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RestController
@ImportRuntimeHints(AuthClientRuntimeHints.class)
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

  @Override
  public ResponseEntity<Void> oauth2Redirect(String clientRegistrationId, String redirectUri) {
    var requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    Assert.notNull(requestAttributes, () -> "No ServletRequestAttributes found");

    var authorizationRequestUrl =
        authService.authorizationRequestUrl(
            clientRegistrationId, redirectUri, requestAttributes.getRequest());
    return ResponseEntity.status(HttpStatus.FOUND)
        .header(HttpHeaders.LOCATION, authorizationRequestUrl)
        .build();
  }

  @Override
  public AuthResponse oauth2Login(String clientRegistrationId, Oauth2LoginRequest request) {
    return authService.oauth2Login(clientRegistrationId, request);
  }
}
