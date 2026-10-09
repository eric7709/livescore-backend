package com.livescore.app.auth.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.livescore.app.auth.User;
import com.livescore.app.auth.UserRepository;
import com.livescore.app.auth.dto.ChangePasswordRequestDTO;
import com.livescore.app.auth.utils.PasswordUtils;
import com.livescore.app.exceptions.BadRequestException;
import com.livescore.app.exceptions.NotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChangePassword {

    private static final String WRONG_CURRENT = "Current password is incorrect";
    private static final String SAME_AS_OLD = "New password must be different from the current one";

    private final UserRepository userRepository;
    private final PasswordUtils passwordUtils;

    @Transactional
    public void execute(Long userId, ChangePasswordRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!user.isEnabled()) {
            throw new BadRequestException("Account is disabled");
        }

        if (!passwordUtils.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException(WRONG_CURRENT);
        }

        if (passwordUtils.matches(request.getNewPassword(), user.getPassword())) {
            throw new BadRequestException(SAME_AS_OLD);
        }

        user.setPassword(passwordUtils.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password changed for user {}", user.getId());
    }
}