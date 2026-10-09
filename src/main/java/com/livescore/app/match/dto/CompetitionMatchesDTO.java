package com.livescore.app.match.dto;

import java.util.List;
import com.livescore.app.competition.dto.CompetitionSummary;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionMatchesDTO {
    private CompetitionSummary competition;
    private List<MatchSummary> matches;
}