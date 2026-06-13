package com.retailcore.pos.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailcore.pos.auth.dto.AuthResponse;
import com.retailcore.pos.auth.dto.LoginRequest;
import com.retailcore.pos.common.exception.GlobalExceptionHandler;
import com.retailcore.pos.user.UserEntity;
import com.retailcore.pos.user.UserRole;
import com.retailcore.pos.user.dto.UserResponse;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.util.ReflectionTestUtils;

@WebMvcTest(
        controllers = AuthController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Test
    void loginReturnsTokenAndUser() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenReturn(new AuthResponse(
                "jwt-token",
                new UserResponse(1L, "admin@example.com", "Admin User", UserRole.ADMIN, true,
                        Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-01-01T00:00:00Z"))
        ));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("admin@example.com", "secret123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.user.email").value("admin@example.com"));
    }

    @Test
    void loginValidatesEmail() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("not-email", "secret123"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void loginRejectsInvalidCredentialsWithUnauthorized() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("admin@example.com", "wrong-password"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void meReturnsCurrentAuthenticatedUser() throws Exception {
        UserEntity user = new UserEntity("admin@example.com", "Admin User", "hash", UserRole.ADMIN, true);
        ReflectionTestUtils.setField(user, "id", 1L);
        UserPrincipal principal = new UserPrincipal(user);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities()
        );

        try {
            mockMvc.perform(get("/api/auth/me").principal(auth))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value("admin@example.com"))
                    .andExpect(jsonPath("$.role").value("ADMIN"));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
