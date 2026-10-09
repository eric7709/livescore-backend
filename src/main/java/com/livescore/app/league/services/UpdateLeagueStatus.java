package com.livescore.app.league.services;

import org.springframework.stereotype.Service;

import com.livescore.app.league.League;
import com.livescore.app.league.LeagueRepository;
import com.livescore.app.league.dto.LeagueDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateLeagueStatus {

    private final LeagueRepository leagueRepository;

    public LeagueDTO update(Long id, boolean active) {

        League league = leagueRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "League not found with id: " + id
                        )
                );

        league.setActive(active);

        League saved = leagueRepository.save(league);

        return LeagueDTO.fromEntity(saved);
    }
}