package com.zestio.app.team.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zestio.app.exceptions.BadRequestException;
import com.zestio.app.profile.Profile;
import com.zestio.app.team.Team;
import com.zestio.app.team.TeamRepository;
import com.zestio.app.team.dto.TeamRequestDTO;
import com.zestio.app.utils.Resolver;
import com.zestio.app.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateTeam {
    private final Resolver resolver;
    private final TeamRepository teamRepository;

    @Transactional
    public Team update(Long id, TeamRequestDTO dto) {
        Team team = resolver.resolveTeam(id);

        if (dto.getName() != null) {
            validateName(dto.getName());
            if (!team.getName().equalsIgnoreCase(dto.getName()))
                ensureNameNotTaken(dto.getName());
            team.setName(dto.getName());
        }

        if (dto.getLogoUrl() != null)
            team.setLogoUrl(dto.getLogoUrl());

        if (dto.getStadium() != null)
            team.setStadium(dto.getStadium());

        if (dto.getManagerId() != null)
            team.setManager(resolveManager(dto.getManagerId()));

        return teamRepository.save(team);
    }

    private void ensureNameNotTaken(String name) {
        if (teamRepository.existsByNameIgnoreCase(name))
            throw new BadRequestException("Team already exists");

    }

    private Profile resolveManager(Long id) {
        return resolver.resolveProfile(id);
    }

    private void validateName(String name) {
        ValidationUtils.requireNonBlank(name, "Team name is required");
    }

}
