package com.example.service.users.auth;

import static com.example.service.users.user.UserRepository.Spec.*;

import com.example.service.users.role.model.RoleEntity;
import com.example.service.users.user.UserRepository;
import com.example.service.users.user.model.UserEntity;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
  private final UserRepository userRepository;

  @Override
  @Transactional(readOnly = true)
  public ApiUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return userRepository
        .fetchOne(withRoles(byEmail(username).and(byDeletedAt(null))))
        .map(this::toAApiUser)
        .orElseThrow(() -> new UsernameNotFoundException("User is not found."));
  }

  @Transactional(readOnly = true)
  public ApiUserDetails loadUserByUUID(UUID uuid) throws UsernameNotFoundException {
    return userRepository
        .findOne(byUuid(uuid))
        .filter(Predicate.not(UserEntity::isDeleted))
        .map(this::toAApiUser)
        .orElseThrow(() -> new BadCredentialsException("User is not found."));
  }

  private ApiUserDetails toAApiUser(UserEntity user) {
    return new ApiUserDetails(
        user.getUuid(),
        user.getEmail(),
        user.getPassword(),
        user.isEnabled(),
        true,
        true,
        true,
        Stream.concat(
                user.getRoles().stream().map(RoleEntity::getName).map(String::toUpperCase),
                Stream.of("ROLE_USER"))
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toUnmodifiableSet()));
  }
}
