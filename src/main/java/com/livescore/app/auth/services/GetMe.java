package com.livescore.app.auth.services;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.livescore.app.auth.UserRepository;
import com.livescore.app.auth.dto.MeResponseDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetMe {

    private final UserRepository userRepository;

    // Lazy fields (profile, league) are read in fromEntity, so this must stay transactional
    @Transactional(readOnly = true)
    public MeResponseDTO execute(String email) {
        return userRepository.findByEmail(email)
                .filter(user -> user.isEnabled())
                .map(MeResponseDTO::fromEntity)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated"));
    }
}