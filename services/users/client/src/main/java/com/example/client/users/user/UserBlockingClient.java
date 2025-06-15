package com.example.client.users.user;

import com.example.annotation.annotation.ClientInterface;
import com.example.client.users.user.dto.CountUsersRequest;
import com.example.client.users.user.dto.FindUsersRequest;
import com.example.client.users.user.dto.RegisterUserRequest;
import com.example.client.users.user.dto.UpdateUserRequest;
import com.example.client.users.user.dto.UserDto;
import com.example.client.users.user.dto.UserDtoEx;
import com.example.common.dto.CountResult;
import com.example.common.web.bind.annotation.RequestParamObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

@ClientInterface
@HttpExchange(url = "/api/v1/users")
@Tag(name = "user", description = "Operations about users")
public interface UserBlockingClient {

  @PostExchange
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Register new user", description = "Returns registered user")
  UserDto registerUser(@Valid @RequestBody RegisterUserRequest request);

  @GetExchange("/self")
  @Operation(summary = "Get authorized user", description = "Returns authorized user")
  @SecurityRequirement(name = "default")
  UserDto getUser();

  @GetExchange("/{uuid}")
  @Operation(summary = "Get user by UUID", description = "Returns user by UUID")
  @SecurityRequirement(name = "default", scopes = "admin")
  UserDtoEx getUser(@PathVariable("uuid") @Parameter(description = "UUID of the user") UUID uuid);

  @GetExchange
  @Operation(summary = "Finds users", description = "Returns found users")
  @SecurityRequirement(name = "default", scopes = "admin")
  List<UserDtoEx> findUsers(@Valid @RequestParamObject FindUsersRequest request);

  @GetExchange("/count")
  @Operation(summary = "Counts users", description = "Returns user count")
  @SecurityRequirement(name = "default", scopes = "admin")
  CountResult countUsers(@Valid @RequestParamObject CountUsersRequest request);

  @PutExchange("/self")
  @Operation(summary = "Update authorized user", description = "Returns updated user")
  @SecurityRequirement(name = "default")
  UserDto updateUser(@Valid @RequestBody UpdateUserRequest request);

  @PutExchange("/{uuid}")
  @Operation(summary = "Update user by UUID", description = "Returns updated user")
  @SecurityRequirement(name = "default", scopes = "admin")
  UserDtoEx updateUser(
      @PathVariable("uuid") @Parameter(description = "UUID of the user") UUID uuid,
      @Valid @RequestBody UpdateUserRequest request);

  @PutExchange("/{uuid}/enable")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Enable user", description = "Enables user")
  @SecurityRequirement(name = "default", scopes = "admin")
  void enableUser(@PathVariable("uuid") @Parameter(description = "UUID of the user") UUID uuid);

  @PutExchange("/{uuid}/disable")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Disable user", description = "Disables user")
  @SecurityRequirement(name = "default", scopes = "admin")
  void disableUser(@PathVariable("uuid") @Parameter(description = "UUID of the user") UUID uuid);

  @DeleteExchange("/{uuid}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete user by UUID", description = "Deletes user by UUID")
  @SecurityRequirement(name = "default", scopes = "admin")
  void deleteUser(@PathVariable("uuid") @Parameter(description = "UUID of the user") UUID uuid);

  @GetExchange("/self/roles")
  @Operation(summary = "Get authorized user roles", description = "Returns authorized user roles")
  @SecurityRequirement(name = "default")
  Set<String> getRoles();

  @GetExchange("/{uuid}/roles")
  @Operation(summary = "Get user roles by uuid", description = "Returns user roles by UUID")
  @SecurityRequirement(name = "default", scopes = "admin")
  Set<String> getRoles(
      @PathVariable("uuid") @Parameter(description = "UUID of the user") UUID uuid);

  @PutExchange("/{uuid}/roles")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Set user roles by uuid", description = "Sets user roles by UUID")
  @SecurityRequirement(name = "default", scopes = "admin")
  void setRoles(
      @PathVariable("uuid") @Parameter(description = "UUID of the user") UUID uuid,
      @RequestBody @Valid Set<@NotBlank String> roles);
}
