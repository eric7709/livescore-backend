package com.zestio.app.profile.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zestio.app.profile.Profile;
import com.zestio.app.profile.ProfileRepository;
import com.zestio.app.profile.enums.Position;
import com.zestio.app.profile.enums.Role;
import com.zestio.app.profile.helpers.ProfileSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetFilteredProfiles {

    private final ProfileRepository profileRepository;

    public Page<Profile> get(
            String search,
            Role role,
            Position position,
            Integer squadNumber,
            Long teamId,
            Pageable pageable) {

        Specification<Profile> specification =
                ProfileSpecification.filterBy(
                        search,
                        role,
                        position,
                        squadNumber,
                        teamId);

        return profileRepository.findAll(specification, pageable);
    }
}
