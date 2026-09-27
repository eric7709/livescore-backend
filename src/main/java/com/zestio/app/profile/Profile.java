package com.zestio.app.profile;

import java.time.LocalDate;

import com.zestio.app.profile.enums.CaptainStatus;
import com.zestio.app.profile.enums.PlayerStatus;
import com.zestio.app.profile.enums.Position;
import com.zestio.app.profile.enums.PreferredFoot;
import com.zestio.app.profile.enums.Role;
import com.zestio.app.team.Team;
import com.zestio.app.utils.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "profile")
@Getter
@Setter
public class Profile extends BaseEntity {
    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @Column(nullable = true)
    private String password; 

    private String fullName;

    private String avatarUrl;

    private Integer squadNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerStatus status = PlayerStatus.ACTIVE;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private Position position;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CaptainStatus captainStatus = CaptainStatus.NONE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private PreferredFoot preferredFoot;

    @Column(nullable = true)
    private Integer height; // in centimeters

    @Column(nullable = true)
    private LocalDate dateOfBirth;

    @PrePersist
    @PreUpdate
    private void updateFullName() {
        if (firstName != null && lastName != null) {
            this.fullName = firstName + " " + lastName;
        }
    }

    public String getFullName() {
        if (fullName == null && firstName != null && lastName != null) {
            return firstName + " " + lastName;
        }
        return fullName;
    }
}