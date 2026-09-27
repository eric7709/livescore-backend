package com.zestio.app.matchEvents.dtos;
import java.util.List;

import com.zestio.app.match.enums.MatchPeriod;

import lombok.Data;

@Data
public class MatchStatistic {
    private MatchPeriod period;
    private String periodLabel;
    private List<EventTypeCount> statistics;
}