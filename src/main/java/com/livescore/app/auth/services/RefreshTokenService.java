package com.livescore.app.auth.services;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.livescore.app.auth.User;
import com.livescore.app.auth.UserRepository;
import com.livescore.app.auth.dto.AuthResponseDTO;
import com.livescore.app.security.JwtService;
import com.livescore.app.exceptions.BadRequestException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String INVALID = "Invalid or expired refresh token";

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public AuthResponseDTO execute(String refreshToken) {
        String email;
        try {
            email = jwtService.extractSubject(refreshToken);
        } catch (Exception e) {
            throw new BadRequestException(INVALID);
        }

        if (!jwtService.isRefreshTokenValid(refreshToken, email)) {
            throw new BadRequestException(INVALID);
        }

        User user = userRepository.findByEmail(email)
                .filter(User::isEnabled)
                .orElseThrow(() -> new BadRequestException(INVALID));

        // Rotate: issue a new pair on every refresh
        return new AuthResponseDTO(
                jwtService.generateAccessToken(user.getEmail(), Map.of("userId", user.getId())),
                jwtService.generateRefreshToken(user.getEmail()));
    }
}