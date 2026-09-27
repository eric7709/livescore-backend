package com.zestio.app.auth.services;

import com.zestio.app.auth.security.RefreshTokenManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogoutProfile {

    private final RefreshTokenManager refreshTokenManager;

    public void logout(String refreshToken) {
        refreshTokenManager.revoke(refreshToken);
    }
}