package com.livescore.app.profile.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.livescore.app.profile.Profile;
import com.livescore.app.profile.ProfileRepository;
import com.livescore.app.profile.dto.ProfileQueryParams;
import com.livescore.app.profile.utils.ProfileSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SearchProfile {

    private final ProfileRepository profileRepository;

    @Transactional(readOnly = true)
    public Page<Profile> search(
            ProfileQueryParams request,
            Pageable pageable) {

        Specification<Profile> specification =
                ProfileSpecification.search(request);

        return profileRepository.findAll(
                specification,
                pageable
        );
    }
}