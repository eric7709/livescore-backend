package com.livescore.app.team.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.livescore.app.exceptions.BadRequestException;
import com.livescore.app.league.League;
import com.livescore.app.league.LeagueRepository;
import com.livescore.app.profile.Profile;
import com.livescore.app.team.Team;
import com.livescore.app.team.TeamRepository;
import com.livescore.app.team.dto.TeamRequestDTO;
import com.livescore.app.utils.Resolver;
import com.livescore.app.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateTeam {
    private final Resolver resolver;
    private final TeamRepository teamRepository;
    private final LeagueRepository leagueRepository;

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

        if (dto.getLeagueId() != null)
            team.setLeague(resolveLeague(dto.getLeagueId()));

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

    private League resolveLeague(Long id) {
        return leagueRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("League not found with id: " + id));
    }

    private void validateName(String name) {
        ValidationUtils.requireNonBlank(name, "Team name is required");
    }

}