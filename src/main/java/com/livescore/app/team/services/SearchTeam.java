package com.livescore.app.team.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.livescore.app.team.Team;
import com.livescore.app.team.TeamRepository;

import lombok.RequiredArgsConstructor;

@Transactional
@Service
@RequiredArgsConstructor
public class SearchTeam {
    private final TeamRepository teamRepository;

    @Transactional(readOnly = true)
    public Page<Team> search(Long leagueId, String query, Pageable pageable) {
        return teamRepository.searchByLeague(
                leagueId,
                query,
                pageable);
    }

     @Transactional(readOnly = true)
    public List<Team> search(String query) {
        return teamRepository
                .findByNameContainingIgnoreCaseOrTeamCodeContainingIgnoreCase(
                        query,
                        query
                );
    }
}
