package com.livescore.app.auth.services;

import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.livescore.app.auth.User;
import com.livescore.app.auth.UserRepository;
import com.livescore.app.auth.dto.AuthResponseDTO;
import com.livescore.app.auth.dto.LoginRequestDTO;
import com.livescore.app.auth.dto.LoginResult;
import com.livescore.app.auth.dto.MeResponseDTO;
import com.livescore.app.security.JwtService;
import com.livescore.app.auth.utils.PasswordUtils;
import com.livescore.app.exceptions.BadRequestException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginUser {
    private static final String INVALID_CREDENTIALS = "Invalid email or password";
    private final UserRepository userRepository;
    private final PasswordUtils passwordUtils;
    private final JwtService jwtService;

    @Transactional(readOnly = true)
    public LoginResult execute(LoginRequestDTO request) {
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException(INVALID_CREDENTIALS));
        if (!passwordUtils.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException(INVALID_CREDENTIALS);
        }
        if (!user.isEnabled()) {
            throw new BadRequestException("Account is disabled");
        }
        String subject = user.getEmail();
        AuthResponseDTO tokens = new AuthResponseDTO(
                jwtService.generateAccessToken(subject, Map.of("userId", user.getId())),
                jwtService.generateRefreshToken(subject));
        return new LoginResult(tokens, MeResponseDTO.fromEntity(user));
    }
}