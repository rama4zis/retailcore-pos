package com.retailcore.pos.user;

import com.retailcore.pos.user.dto.UserCreateRequest;
import com.retailcore.pos.user.dto.UserResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse create(UserCreateRequest request) {
        String email = UserEntity.normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateUserEmailException(email);
        }

        boolean active = request.active() == null || request.active();
        UserEntity user = new UserEntity(
                email,
                request.name(),
                passwordEncoder.encode(request.password()),
                request.role(),
                active
        );

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserEntity findByEmail(String email) {
        String normalizedEmail = UserEntity.normalizeEmail(email);
        return userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new UserNotFoundException(normalizedEmail));
    }

    @Transactional
    public UserResponse changeRole(Long id, UserRole role) {
        UserEntity user = findEntityById(id);
        user.changeRole(role);
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse changeActive(Long id, boolean active) {
        UserEntity user = findEntityById(id);
        user.changeActive(active);
        return UserResponse.from(user);
    }

    private UserEntity findEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }
}
