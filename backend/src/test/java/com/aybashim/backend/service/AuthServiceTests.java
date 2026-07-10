package com.aybashim.backend.service;

import com.aybashim.backend.dto.ProfileUpdateRequest;
import com.aybashim.backend.dto.RegisterRequest;
import com.aybashim.backend.model.AppUser;
import com.aybashim.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTests {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordService passwordService = mock(PasswordService.class);
    private final JwtService jwtService = mock(JwtService.class);
    private final CurrentUser currentUser = mock(CurrentUser.class);
    private final AuthService authService = new AuthService(userRepository, passwordService, jwtService, currentUser);

    @Test
    void registerRejectsBlankRequiredFields() {
        assertThatThrownBy(() -> authService.register(new RegisterRequest(" ", "test@example.com", "secret")))
                .isInstanceOfSatisfying(ResponseStatusException.class, ex ->
                        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));

        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void updateProfileKeepsPasswordWhenNewPasswordIsBlankAndReturnsFreshToken() {
        AppUser user = user(1L, "Old Name", "old@example.com", "existing-hash");
        when(currentUser.get()).thenReturn(user);
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(user)).thenReturn(user);
        when(jwtService.generate(user)).thenReturn("new-token");

        var response = authService.updateProfile(new ProfileUpdateRequest("New Name", "NEW@example.com", " "));

        assertThat(user.getName()).isEqualTo("New Name");
        assertThat(user.getEmail()).isEqualTo("new@example.com");
        assertThat(user.getPasswordHash()).isEqualTo("existing-hash");
        assertThat(response.token()).isEqualTo("new-token");
        verify(passwordService, never()).hash(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void updateProfileRejectsEmailOwnedByAnotherUser() {
        AppUser user = user(1L, "User", "user@example.com", "hash");
        AppUser other = user(2L, "Other", "user@example.com", "hash");
        when(currentUser.get()).thenReturn(user);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> authService.updateProfile(new ProfileUpdateRequest("User", "user@example.com", null)))
                .isInstanceOfSatisfying(ResponseStatusException.class, ex ->
                        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));

        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    private AppUser user(Long id, String name, String email, String passwordHash) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        return user;
    }
}
