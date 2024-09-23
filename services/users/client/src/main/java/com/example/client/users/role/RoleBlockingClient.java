package com.example.client.users.role;

import com.example.annotation.annotation.ClientInterface;
import com.example.client.users.role.dto.GetAllRolesRequest;
import com.example.client.users.role.dto.RoleDto;
import com.example.common.web.bind.annotation.RequestParamObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
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
@HttpExchange(url = "/api/v1/roles")
@Tag(name = "role", description = "Operations about roles")
@SecurityRequirement(name = "default", scopes = "admin")
public interface RoleBlockingClient {

  @PostExchange
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create new role", description = "Returns created role")
  RoleDto createRole(@Valid @RequestBody RoleDto request);

  @GetExchange
  @Operation(summary = "Get all roles", description = "Returns all existing roles")
  List<String> getAllRoles(@Valid @RequestParamObject GetAllRolesRequest request);

  @PutExchange("/{name}")
  @Operation(summary = "Update existing role", description = "Returns updated role")
  RoleDto updateRole(
      @PathVariable("name") @Parameter(description = "Role name") String name,
      @Valid @RequestBody RoleDto request);

  @DeleteExchange("/{name}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete role", description = "Deletes role by name")
  void deleteRole(@PathVariable("name") @Parameter(description = "Role name") String name);
}
