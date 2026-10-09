package com.livescore.app.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserQueryParams {

    /** Free-text search: matches email or full name (partial, case-insensitive). */
    private String search;

    private String firstName;
    private String lastName;
    private String email;

    private Boolean enabled;
    private Long profileId;
    private Long leagueId;
}