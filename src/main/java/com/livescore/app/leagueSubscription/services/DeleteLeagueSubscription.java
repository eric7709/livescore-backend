package com.livescore.app.leagueSubscription.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.livescore.app.leagueSubscription.LeagueSubscription;
import com.livescore.app.leagueSubscription.LeagueSubscriptionRepository;
import com.livescore.app.leagueSubscription.enums.SubscriptionStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteLeagueSubscription {

    private final LeagueSubscriptionRepository subscriptionRepository;

    @Transactional
    public void delete(Long id) {
        LeagueSubscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "League subscription not found"));

        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscriptionRepository.save(subscription);
    }
}
