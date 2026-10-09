package com.livescore.app.competition.services;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.livescore.app.competition.Competition;
import com.livescore.app.competition.CompetitionRepository;
import com.livescore.app.competition.dto.FixturePairing;
import com.livescore.app.competition.dto.RoundRobinFixtureGenerator;
import com.livescore.app.competition.enums.CompetitionLegFormat;
import com.livescore.app.competition.utils.FixtureGenerationGuard;
import com.livescore.app.competition.utils.KnockoutTieResolver;
import com.livescore.app.match.Match;
import com.livescore.app.match.MatchRepository;
import com.livescore.app.match.enums.MatchType;
import com.livescore.app.team.Team;
import com.livescore.app.team.TeamRepository;

import lombok.RequiredArgsConstructor;

/**
 * Fixture generation for cup competitions.
 *
 * Group stage + knockout (World Cup style):
 *   1. generateGroupStage(...) once, up front.
 *   2. generateNextKnockoutRound(...) with the qualifiers once groups finish.
 *   3. AdvanceKnockoutStage generates every later round automatically.
 *
 * Pure knockout (FA Cup style): see GenerateKnockoutFixtures, which builds
 * round 1 for you (including byes) via {@link #createKnockoutRound}.
 *
 * Leg format: if the competition's legFormat is DOUBLE, every knockout tie is
 * two matches (leg 1, then leg 2 with venues swapped, LEG_GAP later) and the
 * aggregate decides it. The final is always a single match (SINGLE_LEG_FINAL).
 *
 * Knockout fixtures can't be pre-generated as "TBD vs TBD" because
 * Match.homeTeam / awayTeam are non-null, so each round is created once the
 * previous one is decided.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class GenerateCupFixtures {

    /** Gap between leg 1 and leg 2 of a two-legged tie. */
    private static final Duration LEG_GAP = Duration.ofDays(7);

    /** Even in a DOUBLE competition, the final is one match. Set false for a two-legged final. */
    private static final boolean SINGLE_LEG_FINAL = true;

    private static final Set<MatchType> KNOCKOUT_TYPES = EnumSet.of(MatchType.KNOCKOUT, MatchType.FINAL);

    private final CompetitionRepository competitionRepository;
    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;

    /**
     * @param competitionId id of the competition to generate the group stage for
     * @param groupSize     teams per group, e.g. 4 (team count must divide evenly)
     * @param firstKickoff  date/time of each group's first round
     * @param roundInterval gap between successive rounds within a group
     */
    public List<Match> generateGroupStage(Long competitionId, int groupSize,
                                           LocalDateTime firstKickoff, Duration roundInterval) {
        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new IllegalArgumentException("Competition not found: " + competitionId));

        FixtureGenerationGuard.assertCanGenerate(
                competition, !matchRepository.findByCompetitionId(competitionId).isEmpty());

        List<Team> teams = new ArrayList<>(competition.getTeams());
        if (teams.size() % groupSize != 0) {
            throw new IllegalStateException(
                    "Team count (%d) is not evenly divisible by group size (%d)"
                            .formatted(teams.size(), groupSize));
        }

        List<List<Team>> groups = splitIntoGroups(teams, groupSize);
        List<Match> matches = new ArrayList<>();

        for (List<Team> group : groups) {
            List<List<FixturePairing>> groupSchedule = RoundRobinFixtureGenerator.generateSingleRoundRobin(group);
            LocalDateTime kickoff = firstKickoff;
            int roundNumber = 1;

            for (List<FixturePairing> round : groupSchedule) {
                for (FixturePairing pairing : round) {
                    matches.add(newMatch(competition, pairing.home(), pairing.away(),
                            kickoff, MatchType.REGULAR, roundNumber, null));
                }
                kickoff = kickoff.plus(roundInterval);
                roundNumber++;
            }
        }

        competition.setTotalTeams(teams.size());
        competitionRepository.save(competition);

        return matchRepository.saveAll(matches);
    }

    /**
     * Generates ONE knockout round from a known, ordered list of qualifying
     * team ids. Adjacent teams are paired: (0 vs 1), (2 vs 3), etc.
     * Honours the competition's legFormat (two legs per tie when DOUBLE).
     *
     * @param orderedQualifierIds even-length list of team ids, in bracket order
     * @param kickoff             date/time of the first leg (or the only match)
     * @param round               knockout round number - 1 for the first knockout round
     */
    public List<Match> generateNextKnockoutRound(Long competitionId, List<Long> orderedQualifierIds,
                                                  LocalDateTime kickoff, int round) {
        if (orderedQualifierIds.size() < 2 || orderedQualifierIds.size() % 2 != 0) {
            throw new IllegalStateException(
                    "Knockout round needs an even number of teams (got %d)"
                            .formatted(orderedQualifierIds.size()));
        }

        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new IllegalArgumentException("Competition not found: " + competitionId));

        Map<Long, Team> teamsById = new HashMap<>();
        teamRepository.findAllById(orderedQualifierIds).forEach(team -> teamsById.put(team.getId(), team));

        List<Team> orderedQualifiers = new ArrayList<>();
        for (Long id : orderedQualifierIds) {
            Team team = teamsById.get(id);
            if (team == null) {
                throw new IllegalArgumentException("Team not found: " + id);
            }
            orderedQualifiers.add(team);
        }

        return createKnockoutRound(competition, orderedQualifiers, round, kickoff,
                orderedQualifiers.size() == 2);
    }

    /**
     * Builds and saves one knockout round. Adjacent teams are paired.
     *
     * @param finalRound true if this round is the final (type FINAL, and a single
     *                   leg when SINGLE_LEG_FINAL). NOT inferred from team count,
     *                   because a preliminary round can have only 2 teams playing
     *                   while others have byes.
     */
    public List<Match> createKnockoutRound(Competition competition, List<Team> orderedTeams,
                                           int round, LocalDateTime kickoff, boolean finalRound) {
        boolean doubleLeg = competition.getLegFormat() == CompetitionLegFormat.DOUBLE
                && !(finalRound && SINGLE_LEG_FINAL);
        MatchType matchType = finalRound ? MatchType.FINAL : MatchType.KNOCKOUT;

        List<Match> matches = new ArrayList<>();
        for (int i = 0; i + 1 < orderedTeams.size(); i += 2) {
            Team a = orderedTeams.get(i);
            Team b = orderedTeams.get(i + 1);

            matches.add(newMatch(competition, a, b, kickoff, matchType, round, doubleLeg ? 1 : null));
            if (doubleLeg) {
                matches.add(newMatch(competition, b, a, kickoff.plus(LEG_GAP), matchType, round, 2));
            }
        }
        // Saved in order, so ids ascend in bracket order (AdvanceKnockoutStage relies on this).
        return matchRepository.saveAll(matches);
    }

    /**
     * Generates the next round from the previous round's match ids. Winners are
     * worked out per TIE (single match, or aggregate of two legs), and teams
     * that had a bye in the first knockout round join here.
     *
     * Throws if any match is unfinished, a tie is level with no tieBreaker,
     * or a match has no round / isn't a knockout match.
     */
    public List<Match> generateNextKnockoutRoundFromResults(Long competitionId, List<Long> previousRoundMatchIds,
                                                              LocalDateTime kickoff) {
        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new IllegalArgumentException("Competition not found: " + competitionId));

        List<Match> previousRound = matchRepository.findAllById(previousRoundMatchIds);
        if (previousRound.size() != previousRoundMatchIds.size()) {
            throw new IllegalArgumentException("One or more matches were not found");
        }

        Set<Integer> rounds = new HashSet<>();
        for (Match match : previousRound) {
            if (match.getRound() == null) {
                throw new IllegalStateException("Match " + match.getId()
                        + " has no round set - can't infer the next round number");
            }
            if (!KNOCKOUT_TYPES.contains(match.getMatchType())) {
                throw new IllegalStateException("Match " + match.getId() + " is not a knockout match");
            }
            rounds.add(match.getRound());
        }
        if (rounds.size() != 1) {
            throw new IllegalStateException("All matches must belong to the same round");
        }
        int previousRoundNumber = rounds.iterator().next();

        List<Team> winners = KnockoutTieResolver.groupIntoTies(previousRound).stream()
                .map(KnockoutTieResolver::winnerOf)
                .toList();

        List<Team> participants = interleave(byeTeams(competition, previousRoundNumber), winners);
        if (participants.size() < 2) {
            throw new IllegalStateException("The competition is already decided - no next round");
        }

        return createKnockoutRound(competition, participants, previousRoundNumber + 1, kickoff,
                participants.size() == 2);
    }

    /**
     * Knockout matches of one round (group matches that share the same round
     * number are excluded), in bracket order. Used by AdvanceKnockoutStage.
     */
    List<Match> findRoundMatches(Long competitionId, int round) {
        return matchRepository.findByCompetitionIdAndRound(competitionId, round).stream()
                .filter(m -> KNOCKOUT_TYPES.contains(m.getMatchType()))
                .sorted(Comparator.comparing(Match::getId))
                .toList();
    }

    // -------------------------------------------------------------------------

    /**
     * Teams that skipped the first knockout round (pure-knockout byes). A bye
     * team is one that has played no match at all in the competition - in a
     * group-stage cup everyone has group matches, so this is empty there.
     * Only applies when {@code finishedRound} is the first knockout round.
     */
    private List<Team> byeTeams(Competition competition, int finishedRound) {
        List<Match> all = matchRepository.findByCompetitionId(competition.getId());

        int firstKnockoutRound = all.stream()
                .filter(m -> KNOCKOUT_TYPES.contains(m.getMatchType()))
                .map(Match::getRound)
                .filter(Objects::nonNull)
                .min(Integer::compare)
                .orElse(finishedRound);
        if (finishedRound != firstKnockoutRound) {
            return List.of();
        }

        
        Set<Long> played = new HashSet<>();
        all.forEach(m -> {
            played.add(m.getHomeTeam().getId());
            played.add(m.getAwayTeam().getId());
        });
        return competition.getTeams().stream()
                .filter(t -> !played.contains(t.getId()))
                .sorted(Comparator.comparing(Team::getId))
                .toList();
    }

    /** bye1, winner1, bye2, winner2, ... so byes meet winners rather than each other. */
    private List<Team> interleave(List<Team> byes, List<Team> winners) {
        List<Team> out = new ArrayList<>();
        for (int i = 0; i < Math.max(byes.size(), winners.size()); i++) {
            if (i < byes.size()) {
                out.add(byes.get(i));
            }
            if (i < winners.size()) {
                out.add(winners.get(i));
            }
        }
        return out;
    }

    private Match newMatch(Competition competition, Team home, Team away, LocalDateTime kickoff,
                           MatchType type, int round, Integer leg) {
        Match match = new Match();
        match.setHomeTeam(home);
        match.setAwayTeam(away);
        match.setCompetition(competition);
        match.setMatchDate(kickoff.atZone(ZoneId.systemDefault()).toInstant());
        match.setMatchType(type);
        match.setRound(round);
        match.setLeg(leg);
        return match;
    }

    private List<List<Team>> splitIntoGroups(List<Team> teams, int groupSize) {
        List<List<Team>> groups = new ArrayList<>();
        for (int i = 0; i < teams.size(); i += groupSize) {
            groups.add(new ArrayList<>(teams.subList(i, i + groupSize)));
        }
        return groups;
    }
}
