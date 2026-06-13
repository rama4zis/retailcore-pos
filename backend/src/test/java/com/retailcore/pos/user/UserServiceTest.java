package com.retailcore.pos.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.retailcore.pos.common.exception.DuplicateResourceException;
import com.retailcore.pos.user.dto.UserCreateRequest;
import com.retailcore.pos.user.dto.UserResponse;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void createHashesPasswordAndDefaultsActiveToTrue() {
        when(userRepository.existsByEmailIgnoreCase("admin@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed-password");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", 1L);
            return user;
        });

        UserResponse response = userService.create(new UserCreateRequest(
                " ADMIN@example.com ",
                " Admin User ",
                "secret123",
                UserRole.ADMIN,
                null
        ));

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        UserEntity saved = captor.getValue();

        assertThat(saved.getEmail()).isEqualTo("admin@example.com");
        assertThat(saved.getName()).isEqualTo("Admin User");
        assertThat(saved.getPasswordHash()).isEqualTo("hashed-password");
        assertThat(saved.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(saved.isActive()).isTrue();
        assertThat(response.email()).isEqualTo("admin@example.com");
    }

    @Test
    void createRejectsDuplicateEmail() {
        when(userRepository.existsByEmailIgnoreCase("admin@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(new UserCreateRequest(
                "admin@example.com",
                "Admin User",
                "secret123",
                UserRole.ADMIN,
                true
        )))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("User email already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    void findAllReturnsUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user(1L, "admin@example.com", UserRole.ADMIN, true)));

        List<UserResponse> responses = userService.findAll();

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().email()).isEqualTo("admin@example.com");
        assertThat(responses.getFirst().role()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void changeRoleUpdatesRole() {
        UserEntity user = user(1L, "cashier@example.com", UserRole.CASHIER, true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = userService.changeRole(1L, UserRole.MANAGER);

        assertThat(user.getRole()).isEqualTo(UserRole.MANAGER);
        assertThat(response.role()).isEqualTo(UserRole.MANAGER);
    }

    @Test
    void changeActiveUpdatesActiveFlag() {
        UserEntity user = user(1L, "cashier@example.com", UserRole.CASHIER, true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = userService.changeActive(1L, false);

        assertThat(user.isActive()).isFalse();
        assertThat(response.active()).isFalse();
    }

    @Test
    void findByEmailRejectsMissingUser() {
        when(userRepository.findByEmailIgnoreCase("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findByEmail("missing@example.com"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found with email: missing@example.com");
    }

    private static UserEntity user(Long id, String email, UserRole role, boolean active) {
        UserEntity user = new UserEntity(email, "Test User", "hash", role, active);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
