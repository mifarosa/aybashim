package com.aybashim.backend.controller;

import com.aybashim.backend.dto.AuthRequest;
import com.aybashim.backend.dto.AuthResponse;
import com.aybashim.backend.dto.RegisterRequest;
import com.aybashim.backend.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {
        return authService.login(request);
    }
}
