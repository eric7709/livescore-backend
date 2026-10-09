package com.livescore.app.competition.dto;

import lombok.Data;

@Data
public class TeamStandingDTO {
    // Changed to primitive int to prevent NullPointerExceptions during sorting
    private int played;
    private int goalsFor;
    private int wins;
    private int losses;
    private int draws;
    private int points;
    private int goalDifference;
    private int goalsAgainst;
    // Kept as Objects because IDs can be null before saving, 
    // and Strings are always objects.
    private Long teamId;
    private String teamName;
    private String lastFive;
}