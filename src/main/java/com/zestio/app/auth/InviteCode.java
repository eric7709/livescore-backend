package com.zestio.app.auth;

import com.zestio.app.profile.Profile;
import com.zestio.app.profile.enums.Role;
import com.zestio.app.team.Team;
import com.zestio.app.utils.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "invite_code")
@Getter
@Setter
public class InviteCode extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team; // null for ADMIN/MODERATOR invites not tied to a team

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_profile_id", nullable = false)
    private Profile createdBy;

    @Column(nullable = false)
    private Instant expiryDate;

    @Column(nullable = false)
    private boolean used = false;

    private Long usedByProfileId;
}