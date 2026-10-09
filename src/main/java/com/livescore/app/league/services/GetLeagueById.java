package com.livescore.app.league.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.livescore.app.league.LeagueRepository;
import com.livescore.app.league.dto.LeagueDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetLeagueById {

    private final LeagueRepository leagueRepository;

    @Transactional(readOnly = true)
    public LeagueDTO get(Long id) {

        return leagueRepository.findById(id)
                .map(LeagueDTO::fromEntity)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "League not found with id: " + id
                        )
                );
    }
}