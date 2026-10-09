package com.livescore.app.team.services;

import org.springframework.dao.DataIntegrityViolationException;
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
public class CreateTeam {
    private final TeamRepository teamRepository;
    private final LeagueRepository leagueRepository;
    private final Resolver resolver;

    @Transactional
    public Team create(TeamRequestDTO dto) {
        validateName(dto.getName());
        ensureNameNotTaken(dto.getName());
        validateLeagueId(dto.getLeagueId());

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
        team.setLeague(resolveLeague(dto.getLeagueId()));
        if (dto.getManagerId() != null)
            team.setManager(resolveManager(dto.getManagerId()));
        return team;
    }

    private Profile resolveManager(Long id) {
        return resolver.resolveProfile(id);
    }

    private League resolveLeague(Long id) {
        return leagueRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("League not found with id: " + id));
    }

    private void validateLeagueId(Long leagueId) {
        if (leagueId == null)
            throw new BadRequestException("League is required");
    }

    private void ensureNameNotTaken(String name) {
        if (teamRepository.existsByNameIgnoreCase(name))
            throw new BadRequestException("Team already exists");

    }

    private void validateName(String name) {
        ValidationUtils.requireNonBlank(name, "Team name is required");
    }

}