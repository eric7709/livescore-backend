package com.livescore.app.competition.dto;

import java.util.ArrayList;
import java.util.List;

import com.livescore.app.team.Team;

/**
 * Generates round-robin schedules using the standard "circle method":
 * one team is fixed, the rest rotate around it each round, guaranteeing
 * every team plays every other team exactly once per leg with a balanced
 * number of rounds.
 *
 * If the number of teams is odd, a synthetic "bye" slot is added and any
 * pairing involving it is simply dropped from that round.
 */
public final class RoundRobinFixtureGenerator {

    private RoundRobinFixtureGenerator() {
    }

    /**
     * Single leg: every team plays every other team once.
     * Returns one list of pairings per round.
     */
    public static List<List<FixturePairing>> generateSingleRoundRobin(List<Team> teams) {
        List<Team> rotating = new ArrayList<>(teams);
        boolean hasBye = rotating.size() % 2 != 0;
        if (hasBye) {
            rotating.add(null); // null = bye
        }

        int n = rotating.size();
        int rounds = n - 1;
        int half = n / 2;
        List<List<FixturePairing>> schedule = new ArrayList<>();

        for (int round = 0; round < rounds; round++) {
            List<FixturePairing> roundPairings = new ArrayList<>();

            for (int i = 0; i < half; i++) {
                Team a = rotating.get(i);
                Team b = rotating.get(n - 1 - i);

                if (a == null || b == null) {
                    continue; // this team has the bye this round
                }

                // alternate which side is "home" so it isn't always the same team
                boolean firstIsHome = (i + round) % 2 == 0;
                Team home = firstIsHome ? a : b;
                Team away = firstIsHome ? b : a;
                roundPairings.add(new FixturePairing(home, away));
            }

            schedule.add(roundPairings);

            // rotate everyone except the fixed team at index 0
            Team last = rotating.remove(n - 1);
            rotating.add(1, last);
        }

        return schedule;
    }

    /**
     * Double leg (EPL style): every team plays every other team home AND away.
     * The second leg is simply the first leg with home/away swapped, which
     * guarantees each reverse fixture exists exactly once.
     */
    public static List<List<FixturePairing>> generateDoubleRoundRobin(List<Team> teams) {
        List<List<FixturePairing>> firstLeg = generateSingleRoundRobin(teams);

        List<List<FixturePairing>> secondLeg = new ArrayList<>();
        for (List<FixturePairing> round : firstLeg) {
            List<FixturePairing> mirrored = new ArrayList<>();
            for (FixturePairing pairing : round) {
                mirrored.add(new FixturePairing(pairing.away(), pairing.home()));
            }
            secondLeg.add(mirrored);
        }

        List<List<FixturePairing>> full = new ArrayList<>(firstLeg);
        full.addAll(secondLeg);
        return full;
    }
}