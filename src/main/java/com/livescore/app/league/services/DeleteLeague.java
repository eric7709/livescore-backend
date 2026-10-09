package com.livescore.app.league.services;

import org.springframework.stereotype.Service;

import com.livescore.app.league.League;
import com.livescore.app.league.LeagueRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteLeague {

    private final LeagueRepository leagueRepository;

    public void delete(Long id) {

        League league = leagueRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "League not found with id: " + id
                        )
                );

        league.setActive(false);

        leagueRepository.save(league);
    }
}