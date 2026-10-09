package com.livescore.app.profile.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.livescore.app.exceptions.BadRequestException;
import com.livescore.app.profile.Profile;
import com.livescore.app.profile.dto.ProfileRequestDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateProfiles {

    private final CreateProfile createProfile;

    @Transactional
    public List<Profile> create(List<ProfileRequestDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            throw new BadRequestException("Profile list cannot be empty");
        }
        return dtos.stream()
                .map(dto -> createProfile.create(dto))
                .toList();
    }
}
