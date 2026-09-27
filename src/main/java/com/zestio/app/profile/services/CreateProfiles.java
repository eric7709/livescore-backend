package com.zestio.app.profile.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zestio.app.exceptions.BadRequestException;
import com.zestio.app.profile.Profile;
import com.zestio.app.profile.dto.ProfileRequestDTO;

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
