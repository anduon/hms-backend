package net.java.hms_backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.java.hms_backend.config.JwtUtil;
import net.java.hms_backend.dto.RoomDto;
import net.java.hms_backend.entity.Role;
import net.java.hms_backend.entity.User;
import net.java.hms_backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

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

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    private String jwtToken;
    private String accountantToken;
    private RoomDto testRoom;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        notificationRepository.deleteAll();
        bookingRepository.deleteAll();
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

        Role accountantRole = roleRepository.save(new Role("ACCOUNTANT"));
        User accountant = new User();
        accountant.setEmail("accountant@example.com");
        accountant.setPassword(passwordEncoder.encode("123456"));
        accountant.setRoles(List.of(accountantRole));
        userRepository.save(accountant);

        accountantToken = jwtUtil.generateToken(accountant);

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
    void testCreateRoomWithAccountant_shouldReturn403() throws Exception {
        mockMvc.perform(post("/api/rooms")
                        .header("Authorization", "Bearer " + accountantToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRoom)))
                .andExpect(status().isForbidden());
    }


    @Test
    void testGetRoomById() throws Exception {
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
    void testGetRoomByIdWithAccountant_shouldReturn403() throws Exception {
        String response = mockMvc.perform(post("/api/rooms")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRoom)))
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/rooms/" + id)
                        .header("Authorization", "Bearer " + accountantToken))
                .andExpect(status().isForbidden());
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
    void testUpdateRoomWithWrongRole_shouldReturn403() throws Exception {
        String response = mockMvc.perform(post("/api/rooms")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRoom)))
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(response).get("id").asLong();

        testRoom.setRoomType("Suite");

        mockMvc.perform(put("/api/rooms/" + id)
                        .header("Authorization", "Bearer " + accountantToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRoom)))
                .andExpect(status().isForbidden());
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
    void testDeleteRoomWithAccountant_shouldReturn403() throws Exception {
        String response = mockMvc.perform(post("/api/rooms")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRoom)))
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/rooms/" + id)
                        .header("Authorization", "Bearer " + accountantToken))
                .andExpect(status().isForbidden());
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
    void testGetAllRoomsWithAccountant_shouldReturn403() throws Exception {
        mockMvc.perform(post("/api/rooms")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRoom)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/rooms")
                        .header("Authorization", "Bearer " + accountantToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetRoomWithInvalidId_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/rooms/99999")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateRoomWithDuplicateRoomNumber_shouldReturn409() throws Exception {
        mockMvc.perform(post("/api/rooms")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRoom)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/rooms")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRoom)))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("Room number already exists")));
    }

    @Test
    void testUpdateRoomWithDuplicateRoomNumber_shouldReturn409() throws Exception {
        RoomDto roomA = new RoomDto();
        roomA.setRoomNumber(101);
        roomA.setRoomType("DELUXE");
        roomA.setMaxOccupancy(2);
        roomA.setStatus("AVAILABLE");

        MvcResult resultA = mockMvc.perform(post("/api/rooms")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roomA)))
                .andExpect(status().isCreated())
                .andReturn();

        RoomDto createdRoomA = objectMapper.readValue(resultA.getResponse().getContentAsString(), RoomDto.class);

        RoomDto roomB = new RoomDto();
        roomB.setRoomNumber(102);
        roomB.setRoomType("STANDARD");
        roomB.setMaxOccupancy(1);
        roomB.setStatus("AVAILABLE");

        MvcResult resultB = mockMvc.perform(post("/api/rooms")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roomB)))
                .andExpect(status().isCreated())
                .andReturn();

        RoomDto createdRoomB = objectMapper.readValue(resultB.getResponse().getContentAsString(), RoomDto.class);

        createdRoomB.setRoomNumber(101);

        mockMvc.perform(put("/api/rooms/{id}", createdRoomB.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createdRoomB)))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("Room number already exists")));
    }

    @Test
    void testCreateRoomWithoutRoomNumber_shouldReturn400() throws Exception {
        testRoom.setRoomNumber(null);

        mockMvc.perform(post("/api/rooms")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRoom)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateNonExistingRoom_shouldReturn404() throws Exception {
        testRoom.setRoomType("Executive");

        mockMvc.perform(put("/api/rooms/99999")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRoom)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteNonExistingRoom_shouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/rooms/99999")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateRoomWithoutToken_shouldReturn401() throws Exception {
        mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRoom)))
                .andExpect(status().isUnauthorized());
    }

}

