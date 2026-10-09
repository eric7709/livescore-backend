package com.livescore.app.leagueSubscription.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.livescore.app.leagueSubscription.LeagueSubscription;
import com.livescore.app.leagueSubscription.LeagueSubscriptionRepository;
import com.livescore.app.leagueSubscription.dto.LeagueSubscriptionDTO;
import com.livescore.app.leagueSubscription.enums.SubscriptionStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateLeagueSubscriptionStatus {

    private final LeagueSubscriptionRepository subscriptionRepository;

    @Transactional
    public LeagueSubscriptionDTO update(Long id, SubscriptionStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("status is required");
        }

        LeagueSubscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "League subscription not found"));

        subscription.setStatus(status);

        return LeagueSubscriptionDTO.fromEntity(
                subscriptionRepository.save(subscription));
    }
}
