package net.java.hms_backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.java.hms_backend.dto.InvoiceDto;
import net.java.hms_backend.entity.*;
import net.java.hms_backend.repository.*;
import net.java.hms_backend.config.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InvoiceControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private RoleRepository roleRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private RoomRepository roomRepository;
    @Autowired private InvoiceRepository invoiceRepository;

    private String adminToken;
    private String receptionistToken;
    private InvoiceDto testInvoice;

    @BeforeEach
    void setup() {
        invoiceRepository.deleteAll();
        bookingRepository.deleteAll();
        roomRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role adminRole = roleRepository.save(new Role("ADMIN"));
        Role receptionistRole = roleRepository.save(new Role("RECEPTIONIST"));

        User admin = new User();
        admin.setEmail("admin@example.com");
        admin.setPassword(passwordEncoder.encode("123456"));
        admin.setRoles(List.of(adminRole));

        User receptionist = new User();
        receptionist.setEmail("receptionist@example.com");
        receptionist.setPassword(passwordEncoder.encode("123456"));
        receptionist.setRoles(List.of(receptionistRole));

        userRepository.saveAll(List.of(admin, receptionist));

        adminToken = jwtUtil.generateToken(admin);
        receptionistToken = jwtUtil.generateToken(receptionist);

        Room room = new Room();
        room.setRoomNumber(101);
        room.setMaxOccupancy(2);
        room.setRoomType("Deluxe");
        room.setStatus("Available");
        room.setLocation("Floor 1");

        RoomPrice price = new RoomPrice();
        price.setPriceType(PriceType.DAILY);
        price.setBasePrice(500.0);
        price.setRoom(room);
        room.setPrices(List.of(price));

        roomRepository.save(room);

        Booking booking = new Booking();
        booking.setGuestFullName("John Doe");
        booking.setGuestIdNumber("123456789");
        booking.setRoom(room);
        booking.setCheckInDate(LocalDateTime.now().minusDays(1));
        booking.setCheckOutDate(LocalDateTime.now().plusDays(2));
        booking.setBookingType("DAILY");
        booking.setStatus("CONFIRMED");
        booking.setNumberOfGuests(1);
        bookingRepository.save(booking);

        testInvoice = new InvoiceDto();
        testInvoice.setBookingId(booking.getId());
        testInvoice.setAmount(BigDecimal.valueOf(1500));
        testInvoice.setPaidAmount(BigDecimal.valueOf(200));
        testInvoice.setStatus("UNPAID");
        testInvoice.setIssuedDate(LocalDateTime.now());
        testInvoice.setDueDate(LocalDateTime.now().plusDays(7));
        testInvoice.setPaymentMethod("CASH");
        testInvoice.setNotes("Test invoice");
    }

    @Test
    void testCreateInvoiceWithAdmin_shouldSucceed() throws Exception {
        mockMvc.perform(post("/api/invoices")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testInvoice)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(1500));
    }

    @Test
    void testCreateInvoiceWithReceptionist_shouldSucceed() throws Exception {
        mockMvc.perform(post("/api/invoices")
                        .header("Authorization", "Bearer " + receptionistToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testInvoice)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(1500));
    }

    @Test
    void testGetAllInvoicesWithAdmin_shouldSucceed() throws Exception {
        mockMvc.perform(get("/api/invoices")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAllInvoicesWithReceptionist_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/invoices")
                        .header("Authorization", "Bearer " + receptionistToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testDeleteInvoiceWithAdmin_shouldSucceed() throws Exception {
        Long invoiceId = invoiceRepository.save(new Invoice(
                null,
                testInvoice.getAmount(),
                testInvoice.getPaidAmount(),
                testInvoice.getStatus(),
                testInvoice.getIssuedDate(),
                testInvoice.getDueDate(),
                testInvoice.getPaymentMethod(),
                testInvoice.getNotes(),
                bookingRepository.findAll().getFirst()
        )).getId();

        mockMvc.perform(delete("/api/invoices/" + invoiceId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteInvoiceWithReceptionist_shouldReturn403() throws Exception {
        Long invoiceId = invoiceRepository.save(new Invoice(
                null,
                testInvoice.getAmount(),
                testInvoice.getPaidAmount(),
                testInvoice.getStatus(),
                testInvoice.getIssuedDate(),
                testInvoice.getDueDate(),
                testInvoice.getPaymentMethod(),
                testInvoice.getNotes(),
                bookingRepository.findAll().getFirst()
        )).getId();

        mockMvc.perform(delete("/api/invoices/" + invoiceId)
                        .header("Authorization", "Bearer " + receptionistToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUpdateInvoiceWithAdmin_shouldSucceed() throws Exception {
        Booking booking = bookingRepository.findAll().getFirst();
        Invoice invoice = new Invoice(
                null,
                BigDecimal.valueOf(500),
                BigDecimal.valueOf(0),
                "UNPAID",
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(7),
                "CASH",
                "Initial invoice",
                booking
        );
        invoice = invoiceRepository.save(invoice);

        InvoiceDto updateDto = new InvoiceDto();
        updateDto.setPaidAmount(BigDecimal.valueOf(1500));
        updateDto.setStatus("PAID");
        updateDto.setPaymentMethod("CARD");
        updateDto.setNotes("Updated invoice");

        mockMvc.perform(put("/api/invoices/" + invoice.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.paidAmount").value(1500))
                .andExpect(jsonPath("$.paymentMethod").value("CARD"))
                .andExpect(jsonPath("$.notes").value("Updated invoice"));
    }

    @Test
    void testUpdateInvoiceWithReceptionist_shouldSucceed() throws Exception {
        Booking booking = bookingRepository.findAll().getFirst();
        Invoice invoice = new Invoice(
                null,
                BigDecimal.valueOf(500),
                BigDecimal.valueOf(0),
                "UNPAID",
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(7),
                "CASH",
                "Initial invoice",
                booking
        );
        invoice = invoiceRepository.save(invoice);

        InvoiceDto updateDto = new InvoiceDto();
        updateDto.setPaidAmount(BigDecimal.valueOf(1500));
        updateDto.setStatus("PAID");
        updateDto.setPaymentMethod("CARD");
        updateDto.setNotes("Updated invoice");

        mockMvc.perform(put("/api/invoices/" + invoice.getId())
                        .header("Authorization", "Bearer " + receptionistToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.paidAmount").value(1500))
                .andExpect(jsonPath("$.paymentMethod").value("CARD"))
                .andExpect(jsonPath("$.notes").value("Updated invoice"));
    }

    @Test
    void testGenerateInvoicePdfWithAdmin_shouldReturnPdf() throws Exception {
        Booking booking = bookingRepository.findAll().getFirst();
        Invoice invoice = new Invoice(
                null,
                BigDecimal.valueOf(1500),
                BigDecimal.valueOf(0),
                "UNPAID",
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(7),
                "CASH",
                "Invoice for PDF",
                booking
        );
        invoice = invoiceRepository.save(invoice);

        mockMvc.perform(get("/api/invoices/" + invoice.getId() + "/pdf")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=invoice_" + invoice.getId() + ".pdf"))
                .andExpect(result -> {
                    byte[] pdfBytes = result.getResponse().getContentAsByteArray();
                    assert pdfBytes.length > 0 : "PDF content is empty";
                });
    }

}
