package com.livescore.app.matchLineup.dtos;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class MatchPlayerStatsBothTeams {
    private Long matchId;
    private List<PlayerLineupInfo> homeTeam;
    private List<PlayerLineupInfo> awayTeam;
}