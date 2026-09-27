package com.zestio.app.profile.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zestio.app.profile.ProfileRepository;
import com.zestio.app.profile.helpers.ProfileHelper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteProfile {

    private final ProfileRepository profileRepository;
    private final ProfileHelper helper;

    @Transactional
    public void delete(Long id) {
        profileRepository.delete(helper.resolveProfile(id));
    }
}
