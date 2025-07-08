package com.example.service.user.user;

import com.example.client.user.user.dto.RegisterUserRequest;
import com.example.client.user.user.dto.UpdateUserRequest;
import com.example.client.user.user.dto.UserDto;
import com.example.client.user.user.dto.UserDtoEx;
import com.example.service.user.user.model.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper
interface UserMapper {
  @Mapping(target = "firstName", expression = "java(src.firstName().strip())")
  @Mapping(
      target = "lastName",
      expression = "java(src.lastName() != null ? src.lastName().strip() : null)")
  @Mapping(target = "password", ignore = true)
  void update(@MappingTarget UserEntity entity, RegisterUserRequest src);

  @Mapping(target = "firstName", expression = "java(src.firstName().strip())")
  @Mapping(target = "lastName", expression = "java(src.lastName().strip())")
  void update(@MappingTarget UserEntity entity, UpdateUserRequest src);

  UserDto toUserDto(UserEntity entity);

  UserDtoEx toUserDtoEx(UserEntity entity);
}
