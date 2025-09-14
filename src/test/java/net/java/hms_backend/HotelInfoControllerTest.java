package net.java.hms_backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.java.hms_backend.config.JwtUtil;
import net.java.hms_backend.dto.HotelInfoDto;
import net.java.hms_backend.entity.HotelInfo;
import net.java.hms_backend.entity.Role;
import net.java.hms_backend.entity.User;
import net.java.hms_backend.repository.HotelInfoRepository;
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

import java.time.LocalTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class HotelInfoControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private HotelInfoRepository hotelInfoRepository;

    private String adminToken;
    private String receptionistToken;

    @BeforeEach
    void setup() {
        hotelInfoRepository.deleteAll();
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

        HotelInfo hotel = new HotelInfo();
        hotel.setName("HOTELIO");
        hotel.setAddress("123 Đường ABC, Quận 1, TP.HCM");
        hotel.setPhone("0123456789");
        hotel.setEmail("admin@example.com");
        hotel.setTaxCode("123456789");
        hotel.setNumberOfFloors(5);
        hotel.setCheckInTime(LocalTime.of(14, 0));
        hotel.setCheckOutTime(LocalTime.of(12, 0));
        hotelInfoRepository.save(hotel);
    }

    @Test
    void testGetHotelInfo_shouldReturnHotelInfoDto() throws Exception {
        mockMvc.perform(get("/api/hotel-info")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("HOTELIO"))
                .andExpect(jsonPath("$.address").value("123 Đường ABC, Quận 1, TP.HCM"));
    }

    @Test
    void testUpdateHotelInfo_shouldReturnUpdatedDto() throws Exception {
        HotelInfoDto updated = new HotelInfoDto();
        updated.setName("HOTELIO");
        updated.setAddress("456 Đường XYZ, Quận 3, TP.HCM");
        updated.setPhone("0987654321");
        updated.setEmail("luxury@ngoc.com");
        updated.setTaxCode("987654321");
        updated.setNumberOfFloors(10);
        updated.setCheckInTime(LocalTime.of(15, 0));
        updated.setCheckOutTime(LocalTime.of(11, 0));

        mockMvc.perform(put("/api/hotel-info")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("HOTELIO"))
                .andExpect(jsonPath("$.phone").value("0987654321"));
    }

    @Test
    void testReceptionistCannotUpdateHotelInfo_shouldReturn403() throws Exception {
        HotelInfoDto updated = new HotelInfoDto();
        updated.setName("Khách sạn Ngọc Luxury");
        updated.setAddress("456 Đường XYZ, Quận 3, TP.HCM");
        updated.setPhone("0987654321");
        updated.setEmail("luxury@ngoc.com");
        updated.setTaxCode("987654321");
        updated.setNumberOfFloors(10);
        updated.setCheckInTime(LocalTime.of(15, 0));
        updated.setCheckOutTime(LocalTime.of(11, 0));

        mockMvc.perform(put("/api/hotel-info")
                        .header("Authorization", "Bearer " + receptionistToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isForbidden());
    }

}
