package net.java.hms_backend.config;

import lombok.RequiredArgsConstructor;
import net.java.hms_backend.entity.Role;
import net.java.hms_backend.entity.User;
import net.java.hms_backend.repository.RoleRepository;
import net.java.hms_backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
                    .orElseThrow(() -> new RuntimeException("ADMIN role not found!"));

            User adminUser = new User();
            adminUser.setFullName("Default Admin");
            adminUser.setEmail("admin@example.com");
            adminUser.setPassword(passwordEncoder.encode("12345678"));
            adminUser.setRoles(List.of(adminRole));

            userRepository.save(adminUser);
            System.out.println("Default admin user created: admin@example.com / 12345678");
        }
    }
}
