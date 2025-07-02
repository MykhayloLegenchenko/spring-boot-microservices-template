package com.example.service.user.auth;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.example.autoconfigure.web.servlet.security.ServletJwtAuthenticationConverter;
import com.example.client.user.auth.AuthBlockingClient;
import com.example.client.user.auth.dto.AuthResponse;
import com.example.client.user.auth.dto.LoginRequest;
import com.example.client.user.auth.dto.Oauth2LoginRequest;
import com.example.client.user.auth.dto.RefreshRequest;
import com.example.common.web.client.blocking.BlockingClientFactory;
import com.example.service.user.configuration.AbstractIntegrationTest;
import com.example.service.user.user.UserRepository;
import com.example.test.web.client.blocking.MockRestClientService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.jspecify.annotations.NullUnmarked;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.endpoint.RestClientAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BindException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@NullUnmarked
class AuthApiTests extends AbstractIntegrationTest {
  private static final Set<String> ROLES_SUPER = Set.of("ROLE_USER", "ROLE_ADMIN", "ROLE_SUPER");
  private static final Set<String> ROLES_USER = Set.of("ROLE_USER");

  private static MockRestClientService mockService;
  private static AuthBlockingClient client;
  private static AuthBlockingClient noRedirectClient;

  @Autowired private Environment environment;
  @Autowired private JwtDecoder jwtDecoder;
  @Autowired private ServletJwtAuthenticationConverter jwtConverter;
  @Autowired private UserRepository userRepository;

  @BeforeAll
  static void setUpAll(
      @LocalServerPort int port,
      @Autowired AuthService authService,
      @Autowired MockRestClientService mockService)
      throws BindException, IOException {

    AuthApiTests.mockService = mockService;
    setupAuthService(authService);

    var clientBuilder = RestClient.builder().baseUrl("http://localhost:" + port);

    client = BlockingClientFactory.create(clientBuilder).createClient(AuthBlockingClient.class);
    noRedirectClient =
        BlockingClientFactory.create(
                clientBuilder.requestFactory(
                    new HttpComponentsClientHttpRequestFactory(
                        HttpClients.custom().disableRedirectHandling().build())))
            .createClient(AuthBlockingClient.class);
  }

  @Test
  void testLoginAndRefresh() {
    var loginResponse = client.login(new LoginRequest("admin@example.com", "password"));
    assertCorrectAuthResponse(loginResponse, ROLES_SUPER);

    assertUnauthorized(() -> client.refresh(new RefreshRequest(loginResponse.accessToken())));

    var refreshResponse = client.refresh(new RefreshRequest(loginResponse.refreshToken()));
    assertCorrectAuthResponse(refreshResponse, ROLES_SUPER);
  }

  @Test
  void testOauth2Redirect() {
    var result = noRedirectClient.oauth2Redirect("google", "http://localhost/oauth2/code/google");
    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.FOUND);
    assertNull(result.getBody());

    var locations = result.getHeaders().get("Location");
    assertThat(locations).hasSize(1);

    var redirectUriBuilder = UriComponentsBuilder.fromUriString(locations.getFirst());
    assertThat(redirectUriBuilder.build().getQueryParams().getFirst("state")).isNotEmpty();
    redirectUriBuilder.replaceQueryParam("state");

    assertThat(redirectUriBuilder.build().getQueryParams().getFirst("nonce")).isNotEmpty();
    redirectUriBuilder.replaceQueryParam("nonce");

    assertThat(redirectUriBuilder.build().toString())
        .isEqualTo(
            MessageFormat.format(
                "https://accounts.google.com/o/oauth2/v2/auth?response_type=code&client_id={0}&scope=openid%20profile%20email&redirect_uri=http://localhost/oauth2/code/google",
                getOAuth2Property("clientId")));
  }

  @Test
  void testOauth2Login() {
    mockService.addPlaceholder("mock.header.authorization", googleTokenAuthHeader());

    var oauth2LoginRequest =
        new Oauth2LoginRequest(
            "http://localhost/oauth2/code/google", "mock-auth-code", "mock-auth-state");

    // Test registration
    var response = client.oauth2Login("google", oauth2LoginRequest);
    assertCorrectAuthResponse(response, ROLES_USER);

    // Test user details
    var user = userRepository.findOne(UserRepository.Spec.byUuid(response.uuid())).orElseThrow();
    assertThat(user.getEmail()).isEqualTo("legenchenko@gmail.com");
    assertThat(user.getClientRegistrationId()).isEqualTo("google");
    assertThat(user.getSubjectId()).isEqualTo("108560288596966436424");
    assertThat(user.getEmail()).isEqualTo("legenchenko@gmail.com");
    assertThat(user.getFirstName()).isEqualTo("Mykhaylo");
    assertThat(user.getLastName()).isEqualTo("Legenchenko");

    // Test login
    response = client.oauth2Login("google", oauth2LoginRequest);
    assertCorrectAuthResponse(response, ROLES_USER);

    mockService.removePlaceholder("mock.header.authorization");
  }

  private String getOAuth2Property(String name) {
    return environment.getProperty("spring.security.oauth2.client.registration.google." + name);
  }

  private String googleTokenAuthHeader() {
    var credentials = getOAuth2Property("clientId") + ":" + getOAuth2Property("clientSecret");
    return "Basic "
        + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
  }

  private static void setupAuthService(AuthService authService) throws BindException, IOException {
    mockService.loadFromResources("responses/auth");

    var authCodeTokenResponseClient =
        (RestClientAuthorizationCodeTokenResponseClient)
            ReflectionTestUtils.getField(authService, "authCodeTokenResponseClient");
    assertThat(authCodeTokenResponseClient).isNotNull();

    var restClient =
        (RestClient) ReflectionTestUtils.getField(authCodeTokenResponseClient, "restClient");
    assertThat(restClient).isNotNull();
    authCodeTokenResponseClient.setRestClient(mockService.mock(restClient));

    var oAuth2UserService =
        (DefaultOAuth2UserService) ReflectionTestUtils.getField(authService, "oAuth2UserService");
    assertThat(oAuth2UserService).isNotNull();

    var restOperations =
        (RestTemplate) ReflectionTestUtils.getField(oAuth2UserService, "restOperations");
    assertThat(restOperations).isNotNull();
    restOperations.getInterceptors().addLast(mockService);
  }

  private void assertCorrectAuthResponse(AuthResponse authResponse, Set<String> expectedRoles) {
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
    assertThat(authorities).isEqualTo(expectedRoles);

    var token = new BearerTokenAuthenticationToken(authResponse.refreshToken());
    assertThatExceptionOfType(InvalidBearerTokenException.class)
        .isThrownBy(() -> authProvider.authenticate(token));
  }

  private void assertUnauthorized(Runnable caller) {
    assertThatExceptionOfType(HttpClientErrorException.Unauthorized.class).isThrownBy(caller::run);
  }
}
