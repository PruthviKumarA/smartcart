package com.smartcart.order.service;

import com.smartcart.order.dto.AuthDtos.*;
import com.smartcart.order.entity.User;
import com.smartcart.order.exception.BadRequestException;
import com.smartcart.order.exception.DuplicateResourceException;
import com.smartcart.order.repository.UserRepository;
import com.smartcart.order.security.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Nested
    @DisplayName("register")
    class Register {

        @Test
        @DisplayName("should register user and return token")
        void shouldRegisterSuccessfully() {
            RegisterRequest request = new RegisterRequest();
            request.setName("Pruthvi");
            request.setEmail("pruthvi@test.com");
            request.setPassword("password123");

            User savedUser = User.builder()
                    .id(1L).name("Pruthvi").email("pruthvi@test.com")
                    .password("encoded").role(User.Role.CUSTOMER).build();

            when(userRepository.existsByEmail("pruthvi@test.com")).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("encoded");
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(jwtUtil.generateToken(1L, "pruthvi@test.com", "CUSTOMER")).thenReturn("jwt-token");

            AuthResponse response = authService.register(request);

            assertThat(response.getToken()).isEqualTo("jwt-token");
            assertThat(response.getEmail()).isEqualTo("pruthvi@test.com");
            assertThat(response.getRole()).isEqualTo("CUSTOMER");
            verify(passwordEncoder).encode("password123");
        }

        @Test
        @DisplayName("should throw when email already exists")
        void shouldThrow_whenEmailExists() {
            RegisterRequest request = new RegisterRequest();
            request.setEmail("existing@test.com");

            when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("Email already registered");
        }
    }

    @Nested
    @DisplayName("login")
    class Login {

        @Test
        @DisplayName("should login successfully with correct credentials")
        void shouldLoginSuccessfully() {
            LoginRequest request = new LoginRequest();
            request.setEmail("pruthvi@test.com");
            request.setPassword("password123");

            User user = User.builder()
                    .id(1L).name("Pruthvi").email("pruthvi@test.com")
                    .password("encoded").role(User.Role.CUSTOMER).build();

            when(userRepository.findByEmail("pruthvi@test.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("password123", "encoded")).thenReturn(true);
            when(jwtUtil.generateToken(1L, "pruthvi@test.com", "CUSTOMER")).thenReturn("jwt-token");

            AuthResponse response = authService.login(request);

            assertThat(response.getToken()).isEqualTo("jwt-token");
            assertThat(response.getName()).isEqualTo("Pruthvi");
        }

        @Test
        @DisplayName("should throw when email not found")
        void shouldThrow_whenEmailNotFound() {
            LoginRequest request = new LoginRequest();
            request.setEmail("unknown@test.com");
            request.setPassword("password");

            when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Invalid email or password");
        }

        @Test
        @DisplayName("should throw when password is wrong")
        void shouldThrow_whenWrongPassword() {
            LoginRequest request = new LoginRequest();
            request.setEmail("pruthvi@test.com");
            request.setPassword("wrong");

            User user = User.builder()
                    .id(1L).email("pruthvi@test.com").password("encoded")
                    .role(User.Role.CUSTOMER).build();

            when(userRepository.findByEmail("pruthvi@test.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Invalid email or password");
        }
    }
}
