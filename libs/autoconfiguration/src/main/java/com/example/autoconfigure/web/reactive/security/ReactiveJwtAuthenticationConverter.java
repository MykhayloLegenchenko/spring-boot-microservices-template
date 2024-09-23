package com.example.autoconfigure.web.reactive.security;

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
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtGrantedAuthoritiesConverterAdapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive converter fom {@link Jwt} to {@link AbstractAuthenticationToken}.
 *
 * <p>Throws {@link BadCredentialsException} if conversion fails.
 */
public class ReactiveJwtAuthenticationConverter
    implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {
  private final Converter<Jwt, Flux<GrantedAuthority>> jwtGrantedAuthoritiesConverter =
      new ReactiveJwtGrantedAuthoritiesConverterAdapter(
          SecurityConfigurationUtils.getJwtGrantedAuthoritiesConverter());

  @Override
  public Mono<AbstractAuthenticationToken> convert(Jwt jwt) {
    var subject = jwt.getClaimAsString(JwtClaimNames.SUB);
    if (subject == null) {
      return Mono.error(new BadCredentialsException("Missing subject"));
    }

    UUID uuid;
    try {
      uuid = UUID.fromString(subject);
    } catch (IllegalArgumentException ex) {
      return Mono.error(new BadCredentialsException("Invalid token subject", ex));
    }

    return Objects.requireNonNull(jwtGrantedAuthoritiesConverter.convert(jwt))
        .map(a -> new SimpleGrantedAuthority(a.getAuthority().toUpperCase()))
        .collectList()
        .map(
            authorities ->
                SecurityConfigurationUtils.createJwtAuthenticationToken(
                    uuid, jwt, authorities, subject));
  }
}
