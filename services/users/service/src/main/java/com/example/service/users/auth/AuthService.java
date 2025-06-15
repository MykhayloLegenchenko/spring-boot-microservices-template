package com.example.service.users.auth;

import com.example.autoconfigure.web.servlet.security.ServletJwtAuthenticationConverter;
import com.example.client.users.auth.dto.AuthResponse;
import com.example.client.users.auth.dto.LoginRequest;
import com.example.client.users.auth.dto.RefreshRequest;
import com.example.common.error.exception.InternalServerErrorException;
import com.example.common.security.ApiUser;
import com.example.common.security.SecurityUtils;
import com.example.common.security.jwt.JwtUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.stereotype.Service;

@Service
@EnableConfigurationProperties(JwtProperties.class)
public final class AuthService {
  private final JwtProperties jwtProperties;
  private final AuthenticationConfiguration authenticationConfiguration;
  private final JwtEncoder accessEncoder;
  private final JwtEncoder refreshEncoder;
  private final JwtAuthenticationProvider refreshAuthProvider;
  private final UserDetailsServiceImpl detailsService;

  public AuthService(
      JwtProperties jwtProperties,
      AuthenticationConfiguration authenticationConfiguration,
      UserDetailsServiceImpl detailsService,
      ServletJwtAuthenticationConverter jwtConverter) {

    this.jwtProperties = jwtProperties;
    this.authenticationConfiguration = authenticationConfiguration;

    accessEncoder =
        JwtUtils.crateEncoder(SecurityUtils.createSecretKey(jwtProperties.accessToken().secret()));

    var refreshSecretKey = SecurityUtils.createSecretKey(jwtProperties.refreshToken().secret());
    refreshEncoder = JwtUtils.crateEncoder(refreshSecretKey);

    var refreshDecoder = NimbusJwtDecoder.withSecretKey(refreshSecretKey).build();
    refreshAuthProvider = new JwtAuthenticationProvider(refreshDecoder);
    refreshAuthProvider.setJwtAuthenticationConverter(jwtConverter);

    this.detailsService = detailsService;
  }

  public AuthResponse login(LoginRequest request) {
    AuthenticationManager authenticationManager;
    try {
      authenticationManager = authenticationConfiguration.getAuthenticationManager();
    } catch (Exception ex) {
      throw new InternalServerErrorException("Failed to get authentication manager instance", ex);
    }

    var token =
        UsernamePasswordAuthenticationToken.unauthenticated(request.email(), request.password());
    return createAuthResult(
        (ApiUserDetails) authenticationManager.authenticate(token).getPrincipal());
  }

  public AuthResponse refresh(RefreshRequest request) {
    var authentication =
        refreshAuthProvider.authenticate(
            new BearerTokenAuthenticationToken(request.refreshToken()));

    if (authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .map(String::toUpperCase)
        .noneMatch("ROLE_REFRESH"::equals)) {
      throw new BadCredentialsException("Invalid token scope");
    }

    var user = detailsService.loadUserByUUID(((ApiUser) authentication.getPrincipal()).uuid());
    if (!user.isEnabled()) {
      throw new DisabledException("User is not found.");
    }

    return createAuthResult(user);
  }

  private AuthResponse createAuthResult(ApiUserDetails user) {
    var issuer = jwtProperties.issuer();
    var scope = JwtUtils.authoritiesToScope(user.getAuthorities());
    var subject = user.uuid().toString();
    var accessToken =
        JwtUtils.creatToken(
            accessEncoder, issuer, subject, scope, jwtProperties.accessToken().ttl());
    var refreshToken =
        JwtUtils.creatToken(
            refreshEncoder, issuer, subject, "refresh", jwtProperties.refreshToken().ttl());

    return new AuthResponse(
        user.uuid(), accessToken, "Bearer", jwtProperties.accessToken().ttl(), refreshToken, scope);
  }
}
