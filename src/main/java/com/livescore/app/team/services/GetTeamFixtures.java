package com.livescore.app.team.services;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.livescore.app.match.Match;
import com.livescore.app.match.MatchRepository;
import com.livescore.app.match.enums.MatchStatus;
import com.livescore.app.team.dto.Fixture;
import com.livescore.app.utils.Resolver;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetTeamFixtures {
    private final Resolver resolver;
    private final MatchRepository matchRepository;
    private static final ZoneId ZONE = ZoneId.of("Africa/Lagos");

    
    public Page<Fixture> get(Long teamId, LocalDate date, Long competitionId, Pageable pageable) {
        resolver.resolveTeam(teamId);

        List<Fixture> filtered = matchRepository.findByTeamIdAndStatus(teamId, MatchStatus.SCHEDULED)
                .stream()
                .filter(m -> date == null
                        || (m.getMatchDate() != null
                                && m.getMatchDate().atZone(ZONE).toLocalDate().equals(date)))
                .filter(m -> competitionId == null
                        || (m.getCompetition() != null && m.getCompetition().getId().equals(competitionId)))
                .sorted(Comparator.comparing(
                        Match::getMatchDate,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ))
                .map(Fixture::toFixture)
                .toList();

        int start = (int) pageable.getOffset();
        if (start >= filtered.size()) {
            return new PageImpl<>(List.of(), pageable, filtered.size());
        }
        int end = Math.min(start + pageable.getPageSize(), filtered.size());

        return new PageImpl<>(filtered.subList(start, end), pageable, filtered.size());
    }
}