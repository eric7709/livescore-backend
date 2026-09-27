package com.zestio.app.config;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.zestio.app.competition.CompetitionRepository;
import com.zestio.app.match.MatchRepository;
import com.zestio.app.matchEvents.MatchEventRepository;
import com.zestio.app.matchLineup.LineupPlayerRepository;
import com.zestio.app.matchLineup.MatchLineupRepository;
import com.zestio.app.profile.ProfileRepository;
import com.zestio.app.team.TeamRepository;

@SpringBootTest
class SeedDataTest {

    @Autowired
    private CompetitionRepository competitionRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private MatchLineupRepository matchLineupRepository;

    @Autowired
    private LineupPlayerRepository lineupPlayerRepository;

    @Autowired
    private MatchEventRepository matchEventRepository;

    @Test
    void loadsAllSeedData() {
        assertTrue(competitionRepository.count() > 0, "Expected seeded competitions");
        assertTrue(teamRepository.count() > 0, "Expected seeded teams");
        assertTrue(profileRepository.count() > 0, "Expected seeded profiles");
        assertTrue(matchRepository.count() > 0, "Expected seeded matches");
        assertTrue(matchLineupRepository.count() > 0, "Expected seeded match lineups");
        assertTrue(lineupPlayerRepository.count() > 0, "Expected seeded lineup players");
        assertTrue(matchEventRepository.count() > 0, "Expected seeded match events");
        assertTrue(profileRepository.existsByPhoneNumber("0000000000"),
                "Expected the seeded admin profile");
    }
}