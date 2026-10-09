package com.livescore.app.competition.dto;

import com.livescore.app.team.Team;

/**
 * A single home/away pairing produced by the fixture generator, before it's
 */
public record FixturePairing(Team home, Team away) {
}