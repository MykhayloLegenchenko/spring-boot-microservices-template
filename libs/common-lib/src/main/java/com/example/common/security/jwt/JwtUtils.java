package com.example.common.security.jwt;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.JwsAlgorithms;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JwtUtils {
  public static String creatToken(
      JwtEncoder encoder, String issuer, String subject, String scope, int ttl) {
    var headers = JwsHeader.with(() -> JwsAlgorithms.HS256).build();
    var claims =
        JwtClaimsSet.builder()
            .issuer(issuer)
            .subject(subject)
            .claim("scope", scope)
            .expiresAt(Instant.now().plus(ttl, ChronoUnit.SECONDS))
            .build();

    return encoder.encode(JwtEncoderParameters.from(headers, claims)).getTokenValue();
  }

  public static JwtEncoder crateEncoder(SecretKey secretKey) {
    return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));
  }

  public static String authoritiesToScope(Collection<? extends GrantedAuthority> authorities) {
    return authorities.stream()
        .map(GrantedAuthority::getAuthority)
        .map(JwtUtils::roleToScope)
        .collect(Collectors.joining(" "));
  }

  public static String rolesToScope(Collection<String> roles) {
    return roles.stream().map(JwtUtils::roleToScope).collect(Collectors.joining(" "));
  }

  private static String roleToScope(String role) {
    var scope = role.startsWith("ROLE_") ? role.substring(5) : role;
    return scope.toLowerCase();
  }
}
