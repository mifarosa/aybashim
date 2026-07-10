package com.aybashim.backend.dto;

public record ProfileUpdateRequest(
        String name,
        String email,
        String password
) {
}
