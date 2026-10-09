package com.livescore.app.leagueSubscription.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.livescore.app.leagueSubscription.LeagueSubscription;
import com.livescore.app.leagueSubscription.LeagueSubscriptionRepository;
import com.livescore.app.leagueSubscription.dto.LeagueSubscriptionDTO;
import com.livescore.app.leagueSubscription.dto.LeagueSubscriptionSearchRequest;
import com.livescore.app.leagueSubscription.utils.LeagueSubscriptionSpecification;
import com.livescore.app.utils.PageResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchLeagueSubscriptions {

    private final LeagueSubscriptionRepository subscriptionRepository;

    public PageResponse<LeagueSubscriptionDTO> search(
            LeagueSubscriptionSearchRequest request,
            Pageable pageable) {

        if (request == null) {
            request = new LeagueSubscriptionSearchRequest();
        }

        Specification<LeagueSubscription> specification = Specification
                .where(LeagueSubscriptionSpecification.query(request.getQuery()))
                .and(LeagueSubscriptionSpecification.leagueId(request.getLeagueId()))
                .and(LeagueSubscriptionSpecification.plan(request.getPlan()))
                .and(LeagueSubscriptionSpecification.status(request.getStatus()))
                .and(LeagueSubscriptionSpecification.startDateFrom(request.getStartDateFrom()))
                .and(LeagueSubscriptionSpecification.startDateTo(request.getStartDateTo()))
                .and(LeagueSubscriptionSpecification.endDateFrom(request.getEndDateFrom()))
                .and(LeagueSubscriptionSpecification.endDateTo(request.getEndDateTo()));

        Page<LeagueSubscriptionDTO> page = subscriptionRepository
                .findAll(specification, pageable)
                .map(LeagueSubscriptionDTO::fromEntity);

        PageResponse<LeagueSubscriptionDTO> response = new PageResponse<>();
        response.setContent(page.getContent());
        response.setPage(page.getNumber());
        response.setSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setLast(page.isLast());

        return response;
    }
}
