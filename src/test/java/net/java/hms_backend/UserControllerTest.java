package net.java.hms_backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.java.hms_backend.config.JwtUtil;
import net.java.hms_backend.dto.UserDto;
import net.java.hms_backend.entity.Role;
import net.java.hms_backend.entity.User;
import net.java.hms_backend.repository.RoleRepository;
import net.java.hms_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.hasItem;


@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String receptionistToken;
    private UserDto testUser;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role adminRole = roleRepository.save(new Role("ADMIN"));
        User admin = new User();
        admin.setEmail("admin@example.com");
        admin.setPassword(passwordEncoder.encode("123456"));
        admin.setRoles(List.of(adminRole));
        userRepository.save(admin);

        adminToken = jwtUtil.generateToken(admin);

        Role receptionistRole = roleRepository.save(new Role("RECEPTIONIST"));
        User receptionist = new User();
        receptionist.setEmail("receptionist@example.com");
        receptionist.setPassword(passwordEncoder.encode("123456"));
        receptionist.setRoles(List.of(receptionistRole));
        userRepository.save(receptionist);

        receptionistToken = jwtUtil.generateToken(receptionist);

        testUser = new UserDto();
        testUser.setFullName("Nguyễn Văn A");
        testUser.setPhoneNumber("0123456789");
        testUser.setEmail("user@example.com");
        testUser.setPassword("123456");
        testUser.setRoles(List.of("ACCOUNTANT"));
    }

    @Test
    void testCreateUser_shouldReturnUserDto() throws Exception {
        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.fullName").value("Nguyễn Văn A"));
    }

    @Test
    void testCreateUserWithReceptionist_shouldReturn403() throws Exception {
        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + receptionistToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetUserById_shouldReturnUserDto() throws Exception {
        User saved = new User();
        saved.setEmail("user@example.com");
        saved.setPassword(passwordEncoder.encode("123456"));
        saved.setFullName("Nguyễn Văn A");
        saved.setPhoneNumber("0123456789");
        saved.setRoles(List.of(roleRepository.save(new Role("ACCOUNTANT"))));
        saved = userRepository.save(saved);

        mockMvc.perform(get("/api/users/" + saved.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@example.com"));
    }

    @Test
    void testGetUserByIdWithReceptionist_shouldReturn403() throws Exception {
        User saved = new User();
        saved.setEmail("user@example.com");
        saved.setPassword(passwordEncoder.encode("123456"));
        saved.setFullName("Nguyễn Văn A");
        saved.setPhoneNumber("0123456789");
        saved.setRoles(List.of(roleRepository.save(new Role("ACCOUNTANT"))));
        saved = userRepository.save(saved);

        mockMvc.perform(get("/api/users/" + saved.getId())
                        .header("Authorization", "Bearer " + receptionistToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testDeleteUserWithReceptionist_shouldReturn403() throws Exception {
        User saved = new User();
        saved.setEmail("delete@example.com");
        saved.setPassword(passwordEncoder.encode("123456"));
        saved.setFullName("Xóa Người Dùng");
        saved.setPhoneNumber("0999999999");
        saved.setRoles(List.of(roleRepository.save(new Role("ACCOUNTANT"))));
        saved = userRepository.save(saved);

        mockMvc.perform(delete("/api/users/" + saved.getId())
                        .header("Authorization", "Bearer " + receptionistToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAdminCanDeleteUser_shouldReturn200() throws Exception {
        User userToDelete = new User();
        userToDelete.setEmail("delete@example.com");
        userToDelete.setPassword(passwordEncoder.encode("123456"));
        userToDelete.setFullName("Người Bị Xóa");
        userToDelete.setPhoneNumber("0999999999");
        userToDelete.setRoles(List.of(roleRepository.save(new Role("ACCOUNTANT"))));
        userToDelete = userRepository.save(userToDelete);

        mockMvc.perform(delete("/api/users/" + userToDelete.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().string("User deleted successfully."));
    }

    @Test
    void testAdminCanUpdateUser_shouldReturnUpdatedDto() throws Exception {
        User userToUpdate = new User();
        userToUpdate.setEmail("update@example.com");
        userToUpdate.setPassword(passwordEncoder.encode("123456"));
        userToUpdate.setFullName("Người Cũ");
        userToUpdate.setPhoneNumber("0888888888");
        userToUpdate.setRoles(List.of(roleRepository.save(new Role("ACCOUNTANT"))));
        userToUpdate = userRepository.save(userToUpdate);

        UserDto updatedDto = new UserDto();
        updatedDto.setFullName("Người Mới");
        updatedDto.setPhoneNumber("0999999999");
        updatedDto.setEmail("update@example.com");
        updatedDto.setPassword("123456");
        updatedDto.setRoles(List.of("ACCOUNTANT"));

        mockMvc.perform(put("/api/users/" + userToUpdate.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Người Mới"));
    }

    @Test
    void testReceptionistCannotUpdateOtherUser_shouldReturn403() throws Exception {
        User userToUpdate = new User();
        userToUpdate.setEmail("update2@example.com");
        userToUpdate.setPassword(passwordEncoder.encode("123456"));
        userToUpdate.setFullName("Người Cũ");
        userToUpdate.setPhoneNumber("0888888888");
        userToUpdate.setRoles(List.of(roleRepository.save(new Role("ACCOUNTANT"))));
        userToUpdate = userRepository.save(userToUpdate);

        UserDto updatedDto = new UserDto();
        updatedDto.setFullName("Người Mới");
        updatedDto.setPhoneNumber("0999999999");
        updatedDto.setEmail("update2@example.com");
        updatedDto.setPassword("123456");
        updatedDto.setRoles(List.of("ACCOUNTANT"));

        mockMvc.perform(put("/api/users/" + userToUpdate.getId())
                        .header("Authorization", "Bearer " + receptionistToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testReceptionistCanUpdateOwnProfile_shouldReturnUpdatedDto() throws Exception {
        User receptionist = userRepository.findByEmail("receptionist@example.com").orElseThrow();

        UserDto updatedDto = new UserDto();
        updatedDto.setFullName("Lễ Tân Mới");
        updatedDto.setPhoneNumber("0111111111");
        updatedDto.setEmail("receptionist@example.com");
        updatedDto.setPassword("123456");

        mockMvc.perform(put("/api/users/" + receptionist.getId())
                        .header("Authorization", "Bearer " + receptionistToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Lễ Tân Mới"));
    }

    @Test
    void testGetCurrentUser_shouldReturnReceptionistInfo() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + receptionistToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("receptionist@example.com"))
                .andExpect(jsonPath("$.roles", hasItem("RECEPTIONIST")));
    }

    @Test
    void testReceptionistCannotUpdateOwnRole_shouldReturn403() throws Exception {
        User receptionist = userRepository.findByEmail("receptionist@example.com").orElseThrow();

        UserDto updatedDto = new UserDto();
        updatedDto.setFullName("Lễ Tân Mới");
        updatedDto.setPhoneNumber("0111111111");
        updatedDto.setEmail("receptionist@example.com");
        updatedDto.setPassword("123456");
        updatedDto.setRoles(List.of("ADMIN"));

        mockMvc.perform(put("/api/users/" + receptionist.getId())
                        .header("Authorization", "Bearer " + receptionistToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDto)))
                .andExpect(status().isForbidden());
    }

}
