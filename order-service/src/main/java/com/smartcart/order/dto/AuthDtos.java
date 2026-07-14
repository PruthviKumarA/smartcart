package com.smartcart.order.dto;

import jakarta.validation.constraints.*;
import lombok.*;

public class AuthDtos {
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class RegisterRequest {
        @NotBlank private String name;
        @NotBlank @Email private String email;
        @NotBlank @Size(min = 6) private String password;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class LoginRequest {
        @NotBlank private String email;
        @NotBlank private String password;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AuthResponse {
        private String token;
        private String email;
        private String name;
        private String role;
    }
}
