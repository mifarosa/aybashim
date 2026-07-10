package com.aybashim.backend.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordServiceTests {

    private final PasswordService passwordService = new PasswordService();

    @Test
    void verifiesMatchingPasswordsAndRejectsDifferentPasswords() {
        String hash = passwordService.hash("correct-password");

        assertThat(passwordService.matches("correct-password", hash)).isTrue();
        assertThat(passwordService.matches("wrong-password", hash)).isFalse();
    }

    @Test
    void rejectsNullAndMalformedHashesWithoutThrowing() {
        assertThat(passwordService.matches(null, null)).isFalse();
        assertThat(passwordService.matches("password", "not-a-valid-hash")).isFalse();
        assertThat(passwordService.matches("password", "abc:def:ghi")).isFalse();
    }
}
