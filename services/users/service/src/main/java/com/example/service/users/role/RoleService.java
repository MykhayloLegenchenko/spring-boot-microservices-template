package com.example.service.users.role;

import static com.example.service.users.role.RoleRepository.PROTECTED_NAMES;
import static com.example.service.users.role.RoleRepository.RESERVED_NAMES;
import static com.example.service.users.role.RoleRepository.Spec.*;

import com.example.client.users.role.dto.GetAllRolesRequest;
import com.example.client.users.role.dto.RoleDto;
import com.example.common.data.DataUtils;
import com.example.common.data.jpa.JpaUtils;
import com.example.common.error.exception.BadRequestException;
import com.example.common.error.exception.NotFoundException;
import com.example.service.users.role.model.RoleEntity;
import java.text.MessageFormat;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RoleService {
  private static final Set<String> SORT_PROPERTIES = Set.of("id", "name");

  private final RoleMapper roleMapper;
  private final RoleRepository roleRepository;

  @Transactional
  public RoleDto createRole(RoleDto request) {
    return save(new RoleEntity(), request);
  }

  public List<String> getAllRoles(GetAllRolesRequest request) {
    return roleRepository.findAll(DataUtils.parseSort(request.sort(), SORT_PROPERTIES)).stream()
        .map(RoleEntity::getName)
        .toList();
  }

  @Transactional
  public RoleDto updateRole(String name, RoleDto request) {
    checkRoleName(name);

    return save(
        roleRepository
            .findOne(byName(name))
            .orElseThrow(() -> new NotFoundException("Role is not found.")),
        request);
  }

  @Transactional
  public void deleteRole(String name) {
    checkRoleName(name);

    if (roleRepository.delete(byName(name)) == 0) {
      throw new NotFoundException("Role is not found.");
    }
  }

  private RoleDto save(RoleEntity role, RoleDto request) {
    checkRoleName(request.name());
    roleMapper.update(role, request);

    try {
      if (role.getId() == null) {
        roleRepository.persistAndFlush(role);
      } else {
        roleRepository.flush();
      }
    } catch (DataIntegrityViolationException ex) {
      JpaUtils.processConstraintViolation(
          ex,
          "role.role_name_uk",
          () -> MessageFormat.format("Role with name \"{0}\" already exists.", role.getName()));
    }

    return roleMapper.toRoleDto(role);
  }

  private static void checkRoleName(String name) {
    if (PROTECTED_NAMES.contains(name)) {
      throw new BadRequestException(MessageFormat.format("Role name {0} is protected", name));
    }

    if (RESERVED_NAMES.contains(name)) {
      throw new BadRequestException(MessageFormat.format("Role name {0} is reserved", name));
    }
  }
}
