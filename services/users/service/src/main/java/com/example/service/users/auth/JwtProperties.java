package com.example.service.users.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("auth.jwt")
@Validated
public record JwtProperties(
    @URL String issuer,
    @NotNull @Valid EncoderProperties accessToken,
    @NotNull @Valid EncoderProperties refreshToken) {

  public record EncoderProperties(@NotBlank String secret, @Positive int ttl) {}
}
