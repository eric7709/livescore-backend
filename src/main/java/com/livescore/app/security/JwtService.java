package com.livescore.app.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Service
public class JwtService {

    private static final String TYPE_CLAIM = "type";
    private static final String ACCESS = "access";
    private static final String REFRESH = "refresh";

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    @Value("${app.jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    private SecretKey key;

    @PostConstruct
    void init() {
        // Fails fast at startup if the secret is shorter than 32 bytes
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // -------------------------------------------------------------------------
    // Generation
    // -------------------------------------------------------------------------

    public String generateAccessToken(String subject, Map<String, Object> extraClaims) {
        Map<String, Object> claims = new HashMap<>();
        if (extraClaims != null) {
            claims.putAll(extraClaims);
        }
        claims.put(TYPE_CLAIM, ACCESS);

        return buildToken(subject, claims, accessTokenExpirationMs);
    }

    public String generateRefreshToken(String subject) {
        return buildToken(subject, Map.of(TYPE_CLAIM, REFRESH), refreshTokenExpirationMs);
    }

    private String buildToken(String subject, Map<String, Object> claims, long expirationMs) {
        Date now = new Date();

        return Jwts.builder()
                .claims(claims)
                .id(UUID.randomUUID().toString()) // unique jti, lets you revoke later if needed
                .subject(subject)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key)
                .compact();
    }

    // -------------------------------------------------------------------------
    // Validation
    // -------------------------------------------------------------------------

    /** Valid signature, not expired, is an ACCESS token, and matches the subject. */
    public boolean isTokenValid(String token, String expectedSubject) {
        return isValid(token, expectedSubject, ACCESS);
    }

    /** Valid signature, not expired, is a REFRESH token, and matches the subject. */
    public boolean isRefreshTokenValid(String token, String expectedSubject) {
        return isValid(token, expectedSubject, REFRESH);
    }

    private boolean isValid(String token, String expectedSubject, String expectedType) {
        try {
            // parsing already verifies the signature and rejects expired tokens
            Claims claims = parseClaims(token);
            return expectedType.equals(claims.get(TYPE_CLAIM, String.class))
                    && claims.getSubject().equals(expectedSubject);
        } catch (Exception e) {
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // Extraction
    // -------------------------------------------------------------------------

    public String extractSubject(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(parseClaims(token));
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}