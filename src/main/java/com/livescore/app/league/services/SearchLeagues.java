package com.livescore.app.league.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.livescore.app.league.League;
import com.livescore.app.league.LeagueRepository;
import com.livescore.app.league.dto.LeagueDTO;
import com.livescore.app.league.dto.LeagueSearchRequest;
import com.livescore.app.league.utils.LeagueSpecification;
import com.livescore.app.utils.PageResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchLeagues {

    private final LeagueRepository leagueRepository;

    public PageResponse<LeagueDTO> search(
            LeagueSearchRequest request,
            Pageable pageable) {

        Specification<League> specification =
                Specification
                        .where(LeagueSpecification.query(
                                request.getQuery()))
                        .and(LeagueSpecification.name(
                                request.getName()))
                        .and(LeagueSpecification.slug(
                                request.getSlug()))
                        .and(LeagueSpecification.subscriptionPlan(
                                request.getSubscriptionPlan()))
                        .and(LeagueSpecification.subscriptionStatus(
                                request.getSubscriptionStatus()))
                        .and(LeagueSpecification.active(
                                request.getActive()))
                        .and(LeagueSpecification.createdFrom(
                                request.getCreatedFrom()))
                        .and(LeagueSpecification.createdTo(
                                request.getCreatedTo()));

        Page<LeagueDTO> page = leagueRepository
                .findAll(specification, pageable)
                .map(LeagueDTO::fromEntity);

        PageResponse<LeagueDTO> response = new PageResponse<>();

        response.setContent(page.getContent());
        response.setPage(page.getNumber());
        response.setSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setLast(page.isLast());

        return response;
    }
}