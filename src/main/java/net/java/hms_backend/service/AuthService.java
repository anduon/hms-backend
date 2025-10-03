package net.java.hms_backend.service;

import lombok.RequiredArgsConstructor;
import net.java.hms_backend.dto.LoginRequest;
import net.java.hms_backend.dto.AuthResponse;
import net.java.hms_backend.entity.User;
import net.java.hms_backend.exception.AuthException;
import net.java.hms_backend.exception.ResourceNotFoundException;
import net.java.hms_backend.exception.UserException;
import net.java.hms_backend.repository.RoleRepository;
import net.java.hms_backend.repository.UserRepository;
import net.java.hms_backend.config.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuditLogService auditLogService;

    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail();
        String password = request.getPassword();

        if (email == null || email.isBlank()) {
            if (password == null || password.isBlank()) {
                throw new AuthException.MissingEmailAndPasswordException();
            }
            throw new AuthException.MissingEmailException();
        }

        if (password == null || password.isBlank()) {
            throw new AuthException.MissingPasswordException();
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Email not found", "email", email));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new UserException.InvalidPasswordException("Incorrect password");
        }

        String token = jwtUtil.generateToken(user);
        String details = "User logged in: [ID=" + user.getId() +
                ", Email=" + user.getEmail() +
                ", FullName=" + user.getFullName() + "]";

        auditLogService.log(
                email,
                "LOGIN",
                "User",
                user.getId(),
                details
        );
        return new AuthResponse(token);
    }
}
