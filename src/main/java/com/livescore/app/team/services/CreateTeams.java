package com.livescore.app.team.services;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.livescore.app.exceptions.BadRequestException;
import com.livescore.app.profile.Profile;
import com.livescore.app.profile.ProfileRepository;
import com.livescore.app.team.Team;
import com.livescore.app.team.TeamRepository;
import com.livescore.app.team.dto.TeamRequestDTO;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CreateTeams {
    private final TeamRepository teamRepository;
    private final ProfileRepository profileRepository;

    @Transactional
    public List<Team> create(List<TeamRequestDTO> dtos) {
        if (dtos == null || dtos.isEmpty())
            throw new BadRequestException("Team list cannot be empty");

        List<String> names = dtos.stream()
                .filter(d -> d != null && d.getName() != null)
                .map(d -> normalize(d.getName()))
                .toList();
        ensureNoDuplicatesInRequest(names);
        ensureNoDuplicatesInDB(names);
        Map<Long, Profile> managers = preloadManagers(dtos);

        return teamRepository.saveAll(
                dtos.stream()
                        .filter(dto -> dto != null)
                        .map(dto -> buildTeam(dto, managers))
                        .toList());
    }
    private void ensureNoDuplicatesInRequest(List<String> names) {
        if (new HashSet<>(names).size() != names.size())
            throw new BadRequestException("Duplicate team names in request");
    }

    private void ensureNoDuplicatesInDB(List<String> names) {
        if (teamRepository.existsByNameInIgnoreCase(names))
            throw new BadRequestException("One or more teams already exist");
    }

    private Map<Long, Profile> preloadManagers(List<TeamRequestDTO> dtos) {
        List<Long> ids = dtos.stream()
                .filter(dto -> dto != null)
                .map(dto -> dto.getManagerId())
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        return profileRepository.findAllById(ids).stream()
                .filter(p -> p != null)
                .collect(Collectors.toMap(p -> p.getId(), p -> p));
    }

    private String normalize(String name) {
        return name.trim().toLowerCase();
    }

    private Team buildTeam(TeamRequestDTO dto, Map<Long, Profile> managers) {
        Team team = new Team();
        team.setName(dto.getName());
        team.setLogoUrl(dto.getLogoUrl());
        team.setTeamCode(dto.getTeamCode());
        if (dto.getManagerId() != null)
            team.setManager(managers.get(dto.getManagerId()));
        return team;
    }

}
