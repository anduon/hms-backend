package net.java.hms_backend.service;

import net.java.hms_backend.dto.UserDto;
import net.java.hms_backend.dto.UserFilterRequest;
import org.springframework.data.domain.Page;

public interface UserService {

    UserDto createUser(UserDto dto);

    Page<UserDto> getAllUsers(int page, int size);

    UserDto getUserById(Long id);

    UserDto updateUser(Long id, UserDto dto);

    void deleteUser(Long id);

    UserDto getCurrentUser();

    Page<UserDto> searchUsers(UserFilterRequest filter, int page, int size);


}
