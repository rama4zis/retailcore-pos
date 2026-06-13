package com.retailcore.pos.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailcore.pos.auth.JwtService;
import com.retailcore.pos.common.exception.GlobalExceptionHandler;
import com.retailcore.pos.config.SecurityConfig;
import com.retailcore.pos.user.dto.UserCreateRequest;
import com.retailcore.pos.user.dto.UserResponse;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void createRequiresAdminRole() throws Exception {
        mockMvc.perform(post("/api/users")
                        .with(user("manager").roles("MANAGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    void createReturnsCreatedUserLocationForAdmin() throws Exception {
        when(userService.create(any(UserCreateRequest.class))).thenReturn(response(1L, UserRole.CASHIER, true));

        mockMvc.perform(post("/api/users")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/users/1"))
                .andExpect(jsonPath("$.email").value("cashier@example.com"));

        verify(userService).create(any(UserCreateRequest.class));
    }

    @Test
    void findAllReturnsUsersForAdmin() throws Exception {
        when(userService.findAll()).thenReturn(List.of(response(1L, UserRole.ADMIN, true)));

        mockMvc.perform(get("/api/users").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].role").value("ADMIN"));
    }

    @Test
    void changeRoleReturnsUpdatedUserForAdmin() throws Exception {
        when(userService.changeRole(1L, UserRole.MANAGER)).thenReturn(response(1L, UserRole.MANAGER, true));

        mockMvc.perform(patch("/api/users/1/role")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"MANAGER\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("MANAGER"));
    }

    @Test
    void changeActiveReturnsUpdatedUserForAdmin() throws Exception {
        when(userService.changeActive(1L, false)).thenReturn(response(1L, UserRole.CASHIER, false));

        mockMvc.perform(patch("/api/users/1/active")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    private static UserCreateRequest createRequest() {
        return new UserCreateRequest("cashier@example.com", "Cashier User", "secret123", UserRole.CASHIER, true);
    }

    private static UserResponse response(Long id, UserRole role, boolean active) {
        return new UserResponse(
                id,
                role == UserRole.ADMIN ? "admin@example.com" : "cashier@example.com",
                role == UserRole.ADMIN ? "Admin User" : "Cashier User",
                role,
                active,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z")
        );
    }
}
