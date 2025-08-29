package net.java.hms_backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RegisterRequest {
    private String fullName;
    private String phoneNumber;
    private String email;
    private String password;
    private List<String> roles;
}
