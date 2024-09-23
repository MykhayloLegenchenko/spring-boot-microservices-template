package com.example.service.users.user;

import static com.example.service.users.user.UserService.Access.*;

import com.example.client.users.user.UserBlockingClient;
import com.example.client.users.user.dto.CountUsersRequest;
import com.example.client.users.user.dto.FindUsersRequest;
import com.example.client.users.user.dto.RegisterUserRequest;
import com.example.client.users.user.dto.UpdateUserRequest;
import com.example.client.users.user.dto.UserDto;
import com.example.client.users.user.dto.UserDtoEx;
import com.example.common.dto.CountResult;
import com.example.common.error.exception.BadRequestException;
import com.example.common.security.SecurityUtils;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class UserController implements UserBlockingClient {
  private final UserService userService;

  @Override
  @PreAuthorize("permitAll()")
  public UserDto registerUser(RegisterUserRequest request) {
    return userService.registerUser(request);
  }

  @Override
  @PreAuthorize("isAuthenticated()")
  public UserDto getUser() {
    return (UserDto) userService.getUser(selfUuid(), USER);
  }

  @Override
  public UserDtoEx getUser(UUID uuid) {
    return (UserDtoEx) userService.getUser(uuid, ADMIN);
  }

  @Override
  public List<UserDtoEx> findUsers(FindUsersRequest request) {
    return userService.findUsers(request);
  }

  @Override
  public CountResult countUsers(CountUsersRequest request) {
    return userService.countUsers(request);
  }

  @Override
  @PreAuthorize("isAuthenticated()")
  public UserDto updateUser(UpdateUserRequest request) {
    return (UserDto) userService.updateUser(selfUuid(), request, USER);
  }

  @Override
  public UserDtoEx updateUser(UUID uuid, UpdateUserRequest request) {
    return (UserDtoEx) userService.updateUser(uuid, request, ADMIN);
  }

  @Override
  public void enableUser(UUID uuid) {
    protectYourself(uuid, "enable");
    userService.setUserEnabled(uuid, true);
  }

  @Override
  public void disableUser(UUID uuid) {
    protectYourself(uuid, "disable");
    userService.setUserEnabled(uuid, false);
  }

  @Override
  public void deleteUser(UUID uuid) {
    protectYourself(uuid, "delete");
    userService.deleteUser(uuid);
  }

  @Override
  @PreAuthorize("isAuthenticated()")
  public Set<String> getRoles() {
    return userService.getRoles(selfUuid(), USER);
  }

  @Override
  public Set<String> getRoles(UUID uuid) {
    return userService.getRoles(uuid, ADMIN);
  }

  @Override
  public void setRoles(UUID uuid, Set<String> roles) {
    userService.setRoles(uuid, roles, isSuper() ? SUPER : ADMIN);
  }

  private static UUID selfUuid() {
    return SecurityUtils.getUserUUID(SecurityContextHolder.getContext());
  }

  private static boolean isSuper() {
    return SecurityUtils.hasRole(SecurityContextHolder.getContext(), "ROLE_SUPER");
  }

  private static void protectYourself(UUID uuid, String verb) {
    if (uuid.equals(selfUuid())) {
      throw new BadRequestException("You cannot " + verb + " yourself");
    }
  }
}
