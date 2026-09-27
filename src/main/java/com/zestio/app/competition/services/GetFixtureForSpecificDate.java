package com.zestio.app.competition.services;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.springframework.stereotype.Service;

import com.zestio.app.competition.Competition;
import com.zestio.app.competition.dto.CompetitionFixture;
import com.zestio.app.competition.dto.Fixture;
import com.zestio.app.match.MatchRepository;
import com.zestio.app.match.enums.MatchStatus;
import com.zestio.app.utils.Resolver;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetFixtureForSpecificDate {
        private final Resolver resolver;
        private final MatchRepository matchRepository;

        private static final ZoneId ZONE = ZoneId.of("Africa/Lagos");

        public CompetitionFixture get(LocalDate date, Long competitionId) {
                Competition competition = resolver.resolveCompetition(competitionId);
                List<Fixture> fixtures = matchRepository
                                .findByCompetitionIdAndStatusOrderByMatchDateAsc(competitionId, MatchStatus.SCHEDULED)
                                .stream()
                                .filter(m -> date == null
                                                || m.getMatchDate().atZone(ZONE).toLocalDate().equals(date))
                                .filter(m -> competitionId == null
                                                || m.getCompetition().getId().equals(competitionId))
                                .map(el -> {
                                        return Fixture.toFixture(el);
                                })
                                .toList();
                CompetitionFixture competitionFixture = new CompetitionFixture();
                competitionFixture.setCompetitionCode(competition.getCompetitionCode());
                competitionFixture.setCompetitionId(competition.getId());
                competitionFixture.setCompetitionLogoUrl(competition.getLogoUrl());
                competitionFixture.setCompetitionName(competition.getName());
                competitionFixture.setFixtures(fixtures);
                return competitionFixture;

        }
}