package com.example.service.users.auth;

import static org.assertj.core.api.Assertions.*;

import com.example.autoconfigure.web.servlet.security.ServletJwtAuthenticationConverter;
import com.example.client.users.auth.AuthBlockingClient;
import com.example.client.users.auth.dto.AuthResponse;
import com.example.client.users.auth.dto.LoginRequest;
import com.example.client.users.auth.dto.RefreshRequest;
import com.example.common.web.client.blocking.BlockingClientFactory;
import com.example.service.users.UsersServiceApplication;
import java.util.Set;
import java.util.stream.Collectors;
import org.jspecify.annotations.NullUnmarked;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = UsersServiceApplication.class)
@NullUnmarked
class AuthApiTests {
  private static AuthBlockingClient client;

  @Autowired private JwtDecoder jwtDecoder;
  @Autowired private ServletJwtAuthenticationConverter jwtConverter;

  @BeforeAll
  static void init(@LocalServerPort int port) {
    client =
        BlockingClientFactory.create(RestClient.builder().baseUrl("http://localhost:" + port))
            .createClient(AuthBlockingClient.class);
  }

  @Test
  void apiCalls() {
    var loginResponse = client.login(new LoginRequest("admin@example.com", "password"));
    assertCorrectAuthResponse(loginResponse);

    assertUnauthorized(() -> client.refresh(new RefreshRequest(loginResponse.accessToken())));

    var refreshResponse = client.refresh(new RefreshRequest(loginResponse.refreshToken()));
    assertCorrectAuthResponse(refreshResponse);
  }

  private void assertCorrectAuthResponse(AuthResponse authResponse) {
    assertThat(authResponse).isNotNull();
    assertThat(authResponse.uuid()).isNotNull();

    var authProvider = new JwtAuthenticationProvider(jwtDecoder);
    authProvider.setJwtAuthenticationConverter(jwtConverter);

    var authentication =
        authProvider.authenticate(new BearerTokenAuthenticationToken(authResponse.accessToken()));
    assertThat(authentication.isAuthenticated()).isTrue();

    var authorities =
        authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toSet());
    assertThat(authorities).containsAll(Set.of("ROLE_USER", "ROLE_ADMIN", "ROLE_SUPER"));

    var token = new BearerTokenAuthenticationToken(authResponse.refreshToken());
    assertThatExceptionOfType(InvalidBearerTokenException.class)
        .isThrownBy(() -> authProvider.authenticate(token));
  }

  private void assertUnauthorized(Runnable caller) {
    assertThatExceptionOfType(HttpClientErrorException.Unauthorized.class).isThrownBy(caller::run);
  }
}
