package org.example.mobilyecommerce.mapper;

import org.example.mobilyecommerce.dto.RoleDto;
import org.example.mobilyecommerce.dto.UserDto;
import org.example.mobilyecommerce.model.Role;
import org.example.mobilyecommerce.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    // User ↔ UserDto
    UserDto toDto(User user);
    User toEntity(UserDto userDto);

    // Role ↔ RoleDto
    RoleDto toDto(Role role);
    Role toEntity(RoleDto roleDto);

    // List تحويل القوائم
    List<RoleDto> toRoleDtoList(List<Role> roles);
    List<Role> toRoleEntityList(List<RoleDto> rolesDto);
}
