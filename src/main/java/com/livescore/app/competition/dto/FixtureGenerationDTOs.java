package com.livescore.app.competition.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Request/response DTOs for the three fixture-generation endpoints.
 * Split into one file for convenience — feel free to break each record
 * out into its own file to match your existing one-class-per-file style.
 */
public final class FixtureGenerationDTOs {
        private FixtureGenerationDTOs() {
        }

        /** Body for POST /{competitionId}/fixtures/league */
        public record GenerateLeagueFixturesRequest(
                        LocalDateTime firstKickoff,
                        int roundIntervalDays) {
        }

        /** Body for POST /{competitionId}/fixtures/cup/groups */
        public record GenerateGroupStageRequest(
                        int groupSize,
                        LocalDateTime firstKickoff,
                        int roundIntervalDays) {
        }

        /**
         * Body for POST /{competitionId}/fixtures/cup/knockout
         * orderedQualifierIds must have an even length; adjacent ids (0v1, 2v3, ...)
         * become the pairings for this round.
         */
        public record GenerateKnockoutRoundRequest(
                        List<Long> orderedQualifierIds,
                        LocalDateTime kickoff,
                        int round) {
        }

        /**
         * Body for POST /{competitionId}/fixtures/cup/knockout/next
         * Alternative to GenerateKnockoutRoundRequest: pass the previous round's
         * match ids (in bracket order) instead of working out winners yourself.
         */
        public record GenerateNextKnockoutRoundRequest(
                        List<Long> previousRoundMatchIds,
                        LocalDateTime kickoff) {
        }

        public record GenerateKnockoutFixturesRequest(
                        LocalDateTime firstKickoff,
                        boolean shuffleTeams) {
        }

        /** Returned by all fixture-generation endpoints. */
        public record FixtureGenerationResponse(
                        CompetitionDTO competition,
                        int matchesCreated) {
        }
}