package com.zestio.app.matchEvents.enums;


public enum EventType {
    // Goals
    GOAL,
    OWN_GOAL,
    PENALTY_GOAL,
    FREE_KICK_GOAL,
    LONG_RANGE_GOAL,
    PENALTY_MISSED,
    PENALTY_AWARDED,

    // Cards
    YELLOW_CARD,
    RED_CARD,
    YELLOW_RED_CARD, // second yellow -> red

    // Substitutions
    SUBSTITUTION,

    // Set pieces
    CORNER,
    FREE_KICK,
    THROW_IN,
    GOAL_KICK,
    OFFSIDE,

    // Fouls / misconduct
    FOUL,

    // Shots
    SHOT_ON_TARGET,
    SHOT_OFF_TARGET,
    SHOT_BLOCKED,

    // Goalkeeping
    SAVE,

    // Possession / play
    ASSIST,
    KEY_PASS,
    DRIBBLE,
    INTERCEPTION,
    TACKLE,
    CLEARANCE,

    // Match management
    KICK_OFF,
    HALF_TIME,
    FULL_TIME,
    VAR_REVIEW,
    INJURY
}