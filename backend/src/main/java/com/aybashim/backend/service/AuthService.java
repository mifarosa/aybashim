package com.aybashim.backend.service;

import com.aybashim.backend.dto.AuthRequest;
import com.aybashim.backend.dto.AuthResponse;
import com.aybashim.backend.dto.RegisterRequest;
import com.aybashim.backend.model.AppUser;
import com.aybashim.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordService passwordService, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        AppUser user = new AppUser();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordService.hash(request.password()));
        AppUser saved = userRepository.save(user);

        return toResponse(saved);
    }

    public AuthResponse login(AuthRequest request) {
        AppUser user = userRepository.findByEmail(normalizeEmail(request.email()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordService.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        return toResponse(user);
    }

    private AuthResponse toResponse(AppUser user) {
        return new AuthResponse(jwtService.generate(user), user.getId(), user.getName(), user.getEmail());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
