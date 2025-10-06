package net.java.hms_backend.config;

import lombok.RequiredArgsConstructor;
import net.java.hms_backend.entity.HotelInfo;
import net.java.hms_backend.entity.Role;
import net.java.hms_backend.entity.User;
import net.java.hms_backend.exception.ResourceNotFoundException;
import net.java.hms_backend.repository.HotelInfoRepository;
import net.java.hms_backend.repository.RoleRepository;
import net.java.hms_backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final HotelInfoRepository hotelInfoRepository;


    @Override
    public void run(String... args) throws Exception {

        if (roleRepository.count() == 0) {
            Role admin = new Role();
            admin.setName("ADMIN");
            Role manager = new Role();
            manager.setName("MANAGER");
            Role receptionist = new Role();
            receptionist.setName("RECEPTIONIST");
            Role accountant = new Role();
            accountant.setName("ACCOUNTANT");

            roleRepository.saveAll(List.of(admin, manager, receptionist, accountant));
            System.out.println("Default roles inserted!");
        }

        if (userRepository.count() == 0) {
            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "name", "ADMIN"));

            User adminUser = new User();
            adminUser.setFullName("Default Admin");
            adminUser.setEmail("admin@example.com");
            adminUser.setPassword(passwordEncoder.encode("12345678"));
            adminUser.setRoles(List.of(adminRole));

            userRepository.save(adminUser);
            System.out.println("Default admin user created: admin@example.com / 12345678");
        }

        if (hotelInfoRepository.count() == 0) {
            HotelInfo hotelInfo = new HotelInfo();
            hotelInfo.setName("HOTELIO");
            hotelInfo.setAddress("123 Đường ABC, Quận 1, TP.HCM");
            hotelInfo.setPhone("0123456789");
            hotelInfo.setEmail("admin@example.com");
            hotelInfo.setTaxCode("123456789");
            hotelInfo.setNumberOfFloors(5);
            hotelInfo.setCheckInTime(LocalTime.of(14, 0));
            hotelInfo.setCheckOutTime(LocalTime.of(12, 0));
            hotelInfo.setWeekendSurchargePercent(10.0);

            hotelInfoRepository.save(hotelInfo);
            System.out.println("Default hotel info inserted!");
        }

    }
}
