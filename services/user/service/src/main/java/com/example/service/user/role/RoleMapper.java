package com.example.service.user.role;

import com.example.client.user.role.dto.RoleDto;
import com.example.service.user.role.model.RoleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper
public interface RoleMapper {
  void update(@MappingTarget RoleEntity entity, RoleDto src);

  RoleDto toRoleDto(RoleEntity entity);
}
