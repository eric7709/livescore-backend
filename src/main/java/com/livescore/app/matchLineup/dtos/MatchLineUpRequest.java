package com.livescore.app.matchLineup.dtos;

import java.util.List;

import com.livescore.app.matchLineup.enums.Formation;

import lombok.Data;

@Data
public class MatchLineUpRequest {
    private Long matchId;
    private Long teamId;
    private Long captainId;
    private Formation formation;
    private List<LineupPlayerRequest> players;
}