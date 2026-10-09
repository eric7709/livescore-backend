package com.livescore.app.league.services;

import org.springframework.stereotype.Service;

import com.livescore.app.league.League;
import com.livescore.app.league.LeagueRepository;
import com.livescore.app.league.dto.LeagueDTO;
import com.livescore.app.league.dto.LeagueRequest;
import com.livescore.app.leagueSubscription.enums.SubscriptionPlan;
import com.livescore.app.leagueSubscription.enums.SubscriptionStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateLeague {

    private final LeagueRepository leagueRepository;

    public LeagueDTO create(LeagueRequest request) {
        validateForCreate(request);
        League league = new League();
        league.setName(request.getName());
        league.setSlug(request.getSlug());
        league.setDescription(request.getDescription());
        league.setLogoUrl(request.getLogoUrl());

        if (request.getSubscriptionPlan() != null) {
            league.setSubscriptionPlan(request.getSubscriptionPlan());
        } else {
            league.setSubscriptionPlan(SubscriptionPlan.FREE);
        }
        league.setSubscriptionStatus(SubscriptionStatus.TRIAL);
        league.setActive(true);
        League saved = leagueRepository.save(league);
        return LeagueDTO.fromEntity(saved);
    }

    private void validateForCreate(LeagueRequest request) {

        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("League name is required");
        }

        if (request.getSlug() == null || request.getSlug().isBlank()) {
            throw new IllegalArgumentException("League slug is required");
        }

        if (leagueRepository.existsBySlug(request.getSlug())) {
            throw new IllegalArgumentException(
                    "A league with this slug already exists"
            );
        }

        if (leagueRepository.existsByNameIgnoreCase(request.getName())) {
            throw new IllegalArgumentException(
                    "A league with this name already exists"
            );
        }
    }
}