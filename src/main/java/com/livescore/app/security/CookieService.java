package com.livescore.app.security;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import com.livescore.app.auth.dto.AuthResponseDTO;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class CookieService {

    public static final String ACCESS_COOKIE = "access_token";
    public static final String REFRESH_COOKIE = "refresh_token";

    private static final String ACCESS_PATH = "/";
    private static final String REFRESH_PATH = "/api/auth";

    // Keep these in sync with app.jwt.*-expiration-ms used by JwtService
    private static final Duration ACCESS_MAX_AGE = Duration.ofMinutes(15);
    private static final Duration REFRESH_MAX_AGE = Duration.ofDays(7);

    // Flip to true when serving over HTTPS
    private static final boolean SECURE = false;
    private static final String SAME_SITE = "Lax";

    public void addTokenCookies(HttpServletResponse response, AuthResponseDTO tokens) {
        add(response, build(ACCESS_COOKIE, tokens.getAccessToken(), ACCESS_PATH, ACCESS_MAX_AGE));
        add(response, build(REFRESH_COOKIE, tokens.getRefreshToken(), REFRESH_PATH, REFRESH_MAX_AGE));
    }

    public void clearTokenCookies(HttpServletResponse response) {
        add(response, build(ACCESS_COOKIE, "", ACCESS_PATH, Duration.ZERO));
        add(response, build(REFRESH_COOKIE, "", REFRESH_PATH, Duration.ZERO));
    }

    private ResponseCookie build(String name, String value, String path, Duration maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(SECURE)
                .sameSite(SAME_SITE)
                .path(path)
                .maxAge(maxAge)
                .build();
    }

    private void add(HttpServletResponse response, ResponseCookie cookie) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}