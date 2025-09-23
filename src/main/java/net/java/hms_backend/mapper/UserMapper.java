package net.java.hms_backend.mapper;

import net.java.hms_backend.dto.UserDto;
import net.java.hms_backend.entity.Role;
import net.java.hms_backend.entity.User;

import java.util.List;
import java.util.stream.Collectors;

public class UserMapper {

    public static UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setEmail(user.getEmail());
        dto.setRoles(user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList()));
        dto.setPassword(null);
        BaseMapper.mapAuditFields(user, dto);
        return dto;
    }


    public static User toEntity(UserDto dto, List<Role> roles) {
        User user = new User();
        user.setId(dto.getId());
        user.setFullName(dto.getFullName());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setEmail(dto.getEmail());
        user.setRoles(roles);
        user.setPassword(dto.getPassword());
        return user;
    }
}
