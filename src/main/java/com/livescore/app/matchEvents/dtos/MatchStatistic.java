package com.livescore.app.matchEvents.dtos;
import java.util.List;

import com.livescore.app.match.enums.MatchPeriod;

import lombok.Data;

@Data
public class MatchStatistic {
    private MatchPeriod period;
    private String periodLabel;
    private List<EventTypeCount> statistics;
}