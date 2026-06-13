package com.retailcore.pos.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.retailcore.pos.auth.dto.AuthResponse;
import com.retailcore.pos.auth.dto.LoginRequest;
import com.retailcore.pos.user.UserEntity;
import com.retailcore.pos.user.UserRole;
import com.retailcore.pos.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginReturnsJwtForActiveUserWithMatchingPassword() {
        UserEntity user = user(true);
        when(userService.findByEmail("admin@example.com")).thenReturn(user);
        when(passwordEncoder.matches("secret123", "hash")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        AuthResponse response = authService.login(new LoginRequest("admin@example.com", "secret123"));

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.user().email()).isEqualTo("admin@example.com");
    }

    @Test
    void loginRejectsWrongPassword() {
        UserEntity user = user(true);
        when(userService.findByEmail("admin@example.com")).thenReturn(user);
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("admin@example.com", "wrong")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid email or password");

        verify(jwtService, never()).generateToken(user);
    }

    @Test
    void loginRejectsInactiveUser() {
        UserEntity user = user(false);
        when(userService.findByEmail("admin@example.com")).thenReturn(user);

        assertThatThrownBy(() -> authService.login(new LoginRequest("admin@example.com", "secret123")))
                .isInstanceOf(InactiveUserException.class)
                .hasMessageContaining("User account is inactive");

        verify(passwordEncoder, never()).matches("secret123", "hash");
    }

    private static UserEntity user(boolean active) {
        UserEntity user = new UserEntity("admin@example.com", "Admin User", "hash", UserRole.ADMIN, active);
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }
}
