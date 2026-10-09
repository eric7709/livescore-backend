package com.livescore.app.auth.services;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.livescore.app.auth.tempUserAndPasswordToken.PasswordResetToken;
import com.livescore.app.auth.tempUserAndPasswordToken.PasswordResetTokenRepository;
import com.livescore.app.auth.User;
import com.livescore.app.auth.UserRepository;
import com.livescore.app.auth.dto.ResetPasswordRequestDTO;
import com.livescore.app.auth.utils.PasswordUtils;
import com.livescore.app.exceptions.BadRequestException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResetPassword {

    private static final String INVALID_TOKEN = "This reset link is invalid or has expired";

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordUtils passwordUtils;

    @Transactional
    public void execute(ResetPasswordRequestDTO request) {
        PasswordResetToken token = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new BadRequestException(INVALID_TOKEN));

        if (token.isUsed() || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException(INVALID_TOKEN);
        }

        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new BadRequestException(INVALID_TOKEN));

        user.setPassword(passwordUtils.encode(request.getPassword()));
        userRepository.save(user);
        token.setUsed(true);
        tokenRepository.save(token);
        // Invalidate any other live tokens for this user (defensive).
        tokenRepository.invalidateAllForUser(user.getId());
    }
}