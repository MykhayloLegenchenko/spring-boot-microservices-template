package com.example.autoconfigure.web.servlet.security;

import com.example.common.security.configuration.SecurityConfigurationUtils;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationContext;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.annotation.web.configurers.RequestCacheConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Default implementation class for the {@link ServletSecurityConfigurator}.
 *
 * <p>Performs the default security configuration, extend this class if custom configuration is
 * required.
 */
@RequiredArgsConstructor
public class DefaultServletSecurityConfigurator implements ServletSecurityConfigurator {
  @Nullable private ApplicationContext context;

  /**
   * Configures the given {@link HttpSecurity} object.
   *
   * @implSpec This implementation configures {@code HttpSecurity} using overridable protected class
   *     methods.
   */
  @Override
  public void configure(HttpSecurity http, ApplicationContext context) throws Exception {
    this.context = context;

    http.csrf(this::csrf)
        .cors(this::cors)
        .sessionManagement(this::sessionManagement)
        .logout(this::logout)
        .oauth2ResourceServer(this::oauth2ResourceServer)
        .requestCache(this::requestCache)
        .authorizeHttpRequests(this::authorizeHttpRequests);
  }

  /**
   * Configures HTTP request authorization rules.
   *
   * @implSpec This implementation allow public access to actuator and swagger endpoints, any other
   *     requests require an authenticated user. It also calls {@link
   *     #customAuthorizationRules(AuthorizeHttpRequestsConfigurer.AuthorizationManagerRequestMatcherRegistry)}
   *     for custom configuration.
   */
  protected void authorizeHttpRequests(
      AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry
          authorize) {

    authorize
        .requestMatchers("/actuator/**")
        .permitAll()
        .requestMatchers(
            "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs.yaml", "/v3/api-docs/**")
        .permitAll()
        .requestMatchers("/api/v1/auth/**")
        .permitAll();

    customAuthorizationRules(authorize);

    authorize.anyRequest().authenticated();
  }

  /**
   * Configures custom authorization rules.
   *
   * <p>This method designed for inheritance.
   *
   * @implSpec This implementation does nothing.
   */
  protected void customAuthorizationRules(
      AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry
          authorize) {}

  /**
   * Configures CSRF.
   *
   * @implSpec This implementation disables CSRF.
   */
  protected void csrf(CsrfConfigurer<HttpSecurity> csrf) {
    csrf.disable();
  }

  /**
   * Configures CORS.
   *
   * @implSpec This implementation registers {@link
   *     SecurityConfigurationUtils#defaultCorsConfiguration()} for all requests.
   */
  protected void cors(CorsConfigurer<HttpSecurity> cors) {
    var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", SecurityConfigurationUtils.defaultCorsConfiguration());

    cors.configurationSource(source);
  }

  /**
   * Configures session management.
   *
   * @implSpec This implementation disables session creation.
   */
  protected void sessionManagement(SessionManagementConfigurer<HttpSecurity> session) {
    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
  }

  /**
   * Configures logout.
   *
   * @implSpec This implementation disables logout.
   */
  protected void logout(LogoutConfigurer<HttpSecurity> logout) {
    logout.disable();
  }

  /**
   * Configures the OAuth resource server.
   *
   * @implSpec This implementation configures the JWT decoder and JWT authentication converter using
   *     beans of types {@link JwtDecoder} and {@link ServletJwtAuthenticationConverter} from the
   *     application context.
   */
  protected void oauth2ResourceServer(OAuth2ResourceServerConfigurer<HttpSecurity> oauth2) {
    var jwtDecoder = getBean(JwtDecoder.class);
    var jwtConverter = getBean(ServletJwtAuthenticationConverter.class);

    oauth2.jwt(jwt -> jwt.decoder(jwtDecoder).jwtAuthenticationConverter(jwtConverter));
  }

  /**
   * Configures the request cache.
   *
   * @implSpec This implementation disables the request cache.
   */
  protected void requestCache(RequestCacheConfigurer<HttpSecurity> requestCache) {
    requestCache.disable();
  }

  private <T> T getBean(Class<T> type) {
    return Objects.requireNonNull(context).getBean(type);
  }
}
