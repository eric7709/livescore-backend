package com.livescore.app.auth.events;

public record PasswordResetRequestedEvent(
        String email,
        String firstName,
        String token,
        long expiresInMinutes
) {}