package net.java.hms_backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.java.hms_backend.config.JwtUtil;
import net.java.hms_backend.dto.RoomDto;
import net.java.hms_backend.entity.Role;
import net.java.hms_backend.entity.User;
import net.java.hms_backend.repository.RoleRepository;
import net.java.hms_backend.repository.RoomRepository;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RoomRepository roomRepository;

    private String jwtToken;
    private RoomDto testRoom;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        roomRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role adminRole = roleRepository.save(new Role("ADMIN"));
        User user = new User();
        user.setEmail("admin@example.com");
        user.setPassword(passwordEncoder.encode("123456"));
        user.setRoles(List.of(adminRole));
        userRepository.save(user);

        jwtToken = jwtUtil.generateToken(user);

        testRoom = new RoomDto();
        testRoom.setRoomNumber(101);
        testRoom.setMaxOccupancy(2);
        testRoom.setRoomType("Deluxe");
        testRoom.setStatus("Available");
        testRoom.setLocation("Floor 1");
    }

    @Test
    void testCreateRoom() throws Exception {
        mockMvc.perform(post("/api/rooms")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRoom)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roomNumber").value(101));
    }

    @Test
    void testGetRoomById() throws Exception {
        // Tạo room trước để có ID
        String response = mockMvc.perform(post("/api/rooms")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRoom)))
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/rooms/" + id)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomType").value("Deluxe"));
    }

    @Test
    void testUpdateRoom() throws Exception {
        String response = mockMvc.perform(post("/api/rooms")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRoom)))
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(response).get("id").asLong();

        testRoom.setRoomType("Suite");

        mockMvc.perform(put("/api/rooms/" + id)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRoom)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomType").value("Suite"));
    }

    @Test
    void testDeleteRoom() throws Exception {
        String response = mockMvc.perform(post("/api/rooms")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRoom)))
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/rooms/" + id)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Room deleted successfully")));
    }

    @Test
    void testGetAllRooms() throws Exception {
        mockMvc.perform(post("/api/rooms")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRoom)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/rooms")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void testCreateRoomWithWrongRole_shouldReturn403() throws Exception {
        Role accountantRole = roleRepository.save(new Role("ACCOUNTANT"));

        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword(passwordEncoder.encode("123456"));
        user.setRoles(List.of(accountantRole));
        userRepository.save(user);

        String token = jwtUtil.generateToken(user);

        mockMvc.perform(post("/api/rooms")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRoom)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetRoomWithInvalidId_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/rooms/99999")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

}

