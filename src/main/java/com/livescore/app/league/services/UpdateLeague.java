package com.livescore.app.league.services;

import org.springframework.stereotype.Service;

import com.livescore.app.league.League;
import com.livescore.app.league.LeagueRepository;
import com.livescore.app.league.dto.LeagueDTO;
import com.livescore.app.league.dto.LeagueRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateLeague {

    private final LeagueRepository leagueRepository;

    public LeagueDTO update(Long id, LeagueRequest request) {

        if (request == null) {
            throw new IllegalArgumentException("League request is required");
        }

        League league = leagueRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "League not found with id: " + id
                        )
                );

        validateUpdate(league, request);

        if (request.getName() != null && !request.getName().isBlank()) {
            league.setName(request.getName().trim());
        }

        if (request.getSlug() != null && !request.getSlug().isBlank()) {

            String newSlug = request.getSlug()
                    .trim()
                    .toLowerCase();

            if (!newSlug.equals(league.getSlug())
                    && leagueRepository.existsBySlug(newSlug)) {

                throw new IllegalArgumentException(
                        "A league with this slug already exists"
                );
            }

            league.setSlug(newSlug);
        }

        if (request.getDescription() != null) {
            league.setDescription(request.getDescription());
        }

        if (request.getLogoUrl() != null) {
            league.setLogoUrl(request.getLogoUrl());
        }

        if (request.getSubscriptionPlan() != null) {
            league.setSubscriptionPlan(
                    request.getSubscriptionPlan()
            );
        }

        League saved = leagueRepository.save(league);

        return LeagueDTO.fromEntity(saved);
    }

    private void validateUpdate(
            League league,
            LeagueRequest request) {

        if (request.getName() != null
                && !request.getName().isBlank()) {

            boolean nameExists =
                    leagueRepository.existsByNameIgnoreCase(
                            request.getName().trim()
                    );

            if (nameExists
                    && !request.getName().trim()
                    .equalsIgnoreCase(league.getName())) {

                throw new IllegalArgumentException(
                        "A league with this name already exists"
                );
            }
        }
    }
}