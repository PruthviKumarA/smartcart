package com.smartcart.order.service;

import com.smartcart.order.dto.AuthDtos.*;
import com.smartcart.order.entity.User;
import com.smartcart.order.exception.*;
import com.smartcart.order.repository.UserRepository;
import com.smartcart.order.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service @RequiredArgsConstructor @Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) throw new DuplicateResourceException("Email already registered");
        User user = User.builder().name(request.getName()).email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())).role(User.Role.CUSTOMER).build();
        User saved = userRepository.save(user);
        return AuthResponse.builder().token(jwtUtil.generateToken(saved.getId(), saved.getEmail(), saved.getRole().name()))
                .email(saved.getEmail()).name(saved.getName()).role(saved.getRole().name()).build();
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new BadRequestException("Invalid email or password"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) throw new BadRequestException("Invalid email or password");
        return AuthResponse.builder().token(jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name()))
                .email(user.getEmail()).name(user.getName()).role(user.getRole().name()).build();
    }
}
