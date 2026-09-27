package com.zestio.app.profile.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zestio.app.profile.Profile;
import com.zestio.app.profile.helpers.ProfileHelper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetProfile {

    private final ProfileHelper helper;

    public Profile get(Long id) {
        return helper.resolveProfile(id);
    }
}
