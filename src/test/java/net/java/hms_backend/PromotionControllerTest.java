package net.java.hms_backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.java.hms_backend.config.JwtUtil;
import net.java.hms_backend.dto.PromotionDto;
import net.java.hms_backend.entity.*;
import net.java.hms_backend.repository.RoleRepository;
import net.java.hms_backend.repository.PromotionRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PromotionControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PromotionRepository promotionRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String accountantToken;

    @BeforeEach
    void setup() {
        promotionRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role adminRole = roleRepository.save(new Role("ADMIN"));
        Role accountantRole = roleRepository.save(new Role("ACCOUNTANT"));

        User admin = new User();
        admin.setEmail("admin@example.com");
        admin.setPassword(passwordEncoder.encode("123456"));
        admin.setRoles(List.of(adminRole));
        userRepository.save(admin);
        adminToken = jwtUtil.generateToken(admin);

        User accountant = new User();
        accountant.setEmail("accountant@example.com");
        accountant.setPassword(passwordEncoder.encode("123456"));
        accountant.setRoles(List.of(accountantRole));
        userRepository.save(accountant);
        accountantToken = jwtUtil.generateToken(accountant);

        Promotion promo = new Promotion();
        promo.setName("Summer Sale");
        promo.setDiscountPercent(15.0);
        promo.setStartDate(LocalDate.now());
        promo.setEndDate(LocalDate.now().plusDays(10));
        promotionRepository.save(promo);
    }

    @Test
    void testMissingPromotionName_shouldReturn400() throws Exception {
        PromotionDto dto = new PromotionDto(null, null, 10.0, LocalDate.now(), LocalDate.now().plusDays(5));

        mockMvc.perform(post("/api/promotions")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Promotion name is required"));
    }

    @Test
    void testMissingDiscountPercent_shouldReturn400() throws Exception {
        PromotionDto dto = new PromotionDto(null, "Summer Sale", null, LocalDate.now(), LocalDate.now().plusDays(5));

        mockMvc.perform(post("/api/promotions")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Discount percent is required"));
    }

    @Test
    void testMissingStartDate_shouldReturn400() throws Exception {
        PromotionDto dto = new PromotionDto(null, "Summer Sale", 10.0, null, LocalDate.now().plusDays(5));

        mockMvc.perform(post("/api/promotions")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Start date is required"));
    }

    @Test
    void testMissingEndDate_shouldReturn400() throws Exception {
        PromotionDto dto = new PromotionDto(null, "Summer Sale", 10.0, LocalDate.now(), null);

        mockMvc.perform(post("/api/promotions")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("End date is required"));
    }

    @Test
    void testAdminCanCreatePromotion_shouldReturn201() throws Exception {
        PromotionDto dto = new PromotionDto(null, "Winter Sale", 20.0, LocalDate.now(), LocalDate.now().plusDays(5));

        mockMvc.perform(post("/api/promotions")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Winter Sale"));
    }

    @Test
    void testReceptionistCannotCreatePromotion_shouldReturn403() throws Exception {
        PromotionDto dto = new PromotionDto(null, "Winter Sale", 20.0, LocalDate.now(), LocalDate.now().plusDays(5));

        mockMvc.perform(post("/api/promotions")
                        .header("Authorization", "Bearer " + accountantToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAdminCanGetAllPromotions_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/promotions")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Summer Sale"));
    }

    @Test
    void testReceptionistCannotGetAllPromotions_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/promotions")
                        .header("Authorization", "Bearer " + accountantToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAdminCanGetPromotionById_shouldReturn200() throws Exception {
        Promotion promo = promotionRepository.findAll().get(0);

        mockMvc.perform(get("/api/promotions/" + promo.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Summer Sale"));
    }

    @Test
    void testReceptionistCannotGetPromotionById_shouldReturn403() throws Exception {
        Promotion promo = promotionRepository.findAll().get(0);

        mockMvc.perform(get("/api/promotions/" + promo.getId())
                        .header("Authorization", "Bearer " + accountantToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAdminCanUpdatePromotion_shouldReturn200() throws Exception {
        Promotion promo = promotionRepository.findAll().get(0);
        PromotionDto updated = new PromotionDto(null, "Updated Sale", 25.0, LocalDate.now(), LocalDate.now().plusDays(7));

        mockMvc.perform(put("/api/promotions/" + promo.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Sale"));
    }

    @Test
    void testReceptionistCannotUpdatePromotion_shouldReturn403() throws Exception {
        Promotion promo = promotionRepository.findAll().get(0);
        PromotionDto updated = new PromotionDto(null, "Updated Sale", 25.0, LocalDate.now(), LocalDate.now().plusDays(7));

        mockMvc.perform(put("/api/promotions/" + promo.getId())
                        .header("Authorization", "Bearer " + accountantToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAdminCanDeletePromotion_shouldReturn200() throws Exception {
        Promotion promo = promotionRepository.findAll().get(0);

        mockMvc.perform(delete("/api/promotions/" + promo.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void testReceptionistCannotDeletePromotion_shouldReturn403() throws Exception {
        Promotion promo = promotionRepository.findAll().get(0);

        mockMvc.perform(delete("/api/promotions/" + promo.getId())
                        .header("Authorization", "Bearer " + accountantToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAdminCanGetActivePromotion_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/promotions/active")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Summer Sale"));
    }

    @Test
    void testReceptionistCannotGetActivePromotion_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/promotions/active")
                        .header("Authorization", "Bearer " + accountantToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCreatePromotionWithInvalidDiscount_shouldReturn400() throws Exception {
        PromotionDto dto = new PromotionDto(null, "Crazy Sale", 150.0, LocalDate.now(), LocalDate.now().plusDays(5));

        mockMvc.perform(post("/api/promotions")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Discount percent must be between 0 and 100"));
    }

    @Test
    void testCreatePromotionWithInvalidDateRange_shouldReturn400() throws Exception {
        PromotionDto dto = new PromotionDto(null, "Reverse Sale", 20.0, LocalDate.now().plusDays(5), LocalDate.now());

        mockMvc.perform(post("/api/promotions")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("End date must be after start date"));
    }

}
