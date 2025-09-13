package net.java.hms_backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.java.hms_backend.config.JwtUtil;
import net.java.hms_backend.dto.AssetDto;
import net.java.hms_backend.entity.Role;
import net.java.hms_backend.entity.Room;
import net.java.hms_backend.entity.User;
import net.java.hms_backend.repository.AssetRepository;
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

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class AssetControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private RoomRepository roomRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private AssetRepository assetRepository;

    private String adminToken;
    private String accountantToken;
    private AssetDto testAsset;

    @BeforeEach
    void setup() {
        assetRepository.deleteAll();
        roomRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role adminRole = roleRepository.save(new Role("ADMIN"));
        Role accountantRole = roleRepository.save(new Role("ACCOUNTANT"));
        User admin = new User();
        admin.setEmail("admin@example.com");
        admin.setPassword(passwordEncoder.encode("123456"));
        admin.setRoles(List.of(adminRole));
        User accountant = new User();
        accountant.setEmail("accountant@example.com");
        accountant.setPassword(passwordEncoder.encode("123456"));
        accountant.setRoles(List.of(accountantRole));

        userRepository.saveAll(List.of(admin, accountant));

        adminToken = jwtUtil.generateToken(admin);
        accountantToken = jwtUtil.generateToken(accountant);

        Room room = new Room();
        room.setRoomNumber(101);
        room.setMaxOccupancy(2);
        room.setRoomType("Deluxe");
        room.setStatus("Available");
        room.setLocation("Floor 1");
        roomRepository.save(room);

        testAsset = new AssetDto();
        testAsset.setName("TV");
        testAsset.setCategory("Electronics");
        testAsset.setCondition("New");
        testAsset.setOriginalCost(500.0);
        testAsset.setPurchaseDate(LocalDate.now());
        testAsset.setNote("Smart TV 55 inch");
        testAsset.setRoomNumber(room.getRoomNumber());
    }

    @Test
    void testCreateAssetWithAdmin_shouldSucceed() throws Exception {
        mockMvc.perform(post("/api/assets")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAsset)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("TV"));
    }

    @Test
    void testCreateAssetWithAccountant_shouldReturn403() throws Exception {
        mockMvc.perform(post("/api/assets")
                        .header("Authorization", "Bearer " + accountantToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAsset)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCreateAssetWithoutToken_shouldReturn401() throws Exception {
        mockMvc.perform(post("/api/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAsset)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testDeleteAssetWithAdmin_shouldSucceed() throws Exception {
        String response = mockMvc.perform(post("/api/assets")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAsset)))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/assets/" + id)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Asset deleted successfully")));
    }

    @Test
    void testDeleteAssetWithAccountant_shouldReturn403() throws Exception {
        String response = mockMvc.perform(post("/api/assets")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAsset)))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/assets/" + id)
                        .header("Authorization", "Bearer " + accountantToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetAssetByIdWithAdmin_shouldSucceed() throws Exception {
        String response = mockMvc.perform(post("/api/assets")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAsset)))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/assets/" + id)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("TV"));
    }

    @Test
    void testGetAssetByIdWithAccountant_shouldReturn403() throws Exception {
        String response = mockMvc.perform(post("/api/assets")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAsset)))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/assets/" + id)
                        .header("Authorization", "Bearer " + accountantToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetAllAssetsWithAdmin_shouldSucceed() throws Exception {
        mockMvc.perform(get("/api/assets")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(0))));
    }

    @Test
    void testGetAllAssetsWithAccountant_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/assets")
                        .header("Authorization", "Bearer " + accountantToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUpdateAssetWithAdmin_shouldSucceed() throws Exception {
        String response = mockMvc.perform(post("/api/assets")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAsset)))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();
        testAsset.setCondition("Used");

        mockMvc.perform(put("/api/assets/" + id)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAsset)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.condition").value("Used"));
    }

    @Test
    void testUpdateAssetWithAccountant_shouldReturn403() throws Exception {
        String response = mockMvc.perform(post("/api/assets")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAsset)))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();
        testAsset.setCondition("Used");

        mockMvc.perform(put("/api/assets/" + id)
                        .header("Authorization", "Bearer " + accountantToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAsset)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCreateAssetWithoutRoomNumber_shouldReturn400() throws Exception {
        testAsset.setRoomNumber(null);

        mockMvc.perform(post("/api/assets")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAsset)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateAssetWithInvalidRoomNumber_shouldReturn404() throws Exception {
        testAsset.setRoomNumber(999);

        mockMvc.perform(post("/api/assets")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAsset)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Room")));
    }

    @Test
    void testUpdateAssetWithInvalidRoomNumber_shouldReturn404() throws Exception {
        String response = mockMvc.perform(post("/api/assets")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAsset)))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();
        testAsset.setRoomNumber(999);

        mockMvc.perform(put("/api/assets/" + id)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAsset)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Room")));
    }
}
