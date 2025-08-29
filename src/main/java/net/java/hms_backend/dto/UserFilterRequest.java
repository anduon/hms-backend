package net.java.hms_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFilterRequest {
    private String fullName;
    private String phoneNumber;
    private String email;
    private List<String> roles;
}
