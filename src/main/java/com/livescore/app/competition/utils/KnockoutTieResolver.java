package com.livescore.app.competition.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.livescore.app.match.Match;
import com.livescore.app.match.enums.MatchStatus;
import com.livescore.app.team.Team;

/**
 * Knockout helpers shared by fixture generation, round advancement and
 * match updates. A "tie" is either one match (single leg) or two matches
 * between the same teams in the same round (leg 1 and leg 2, venues swapped).
 */
public final class KnockoutTieResolver {

    private KnockoutTieResolver() {
    }

    /** Same key for both legs of a tie, whichever team is at home. */
    public static String tieKey(Match match) {
        Long a = match.getHomeTeam().getId();
        Long b = match.getAwayTeam().getId();
        return Math.min(a, b) + "-" + Math.max(a, b);
    }

    /**
     * Groups one round's matches into ties, in bracket order (ordered by the
     * id of each tie's first match, i.e. the order they were generated in).
     */
    public static List<List<Match>> groupIntoTies(List<Match> roundMatches) {
        Map<String, List<Match>> ties = new LinkedHashMap<>();
        roundMatches.stream()
                .sorted(Comparator.comparing(Match::getId))
                .forEach(m -> ties.computeIfAbsent(tieKey(m), k -> new ArrayList<>()).add(m));
        return new ArrayList<>(ties.values());
    }

    /**
     * Winner of a tie.
     * - One match:  {@link Match#getWinner()} (score, then tieBreaker).
     * - Two legs:   aggregate goals; if level, the second leg's tieBreaker
     *               decides (HOME_* = second-leg home team, AWAY_* = second-leg away team).
     */
    public static Team winnerOf(List<Match> legs) {
        if (legs.size() == 1) {
            return legs.get(0).getWinner();
        }
        if (legs.size() != 2) {
            throw new IllegalStateException("A tie must have 1 or 2 matches, found " + legs.size());
        }

        List<Match> ordered = new ArrayList<>(legs);
        ordered.sort(Comparator.comparing((Match m) -> m.getLeg() == null ? 1 : m.getLeg())
                .thenComparing(Match::getId));
        Match first = ordered.get(0);
        Match second = ordered.get(1);

        if (first.getStatus() != MatchStatus.FINISHED || second.getStatus() != MatchStatus.FINISHED) {
            throw new IllegalStateException("Both legs must be finished before the tie can be decided");
        }

        Team teamA = first.getHomeTeam(); // away in the second leg
        Team teamB = first.getAwayTeam(); // home in the second leg
        int goalsA = first.getHomeScore() + second.getAwayScore();
        int goalsB = first.getAwayScore() + second.getHomeScore();

        if (goalsA != goalsB) {
            return goalsA > goalsB ? teamA : teamB;
        }
        return switch (second.getTieBreaker()) {
            case HOME_WIN_ON_PENALTIES, HOME_WIN_ON_AWAY_GOALS -> second.getHomeTeam();
            case AWAY_WIN_ON_PENALTIES, AWAY_WIN_ON_AWAY_GOALS -> second.getAwayTeam();
            case NONE -> throw new IllegalStateException(
                    "Tie " + teamA.getName() + " vs " + teamB.getName() + " is level "
                            + goalsA + "-" + goalsB + " on aggregate with no tieBreaker on the second leg");
        };
    }

    /**
     * Does finishing this match require a tieBreaker to be recorded?
     * - single leg: yes if it ended level
     * - leg 1:      never (a level first leg is normal)
     * - leg 2:      yes only if the AGGREGATE is level
     *
     * @param finished     the match being finished (its scores are used as-is)
     * @param roundMatches all matches of the same competition + round (may include {@code finished})
     */
    public static boolean needsTieBreaker(Match finished, List<Match> roundMatches) {
        boolean levelOnDay = finished.getHomeScore().equals(finished.getAwayScore());
        Integer leg = finished.getLeg();

        if (leg == null) {
            return levelOnDay;
        }
        if (leg == 1) {
            return false;
        }

        String key = tieKey(finished);
        return roundMatches.stream()
                .filter(m -> !m.getId().equals(finished.getId()))
                .filter(m -> Integer.valueOf(1).equals(m.getLeg()) && tieKey(m).equals(key))
                .findFirst()
                .map(first -> {
                    int goalsA = first.getHomeScore() + finished.getAwayScore();
                    int goalsB = first.getAwayScore() + finished.getHomeScore();
                    return goalsA == goalsB;
                })
                .orElse(levelOnDay);
    }
}