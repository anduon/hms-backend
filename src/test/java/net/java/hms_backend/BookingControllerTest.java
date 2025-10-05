package net.java.hms_backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.java.hms_backend.config.JwtUtil;
import net.java.hms_backend.dto.BookingDto;
import net.java.hms_backend.entity.Booking;
import net.java.hms_backend.entity.Role;
import net.java.hms_backend.entity.Room;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private RoomRepository roomRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private NotificationRepository notificationRepository;

    private String adminToken;
    private String accountantToken;
    private Room testRoom;

    @BeforeEach
    void setup() {
        notificationRepository.deleteAll();
        bookingRepository.deleteAll();
        roomRepository.deleteAll();
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

        testRoom = new Room();
        testRoom.setRoomNumber(101);
        testRoom.setRoomType("DELUXE");
        testRoom.setStatus("AVAILABLE");
        testRoom.setMaxOccupancy(10);
        roomRepository.save(testRoom);
    }

    BookingDto createSampleBookingDto() {
        BookingDto dto = new BookingDto();
        dto.setGuestFullName("Nguyễn Văn A");
        dto.setGuestIdNumber("123456789");
        dto.setGuestNationality("Việt Nam");
        dto.setRoomNumber(testRoom.getRoomNumber());
        dto.setCheckInDate(LocalDateTime.now().plusDays(1));
        dto.setCheckOutDate(LocalDateTime.now().plusDays(3));
        dto.setBookingType("DAILY");
        dto.setStatus("CONFIRMED");
        dto.setNumberOfGuests(2);
        dto.setNotes("Ghi chú");
        return dto;
    }

    @Test
    void testAdminCanCreateBooking_shouldReturn200() throws Exception {
        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createSampleBookingDto())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guestFullName").value("Nguyễn Văn A"));
    }

    @Test
    void testAccountantCannotCreateBooking_shouldReturn403() throws Exception {
        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + accountantToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createSampleBookingDto())))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAdminCanGetBookingById_shouldReturn200() throws Exception {
        Booking booking = new Booking();
        booking.setGuestFullName("Nguyễn Văn A");
        booking.setGuestIdNumber("123456789");
        booking.setGuestNationality("Việt Nam");
        booking.setRoom(testRoom);
        booking.setCheckInDate(LocalDateTime.now().plusDays(1));
        booking.setCheckOutDate(LocalDateTime.now().plusDays(3));
        booking.setBookingType("ONLINE");
        booking.setStatus("CONFIRMED");
        booking.setNumberOfGuests(2);
        bookingRepository.save(booking);

        mockMvc.perform(get("/api/bookings/" + booking.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guestFullName").value("Nguyễn Văn A"));
    }

    @Test
    void testAccountantCannotGetBookingById_shouldReturn403() throws Exception {
        Booking booking = new Booking();
        booking.setGuestFullName("Nguyễn Văn A");
        booking.setGuestIdNumber("123456789");
        booking.setGuestNationality("Việt Nam");
        booking.setRoom(testRoom);
        booking.setCheckInDate(LocalDateTime.now().plusDays(1));
        booking.setCheckOutDate(LocalDateTime.now().plusDays(3));
        booking.setBookingType("ONLINE");
        booking.setStatus("CONFIRMED");
        booking.setNumberOfGuests(2);
        bookingRepository.save(booking);
        mockMvc.perform(get("/api/bookings/" + booking.getId())
                        .header("Authorization", "Bearer " + accountantToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAdminCanGetAllBookings_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/bookings")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void testAccountantCannotGetAllBookings_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/bookings")
                        .header("Authorization", "Bearer " + accountantToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAdminCanUpdateBooking_shouldReturn200() throws Exception {
        Booking booking = new Booking();
        booking.setGuestFullName("Nguyễn Văn A");
        booking.setGuestIdNumber("123456789");
        booking.setGuestNationality("Việt Nam");
        booking.setRoom(testRoom);
        booking.setCheckInDate(LocalDateTime.now().plusDays(1));
        booking.setCheckOutDate(LocalDateTime.now().plusDays(3));
        booking.setBookingType("ONLINE");
        booking.setStatus("CONFIRMED");
        booking.setNumberOfGuests(2);
        bookingRepository.save(booking);        BookingDto updatedDto = createSampleBookingDto();
        updatedDto.setGuestFullName("Nguyễn Văn B");

        mockMvc.perform(put("/api/bookings/" + booking.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guestFullName").value("Nguyễn Văn B"));
    }

    @Test
    void testAccountantCannotUpdateBooking_shouldReturn403() throws Exception {
        Booking booking = new Booking();
        booking.setGuestFullName("Nguyễn Văn A");
        booking.setGuestIdNumber("123456789");
        booking.setGuestNationality("Việt Nam");
        booking.setRoom(testRoom);
        booking.setCheckInDate(LocalDateTime.now().plusDays(1));
        booking.setCheckOutDate(LocalDateTime.now().plusDays(3));
        booking.setBookingType("ONLINE");
        booking.setStatus("CONFIRMED");
        booking.setNumberOfGuests(2);
        bookingRepository.save(booking);        BookingDto updatedDto = createSampleBookingDto();

        mockMvc.perform(put("/api/bookings/" + booking.getId())
                        .header("Authorization", "Bearer " + accountantToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAdminCanDeleteBooking_shouldReturn204() throws Exception {
        Booking booking = new Booking();
        booking.setGuestFullName("Nguyễn Văn A");
        booking.setGuestIdNumber("123456789");
        booking.setGuestNationality("Việt Nam");
        booking.setRoom(testRoom);
        booking.setCheckInDate(LocalDateTime.now().plusDays(1));
        booking.setCheckOutDate(LocalDateTime.now().plusDays(3));
        booking.setBookingType("ONLINE");
        booking.setStatus("CONFIRMED");
        booking.setNumberOfGuests(2);
        bookingRepository.save(booking);
        mockMvc.perform(delete("/api/bookings/" + booking.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void testAccountantCannotDeleteBooking_shouldReturn403() throws Exception {
        Booking booking = new Booking();
        booking.setGuestFullName("Nguyễn Văn A");
        booking.setGuestIdNumber("123456789");
        booking.setGuestNationality("Việt Nam");
        booking.setRoom(testRoom);
        booking.setCheckInDate(LocalDateTime.now().plusDays(1));
        booking.setCheckOutDate(LocalDateTime.now().plusDays(3));
        booking.setBookingType("ONLINE");
        booking.setStatus("CONFIRMED");
        booking.setNumberOfGuests(2);
        bookingRepository.save(booking);
        mockMvc.perform(delete("/api/bookings/" + booking.getId())
                        .header("Authorization", "Bearer " + accountantToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCreateBookingWithInvalidDateRange_shouldReturn400() throws Exception {
        BookingDto dto = createSampleBookingDto();
        dto.setCheckInDate(LocalDateTime.now().plusDays(3));
        dto.setCheckOutDate(LocalDateTime.now().plusDays(1));

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Check-out date must be after check-in date."));
    }

    @Test
    void testUpdateBookingWithInvalidDateRange_shouldReturn400() throws Exception {
        Booking booking = new Booking();
        booking.setGuestFullName("Nguyễn Văn A");
        booking.setGuestIdNumber("123456789");
        booking.setGuestNationality("Việt Nam");
        booking.setRoom(testRoom);
        booking.setCheckInDate(LocalDateTime.now().plusDays(1));
        booking.setCheckOutDate(LocalDateTime.now().plusDays(3));
        booking.setBookingType("ONLINE");
        booking.setStatus("CONFIRMED");
        booking.setNumberOfGuests(2);
        bookingRepository.save(booking);        BookingDto updatedDto = createSampleBookingDto();
        updatedDto.setCheckInDate(LocalDateTime.now().plusDays(5));

        mockMvc.perform(put("/api/bookings/" + booking.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Check-out date must be after check-in date."));
    }

    @Test
    void testMissingGuestFullName_shouldReturn400() throws Exception {
        BookingDto dto = createSampleBookingDto();
        dto.setGuestFullName(null);

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Guest name is required"));
    }

    @Test
    void testMissingGuestIdNumber_shouldReturn400() throws Exception {
        BookingDto dto = createSampleBookingDto();
        dto.setGuestIdNumber(null);

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("ID number is required"));
    }

    @Test
    void testMissingRoomNumber_shouldReturn400() throws Exception {
        BookingDto dto = createSampleBookingDto();
        dto.setRoomNumber(null);

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Room number is required"));
    }

    @Test
    void testMissingCheckInDate_shouldReturn400() throws Exception {
        BookingDto dto = createSampleBookingDto();
        dto.setCheckInDate(null);

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Check-in date is required"));
    }

    @Test
    void testMissingCheckOutDate_shouldReturn400() throws Exception {
        BookingDto dto = createSampleBookingDto();
        dto.setCheckOutDate(null);

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Check-out date is required"));
    }

    @Test
    void testMissingBookingType_shouldReturn400() throws Exception {
        BookingDto dto = createSampleBookingDto();
        dto.setBookingType(null);

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Booking type is required"));
    }

    @Test
    void testMissingStatus_shouldReturn400() throws Exception {
        BookingDto dto = createSampleBookingDto();
        dto.setStatus(null);

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Status is required"));
    }

    @Test
    void testMissingNumberOfGuests_shouldReturn400() throws Exception {
        BookingDto dto = createSampleBookingDto();
        dto.setNumberOfGuests(null);

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Number of guests is required"));
    }

    @Test
    void testCreateBookingWithNonexistentRoom_shouldReturn404() throws Exception {
        BookingDto dto = createSampleBookingDto();
        dto.setRoomNumber(999);

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Room not found with roomNumber : '999'"));
    }

    @Test
    void testUpdateBookingWithNonexistentRoom_shouldReturn404() throws Exception {
        Booking booking = new Booking();
        booking.setGuestFullName("Nguyễn Văn A");
        booking.setGuestIdNumber("123456789");
        booking.setGuestNationality("Việt Nam");
        booking.setRoom(testRoom);
        booking.setCheckInDate(LocalDateTime.now().plusDays(1));
        booking.setCheckOutDate(LocalDateTime.now().plusDays(3));
        booking.setBookingType("ONLINE");
        booking.setStatus("CONFIRMED");
        booking.setNumberOfGuests(2);
        bookingRepository.save(booking);        BookingDto updatedDto = createSampleBookingDto();
        updatedDto.setRoomNumber(999);

        mockMvc.perform(put("/api/bookings/" + booking.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Room not found with roomNumber : '999'"));
    }

    @Test
    void testCreateBookingWithOverlappingDates_shouldReturnConflict() throws Exception {
        Booking booking = new Booking();
        booking.setGuestFullName("Nguyễn Văn A");
        booking.setGuestIdNumber("123456789");
        booking.setGuestNationality("Việt Nam");
        booking.setRoom(testRoom);
        booking.setCheckInDate(LocalDateTime.now().plusDays(1));
        booking.setCheckOutDate(LocalDateTime.now().plusDays(3));
        booking.setBookingType("ONLINE");
        booking.setStatus("CONFIRMED");
        booking.setNumberOfGuests(2);
        bookingRepository.save(booking);

        BookingDto overlappingBooking = createSampleBookingDto();
        overlappingBooking.setRoomNumber(101);
        overlappingBooking.setCheckInDate(LocalDateTime.now().plusDays(2));
        overlappingBooking.setCheckOutDate(LocalDateTime.now().plusDays(4));

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(overlappingBooking)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Room is already booked during the requested period."));
    }

    @Test
    void testUpdateBookingWithOverlappingDates_shouldReturnConflict() throws Exception {
        Booking booking = new Booking();
        booking.setGuestFullName("Nguyễn Văn A");
        booking.setGuestIdNumber("123456789");
        booking.setGuestNationality("Việt Nam");
        booking.setRoom(testRoom);
        booking.setCheckInDate(LocalDateTime.now().plusDays(1));
        booking.setCheckOutDate(LocalDateTime.now().plusDays(3));
        booking.setBookingType("ONLINE");
        booking.setStatus("CONFIRMED");
        booking.setNumberOfGuests(2);
        bookingRepository.save(booking);

        Booking newBooking = new Booking();
        newBooking.setGuestFullName("Nguyễn Văn A");
        newBooking.setGuestIdNumber("123456789");
        newBooking.setGuestNationality("Việt Nam");
        newBooking.setRoom(testRoom);
        newBooking.setCheckInDate(LocalDateTime.now().plusDays(4));
        newBooking.setCheckOutDate(LocalDateTime.now().plusDays(6));
        newBooking.setBookingType("ONLINE");
        newBooking.setStatus("CONFIRMED");
        newBooking.setNumberOfGuests(2);
        bookingRepository.save(newBooking);

        BookingDto overlappingBooking = createSampleBookingDto();
        overlappingBooking.setRoomNumber(101);
        overlappingBooking.setCheckInDate(LocalDateTime.now().plusDays(2));
        overlappingBooking.setCheckOutDate(LocalDateTime.now().plusDays(4));

        mockMvc.perform(put("/api/bookings/" + newBooking.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(overlappingBooking)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Room is already booked during the requested period."));
    }

    @Test
    void testCreateBookingWithInvalidBookingType_shouldReturn400() throws Exception {
        BookingDto invalidBooking = createSampleBookingDto();
        invalidBooking.setBookingType("WEEKLY");

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidBooking)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid booking type"));
    }

}
