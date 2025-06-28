package com.example.service.users.auth;

import com.example.autoconfigure.web.servlet.security.ServletJwtAuthenticationConverter;
import com.example.client.users.auth.dto.AuthResponse;
import com.example.client.users.auth.dto.LoginRequest;
import com.example.client.users.auth.dto.Oauth2LoginRequest;
import com.example.client.users.auth.dto.RefreshRequest;
import com.example.client.users.user.dto.RegisterUserRequest;
import com.example.common.error.exception.BadRequestException;
import com.example.common.error.exception.InternalServerErrorException;
import com.example.common.security.ApiUser;
import com.example.common.security.SecurityUtils;
import com.example.common.security.jwt.JwtUtils;
import com.example.service.users.user.UserRepository;
import com.example.service.users.user.UserService;
import com.example.service.users.user.model.UserEntity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.util.Objects;
import org.apache.commons.lang3.RandomStringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.RestClientAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.endpoint.*;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.util.ServletRequestPathUtils;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@EnableConfigurationProperties(JwtProperties.class)
public final class AuthService {
  private final JwtProperties jwtProperties;
  private final AuthenticationConfiguration authenticationConfiguration;
  private final JwtEncoder accessEncoder;
  private final JwtEncoder refreshEncoder;
  private final JwtAuthenticationProvider refreshAuthProvider;
  private final UserDetailsServiceImpl detailsService;
  private final ClientRegistrationRepository clientRegistrationRepository;
  private final DefaultOAuth2AuthorizationRequestResolver authRequestResolver;
  private final RestClientAuthorizationCodeTokenResponseClient authCodeTokenResponseClient;
  private final OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService;
  private final UserRepository userRepository;
  private final UserService userService;

  public AuthService(
      JwtProperties jwtProperties,
      AuthenticationConfiguration authenticationConfiguration,
      UserDetailsServiceImpl detailsService,
      ServletJwtAuthenticationConverter jwtConverter,
      ClientRegistrationRepository clientRegistrationRepository,
      UserRepository userRepository,
      UserService userService) {

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

    this.clientRegistrationRepository = clientRegistrationRepository;
    authRequestResolver =
        new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, "/oauth2");

    authCodeTokenResponseClient = new RestClientAuthorizationCodeTokenResponseClient();
    oAuth2UserService = new DefaultOAuth2UserService();

    this.userRepository = userRepository;
    this.userService = userService;
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

  public String authorizationRequestUrl(
      String clientRegistrationId, String redirectUri, HttpServletRequest request) {
    var requestWrapper =
        new HttpServletRequestWrapper(request) {
          @Override
          @Nullable
          public Object getAttribute(String name) {
            if (ServletRequestPathUtils.PATH_ATTRIBUTE.equals(name)) {
              return null;
            }

            return super.getAttribute(name);
          }

          @Override
          public String getRequestURI() {
            return "/oauth2/" + clientRegistrationId;
          }
        };

    var authRequest = authRequestResolver.resolve(requestWrapper);
    if (authRequest == null) {
      throw new BadRequestException("Bad registration ID: " + clientRegistrationId);
    }

    return UriComponentsBuilder.fromUriString(authRequest.getAuthorizationRequestUri())
        .replaceQueryParam(OAuth2ParameterNames.REDIRECT_URI, redirectUri)
        .build()
        .toString();
  }

  public AuthResponse oauth2Login(String clientRegistrationId, Oauth2LoginRequest request) {
    var clientRegistration =
        clientRegistrationRepository.findByRegistrationId(clientRegistrationId);
    if (clientRegistration == null) {
      throw new BadRequestException("Invalid client registration ID: " + clientRegistrationId);
    }

    var tokenResponse = getOAuth2AccessTokenResponse(clientRegistration, request);
    var oAuth2User =
        oAuth2UserService.loadUser(
            new OAuth2UserRequest(clientRegistration, tokenResponse.getAccessToken()));

    var subjectId = oAuth2User.<String>getAttribute("sub");
    Assert.notNull(subjectId, "Invalid subject");

    var user =
        userRepository
            .fetchOne(UserRepository.Spec.withRoles(UserRepository.Spec.bySubjectId(subjectId)))
            .orElseGet(() -> registerOAuth2User(clientRegistrationId, subjectId, oAuth2User));

    if (!Objects.equals(clientRegistrationId, user.getClientRegistrationId())) {
      throw new BadRequestException("Incorrect client registration ID");
    }

    return createAuthResult(UserDetailsServiceImpl.toAApiUser(user));
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

  private OAuth2AccessTokenResponse getOAuth2AccessTokenResponse(
      ClientRegistration clientRegistration, Oauth2LoginRequest request) {
    var exchange =
        new OAuth2AuthorizationExchange(
            OAuth2AuthorizationRequest.authorizationCode()
                .clientId(clientRegistration.getClientId())
                .authorizationUri(clientRegistration.getProviderDetails().getAuthorizationUri())
                .redirectUri(request.redirectUri())
                .build(),
            OAuth2AuthorizationResponse.success(request.code())
                .state(request.state())
                .redirectUri(request.redirectUri())
                .build());
    try {
      return authCodeTokenResponseClient.getTokenResponse(
          new OAuth2AuthorizationCodeGrantRequest(clientRegistration, exchange));
    } catch (OAuth2AuthorizationException ex) {
      throw new BadCredentialsException(ex.getMessage(), ex);
    }
  }

  private UserEntity registerOAuth2User(
      String clientRegistrationId, String subjectId, OAuth2User oAuth2User) {

    var email = oAuth2User.<String>getAttribute("email");
    Assert.notNull(email, "Invalid email");

    var firstName = oAuth2User.<String>getAttribute("given_name");
    if (firstName == null) {
      firstName = "User";
    }

    var registerUserRequest =
        new RegisterUserRequest(
            email,
            firstName,
            oAuth2User.getAttribute("family_name"),
            RandomStringUtils.secure().nextAlphabetic(8));
    return userService.registerUserEntity(clientRegistrationId, subjectId, registerUserRequest);
  }
}
