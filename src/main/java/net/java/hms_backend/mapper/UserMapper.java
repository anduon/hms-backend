package net.java.hms_backend.mapper;

import net.java.hms_backend.dto.UserDto;
import net.java.hms_backend.entity.Role;
import net.java.hms_backend.entity.User;

import java.util.List;
import java.util.stream.Collectors;

public class UserMapper {

    public static UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getFullName(),
                user.getPhoneNumber(),
                user.getEmail(),
                user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toList()),
                null
        );
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
