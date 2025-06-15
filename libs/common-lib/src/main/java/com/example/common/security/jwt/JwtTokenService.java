package com.example.common.security.jwt;

import com.example.common.security.SecurityUtils;
import jakarta.validation.constraints.Positive;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.stereotype.Service;

@Service
public final class JwtTokenService {
  private final String issuer;
  private final int ttl;
  private final JwtEncoder jwtEncoder;

  public JwtTokenService(
      @Value("${auth.jwt.issuer}") String issuer,
      @Value("${auth.jwt.access-token.secret}") String secret,
      @Positive @Value("${auth.jwt.access-token.ttl}") int ttl) {

    this.issuer = issuer;
    this.ttl = ttl;
    jwtEncoder = JwtUtils.crateEncoder(SecurityUtils.createSecretKey(secret));
  }

  public String createToken(UUID userUuid, String... roles) {
    return createToken(userUuid, List.of(roles));
  }

  public String createToken(UUID userUuid, Collection<String> roles) {
    return JwtUtils.creatToken(
        jwtEncoder, issuer, userUuid.toString(), JwtUtils.rolesToScope(roles), ttl);
  }
}
