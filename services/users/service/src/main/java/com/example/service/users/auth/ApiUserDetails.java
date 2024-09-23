package com.example.service.users.auth;

import com.example.common.security.ApiUser;
import java.util.Collection;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

@Value
@EqualsAndHashCode(callSuper = true)
public class ApiUserDetails extends User implements ApiUser {
  UUID uuid;
  String name;

  public ApiUserDetails(
      UUID uuid,
      String username,
      String password,
      boolean enabled,
      boolean accountNonExpired,
      boolean credentialsNonExpired,
      boolean accountNonLocked,
      Collection<? extends GrantedAuthority> authorities) {
    super(
        username,
        password,
        enabled,
        accountNonExpired,
        credentialsNonExpired,
        accountNonLocked,
        authorities);

    this.uuid = uuid;
    this.name = String.valueOf(uuid);
  }

  @Override
  public UUID uuid() {
    return uuid;
  }
}
