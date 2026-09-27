package com.zestio.app.matchLineup.dtos;

import lombok.Data;

@Data
public class MatchLineupBothTeams{
    private Long matchId;
    private MatchLineupDTO homeTeam;
    private MatchLineupDTO awayTeam;
}