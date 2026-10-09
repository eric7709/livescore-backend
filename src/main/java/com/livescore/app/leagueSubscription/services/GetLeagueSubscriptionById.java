package com.livescore.app.leagueSubscription.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.livescore.app.leagueSubscription.LeagueSubscriptionRepository;
import com.livescore.app.leagueSubscription.dto.LeagueSubscriptionDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetLeagueSubscriptionById {

    private final LeagueSubscriptionRepository subscriptionRepository;

    public LeagueSubscriptionDTO get(Long id) {
        return subscriptionRepository.findById(id)
                .map(LeagueSubscriptionDTO::fromEntity)
                .orElseThrow(() -> new IllegalArgumentException(
                        "League subscription not found with id: " + id));
    }
}
