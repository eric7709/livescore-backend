package com.livescore.app.team.services;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.livescore.app.match.MatchRepository;
import com.livescore.app.match.enums.MatchStatus;
import com.livescore.app.team.dto.Result;
import com.livescore.app.utils.Resolver;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetTeamResult {
    private final Resolver resolver;
    private final MatchRepository matchRepository;

    private static final ZoneId ZONE = ZoneId.of("Africa/Lagos");

    public Page<Result> get(Long teamId, LocalDate date, Long competitionId, Pageable pageable) {
        resolver.resolveTeam(teamId);

        List<Result> filtered = matchRepository.findByTeamIdAndStatus(teamId, MatchStatus.FINISHED)
                .stream()
                .filter(m -> date == null
                        || m.getMatchDate().atZone(ZONE).toLocalDate().equals(date))
                .filter(m -> competitionId == null
                        || m.getCompetition().getId().equals(competitionId))
                .map(match -> Result.toResult(match, teamId))
                .toList();

        int start = (int) pageable.getOffset();
        if (start >= filtered.size()) {
            return new PageImpl<>(List.of(), pageable, filtered.size());
        }
        int end = Math.min(start + pageable.getPageSize(), filtered.size());

        return new PageImpl<>(filtered.subList(start, end), pageable, filtered.size());
    }
}