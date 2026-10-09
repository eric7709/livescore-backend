package com.livescore.app.match.dto;

import java.util.List;

public record MatchOverviewDTO(
        List<TeamFormEntryDTO> homeTeamForm,
        List<TeamFormEntryDTO> awayTeamForm,
        List<HeadToHeadEntryDTO> headToHead
) {}