package com.retailcore.pos.auth;

import com.retailcore.pos.auth.dto.AuthResponse;
import com.retailcore.pos.auth.dto.LoginRequest;
import com.retailcore.pos.user.UserEntity;
import com.retailcore.pos.user.UserService;
import com.retailcore.pos.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        UserEntity user = userService.findByEmail(request.email());
        if (!user.isActive()) {
            throw new InactiveUserException();
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return new AuthResponse(jwtService.generateToken(user), UserResponse.from(user));
    }
}
