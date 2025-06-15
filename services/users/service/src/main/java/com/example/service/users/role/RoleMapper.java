package com.example.service.users.role;

import com.example.client.users.role.dto.RoleDto;
import com.example.service.users.role.model.RoleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper
public interface RoleMapper {
  void update(@MappingTarget RoleEntity entity, RoleDto src);

  RoleDto toRoleDto(RoleEntity entity);
}
