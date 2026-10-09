package com.livescore.app.auth.services;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Locale;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.livescore.app.auth.tempUserAndPasswordToken.PasswordResetToken;
import com.livescore.app.auth.tempUserAndPasswordToken.PasswordResetTokenRepository;
import com.livescore.app.auth.User;
import com.livescore.app.auth.UserRepository;
import com.livescore.app.auth.dto.ForgotPasswordRequestDTO;
import com.livescore.app.auth.events.PasswordResetRequestedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ForgotPassword {

    private static final long TOKEN_TTL_MINUTES = 30;
    private static final SecureRandom RNG = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Always returns normally, whether or not the email is registered, so
     * callers can't enumerate accounts. Email delivery happens after commit.
     */
    @Transactional
    public void execute(ForgotPasswordRequestDTO request) {
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        Optional<User> maybeUser = userRepository.findByEmail(email);
        if (maybeUser.isEmpty()) {
            log.debug("Password reset requested for unknown email {}", email);
            return;
        }
        User user = maybeUser.get();
        // Only one live token per user at a time.
        tokenRepository.invalidateAllForUser(user.getId());
        String rawToken = generateToken();
        LocalDateTime now = LocalDateTime.now();

        tokenRepository.save(PasswordResetToken.builder()
                .token(rawToken)
                .userId(user.getId())
                .expiresAt(now.plusMinutes(TOKEN_TTL_MINUTES))
                .used(false)
                .createdAt(now)
                .build());

        eventPublisher.publishEvent(new PasswordResetRequestedEvent(
                user.getEmail(),
                user.getFirstName(),
                rawToken,
                TOKEN_TTL_MINUTES));
    }

    private String generateToken() {
        byte[] bytes = new byte[48]; // 384-bit, URL-safe ~64 chars
        RNG.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}