package com.zestio.app.match.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;
import java.util.List;

import com.zestio.app.match.Match;
import com.zestio.app.match.enums.MatchGround;
import com.zestio.app.match.enums.MatchPeriod;
import com.zestio.app.match.enums.MatchStatus;
import com.zestio.app.match.enums.MatchType;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchDTO {
    private Long id;
    private Integer homeScore;
    private Integer awayScore;
    private MatchPeriod period;
    private MatchStatus status;
    private MatchType matchType;
    private String abandonedReason;
    
    private String stadium;
    private Instant matchDate;
    private Instant startedAt;
    private Instant periodStartedAt;

    private Long homeTeamId;
    private String homeTeamName;
    private String homeTeamLogoUrl;
    private String homeTeamCode;

    private Long awayTeamId;
    private String awayTeamName;
    private String awayTeamLogoUrl;
    private String awayTeamCode;
    private MatchGround ground;

    private Long competitionId;
    private String competitionName;
    private String competitionLogoUrl;
    private boolean lineupSubmitted;
    private List<MatchPeriod> matchPeriods;

    public static MatchDTO fromEntity(Match match) {
        return MatchDTO.builder()
                .id(match.getId())
                .homeScore(match.getHomeScore())
                .awayScore(match.getAwayScore())
                .period(match.getPeriod())
                .status(match.getStatus())
                .abandonedReason(match.getAbandonedReason())
                .stadium(match.getStadium())
                .matchDate(match.getMatchDate())
                .startedAt(match.getStartedAt())
                .periodStartedAt(match.getPeriodStartedAt())
                .matchType(match.getMatchType())
                .matchPeriods(match.getMatchPeriods() != null ? match.getMatchPeriods() : null)
                .homeTeamId(match.getHomeTeam() != null ? match.getHomeTeam().getId() : null)
                .awayTeamId(match.getAwayTeam() != null ? match.getAwayTeam().getId() : null)
                .awayTeamLogoUrl(match.getAwayTeam() != null ? match.getAwayTeam().getLogoUrl() : null)
                .awayTeamCode(match.getAwayTeam() != null ? match.getAwayTeam().getTeamCode() : null)
                .homeTeamCode(match.getHomeTeam() != null ? match.getHomeTeam().getTeamCode() : null)
                .homeTeamLogoUrl(match.getHomeTeam() != null ? match.getHomeTeam().getLogoUrl() : null)
                .awayTeamName(match.getAwayTeam() != null ? match.getAwayTeam().getName() : null)
                .homeTeamName(match.getHomeTeam() != null ? match.getHomeTeam().getName() : null)
                .competitionId(match.getCompetition() != null ? match.getCompetition().getId() : null)
                .competitionName(match.getCompetition() != null ? match.getCompetition().getName() : null)
                .competitionLogoUrl(match.getCompetition() != null ? match.getCompetition().getLogoUrl() : null)
                .lineupSubmitted(match.getHomeLineup() != null && match.getAwayLineup()!=null)
                .build();
    }
}
