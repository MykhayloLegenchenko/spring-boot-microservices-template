package com.example.common.security.configuration;

import com.example.common.security.ApiUser;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.web.cors.CorsConfiguration;

/**
 * Security configuration utilities.
 *
 * <p>Intended for use by Spring security configurators.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SecurityConfigurationUtils {

  /** Returns the default CORS configuration. */
  public static CorsConfiguration defaultCorsConfiguration() {
    var config = new CorsConfiguration();
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "HEAD", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedOrigins(List.of("*"));

    var headers =
        List.of(
            "Authorization",
            "Access-Control-Request-Headers",
            "Access-Control-Request-Method",
            "Content-Type");
    config.setAllowedHeaders(headers);
    config.setExposedHeaders(headers);

    config.setMaxAge(3600L);

    return config;
  }

  /** Returns an instance of {@link JwtGrantedAuthoritiesConverter}. */
  public static JwtGrantedAuthoritiesConverter getJwtGrantedAuthoritiesConverter() {
    var converter = new JwtGrantedAuthoritiesConverter();
    converter.setAuthorityPrefix("ROLE_");

    return converter;
  }

  /** Creates a new {@link AbstractAuthenticationToken}. */
  public static AbstractAuthenticationToken createJwtAuthenticationToken(
      UUID uuid, Jwt jwt, Collection<? extends GrantedAuthority> authorities, String name) {
    var token = new JwtAuthenticationToken(jwt, authorities, name);
    return new JwtAuthToken(uuid, token);
  }

  @Value
  @EqualsAndHashCode(callSuper = true)
  private static class JwtAuthToken extends AbstractAuthenticationToken {
    Object principal;
    Object credentials;

    public JwtAuthToken(UUID uuid, JwtAuthenticationToken token) {
      super(token.getAuthorities());
      setAuthenticated(true);

      this.principal = ApiUser.create(uuid);
      this.credentials = token.getCredentials();
    }
  }
}
