package com.example.service.users.role;

import com.example.client.users.role.RoleBlockingClient;
import com.example.client.users.role.dto.GetAllRolesRequest;
import com.example.client.users.role.dto.RoleDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
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
