package com.example.autoconfigure.web.reactive.security;

import com.example.common.security.configuration.SecurityConfigurationUtils;
import java.util.Objects;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * Default implementation class for the {@link ReactiveSecurityConfigurator}.
 *
 * <p>Performs the default security configuration, extend this class if custom configuration is
 * required.
 */
public class DefaultReactiveSecurityConfigurator implements ReactiveSecurityConfigurator {
  @Nullable private ApplicationContext context;

  /**
   * Configures the given {@link ServerHttpSecurity} object.
   *
   * @implSpec This implementation configures {@code ServerHttpSecurity} using overridable protected
   *     class methods.
   */
  @Override
  public void configure(ServerHttpSecurity http, ApplicationContext context) {
    this.context = context;

    http.csrf(this::csrf)
        .cors(this::cors)
        .securityContextRepository(securityContextRepository())
        .logout(this::logout)
        .oauth2ResourceServer(this::oauth2ResourceServer)
        .requestCache(this::requestCache)
        .authorizeExchange(this::authorizeExchange);
  }

  /**
   * Configures the authorization rules for exchanges.
   *
   * @implSpec This implementation allow public access to actuator and swagger endpoints, any other
   *     requests require an authenticated user. It also calls {@link
   *     #customAuthorizationRules(ServerHttpSecurity.AuthorizeExchangeSpec)} for custom
   *     configuration.
   */
  protected void authorizeExchange(ServerHttpSecurity.AuthorizeExchangeSpec authorize) {
    authorize
        .pathMatchers("/actuator/**")
        .permitAll()
        .pathMatchers(
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/webjars/swagger-ui/**",
            "/v3/api-docs.yaml",
            "/v3/api-docs/**")
        .permitAll();

    customAuthorizationRules(authorize);

    authorize.anyExchange().authenticated();
  }

  /**
   * Configures custom authorization rules.
   *
   * <p>This method is designed for inheritance.
   *
   * @implSpec This implementation does nothing.
   */
  @SuppressWarnings("EmptyMethod")
  protected void customAuthorizationRules(
      @SuppressWarnings("unused") ServerHttpSecurity.AuthorizeExchangeSpec authorize) {}

  /**
   * Configures CSRF.
   *
   * @implSpec This implementation disables CSRF.
   */
  protected void csrf(ServerHttpSecurity.CsrfSpec csrf) {
    csrf.disable();
  }

  /**
   * Configures CORS.
   *
   * @implSpec This implementation registers {@link
   *     SecurityConfigurationUtils#defaultCorsConfiguration()} for all requests.
   */
  protected void cors(ServerHttpSecurity.CorsSpec cors) {
    var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", SecurityConfigurationUtils.defaultCorsConfiguration());

    cors.configurationSource(source);
  }

  /**
   * Returns an instance of {@link ServerSecurityContextRepository} for context repository
   * configuration.
   *
   * @implSpec This implementation returns an {@link NoOpServerSecurityContextRepository} instance.
   */
  protected ServerSecurityContextRepository securityContextRepository() {
    return NoOpServerSecurityContextRepository.getInstance();
  }

  /**
   * Configures logout.
   *
   * @implSpec This implementation disables logout.
   */
  protected void logout(ServerHttpSecurity.LogoutSpec logout) {
    logout.disable();
  }

  /**
   * Configures the OAuth resource server.
   *
   * @implSpec This implementation configures the JWT decoder and JWT authentication converter using
   *     beans of types {@link ReactiveJwtDecoder} and {@link ReactiveJwtAuthenticationConverter}
   *     from the application context.
   */
  protected void oauth2ResourceServer(ServerHttpSecurity.OAuth2ResourceServerSpec oauth2) {
    var jwtDecoder = getBean(ReactiveJwtDecoder.class);
    var jwtConverter = getBean(ReactiveJwtAuthenticationConverter.class);

    oauth2.jwt(jwt -> jwt.jwtDecoder(jwtDecoder).jwtAuthenticationConverter(jwtConverter));
  }

  /**
   * Configures the request cache.
   *
   * @implSpec This implementation disables the request cache.
   */
  protected void requestCache(ServerHttpSecurity.RequestCacheSpec requestCache) {
    requestCache.disable();
  }

  private <T> T getBean(Class<T> type) {
    return Objects.requireNonNull(context).getBean(type);
  }
}
