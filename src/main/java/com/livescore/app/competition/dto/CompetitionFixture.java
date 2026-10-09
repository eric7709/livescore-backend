package com.livescore.app.competition.dto;

import java.util.List;

import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.NoArgsConstructor;

@Component
@Data
@NoArgsConstructor
public class CompetitionFixture {
    private Long competitionId;
    private String competitionName;
    private String competitionCode;
    private String competitionLogoUrl;
    private List<Fixture> fixtures;
}