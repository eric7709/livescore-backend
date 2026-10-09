package com.livescore.app.auth.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.livescore.app.auth.User;
import com.livescore.app.auth.UserRepository;
import com.livescore.app.exceptions.BadRequestException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateUsersName {

    private final UserRepository userRepository;

    @Transactional
    public void execute(String email, String firstName, String lastName) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (firstName != null && !firstName.isBlank()) {
            user.setFirstName(firstName.trim());
        }

        if (lastName != null && !lastName.isBlank()) {
            user.setLastName(lastName.trim());
        }

        userRepository.save(user);
    }
}