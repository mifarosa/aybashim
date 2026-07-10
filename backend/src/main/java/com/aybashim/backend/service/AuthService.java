package com.aybashim.backend.service;

import com.aybashim.backend.dto.AuthRequest;
import com.aybashim.backend.dto.AuthResponse;
import com.aybashim.backend.dto.ProfileUpdateRequest;
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
    private final CurrentUser currentUser;

    public AuthService(UserRepository userRepository, PasswordService passwordService, JwtService jwtService, CurrentUser currentUser) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.jwtService = jwtService;
        this.currentUser = currentUser;
    }

    public AuthResponse register(RegisterRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Register request is required");
        }
        if (request.name() == null || request.name().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required");
        }
        if (request.password() == null || request.password().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
        }

        String email = normalizeEmail(request.email());
        if (email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }
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
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Login request is required");
        }
        AppUser user = userRepository.findByEmail(normalizeEmail(request.email()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordService.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        return toResponse(user);
    }

    public AuthResponse updateProfile(ProfileUpdateRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Profile update request is required");
        }
        AppUser user = currentUser.get();
        String name = request.name() == null ? "" : request.name().trim();
        String email = normalizeEmail(request.email());

        if (name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required");
        }
        if (email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }

        userRepository.findByEmail(email)
                .filter(existing -> !existing.getId().equals(user.getId()))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
                });

        user.setName(name);
        user.setEmail(email);

        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordService.hash(request.password()));
        }

        return toResponse(userRepository.save(user));
    }

    private AuthResponse toResponse(AppUser user) {
        return new AuthResponse(jwtService.generate(user), user.getId(), user.getName(), user.getEmail());
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
