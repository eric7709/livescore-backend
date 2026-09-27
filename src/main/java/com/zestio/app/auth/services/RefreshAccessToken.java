package com.zestio.app.auth.services;

import com.zestio.app.auth.RefreshToken;
import com.zestio.app.auth.dto.AuthResponse;
import com.zestio.app.auth.security.JwtService;
import com.zestio.app.auth.security.RefreshTokenManager;
import com.zestio.app.profile.Profile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class RefreshAccessToken {

    private final RefreshTokenManager refreshTokenManager;
    private final JwtService jwtService;

    public AuthResponse refresh(String requestToken) {
        RefreshToken token = refreshTokenManager.findByToken(requestToken)
                .map(refreshTokenManager::verifyExpiration)
                .orElseThrow(() -> new IllegalStateException("Invalid or expired refresh token"));

        Profile profile = token.getProfile();
        String accessToken = jwtService.generateAccessToken(
                profile.getPhoneNumber(),
                Map.of("role", profile.getRole().name(), "profileId", profile.getId())
        );

        return new AuthResponse(accessToken, token.getToken(), "Bearer");
    }
}