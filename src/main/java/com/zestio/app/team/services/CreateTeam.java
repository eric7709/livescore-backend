package com.zestio.app.team.services;

import org.springframework.dao.DataIntegrityViolationException;
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
public class CreateTeam {
    private final TeamRepository teamRepository;
    private final Resolver resolver;

    @Transactional
    public Team create(TeamRequestDTO dto) {
        validateName(dto.getName());
        ensureNameNotTaken(dto.getName());

        try {
            return teamRepository.save(buildTeam(dto));
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException("Team already exists");
        }
    }

    private Team buildTeam(TeamRequestDTO dto) {
        Team team = new Team();
        team.setName(dto.getName());
        team.setLogoUrl(dto.getLogoUrl());
        team.setStadium(dto.getStadium());
        team.setTeamCode(dto.getTeamCode());
        if (dto.getManagerId() != null)
            team.setManager(resolveManager(dto.getManagerId()));
        return team;
    }

    private Profile resolveManager(Long id) {
        return resolver.resolveProfile(id);
    }

    private void ensureNameNotTaken(String name) {
        if (teamRepository.existsByNameIgnoreCase(name))
            throw new BadRequestException("Team already exists");

    }

    private void validateName(String name) {
        ValidationUtils.requireNonBlank(name, "Team name is required");
    }

}
