package com.example.service.user.role;

import com.example.client.user.role.RoleBlockingClient;
import com.example.client.user.role.RoleClientRuntimeHints;
import com.example.client.user.role.dto.GetAllRolesRequest;
import com.example.client.user.role.dto.RoleDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ImportRuntimeHints(RoleClientRuntimeHints.class)
@PreAuthorize("hasRole('ROLE_ADMIN')")
@RequiredArgsConstructor
public class RoleController implements RoleBlockingClient {
  private final RoleService roleService;

  @Override
  public RoleDto createRole(RoleDto request) {
    return roleService.createRole(request);
  }

  @Override
  public List<String> getAllRoles(GetAllRolesRequest request) {
    return roleService.getAllRoles(request);
  }

  @Override
  public RoleDto updateRole(String name, RoleDto request) {
    return roleService.updateRole(name, request);
  }

  @Override
  public void deleteRole(String name) {
    roleService.deleteRole(name);
  }
}
