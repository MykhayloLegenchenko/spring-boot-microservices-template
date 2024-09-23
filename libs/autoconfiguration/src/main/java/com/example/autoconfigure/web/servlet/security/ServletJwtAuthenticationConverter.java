package com.example.autoconfigure.web.servlet.security;

import com.example.common.security.configuration.SecurityConfigurationUtils;
import java.util.Objects;
import java.util.UUID;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

/**
 * Converter fom {@link Jwt} to {@link AbstractAuthenticationToken}.
 *
 * <p>Throws {@link BadCredentialsException} if conversion fails.
 */
public class ServletJwtAuthenticationConverter
    implements Converter<Jwt, AbstractAuthenticationToken> {

  private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter =
      SecurityConfigurationUtils.getJwtGrantedAuthoritiesConverter();

  @Override
  public AbstractAuthenticationToken convert(Jwt jwt) {
    var subject = jwt.getClaimAsString(JwtClaimNames.SUB);
    if (subject == null) {
      throw new BadCredentialsException("Missing subject");
    }

    UUID uuid;
    try {
      uuid = UUID.fromString(subject);
    } catch (IllegalArgumentException ex) {
      throw new BadCredentialsException("Invalid token subject", ex);
    }

    var authorities =
        Objects.requireNonNull(jwtGrantedAuthoritiesConverter.convert(jwt)).stream()
            .map(GrantedAuthority::getAuthority)
            .map(String::toUpperCase)
            .map(SimpleGrantedAuthority::new)
            .toList();

    return SecurityConfigurationUtils.createJwtAuthenticationToken(uuid, jwt, authorities, subject);
  }
}
