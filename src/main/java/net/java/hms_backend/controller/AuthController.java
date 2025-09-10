package net.java.hms_backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.java.hms_backend.dto.AuthResponse;
import net.java.hms_backend.dto.LoginRequest;
import net.java.hms_backend.dto.RegisterRequest;
import net.java.hms_backend.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
