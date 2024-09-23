package com.example.common.security;

import com.example.common.error.exception.InternalServerErrorException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;

/** Security utilities. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SecurityUtils {

  /**
   * Checks if the logged-in user has the role.
   *
   * @param context security context
   * @param role role name
   * @return {@code true} is the user has the role, otherwise {@code false}
   */
  public static boolean hasRole(SecurityContext context, String role) {
    var auth = context.getAuthentication();
    return auth != null
        && auth.isAuthenticated()
        && auth.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(role::equals);
  }

  /**
   * Returns UUID of the logged-in user.
   *
   * @param context security context
   * @throws AccessDeniedException if the user is not logged-in
   */
  public static UUID getUserUUID(SecurityContext context) throws AccessDeniedException {
    var auth = context.getAuthentication();
    if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof ApiUser apiUser) {
      return apiUser.uuid();
    } else {
      throw new AccessDeniedException("Access Denied");
    }
  }

  /**
   * Creates a secret key.
   *
   * @param secret key secret
   * @return created secret key
   * @throws InternalServerErrorException if the secret key creation failed
   */
  public static SecretKey createSecretKey(String secret) throws InternalServerErrorException {
    try {
      return new SecretKeySpec(
          MessageDigest.getInstance("SHA-256").digest(secret.getBytes(StandardCharsets.UTF_8)),
          "HmacSHA256");
    } catch (NoSuchAlgorithmException ex) {
      throw new InternalServerErrorException("Failed to get MessageDigest instance", ex);
    }
  }
}
