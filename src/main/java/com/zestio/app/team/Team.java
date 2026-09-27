package com.zestio.app.team;

import com.zestio.app.competition.Competition;
import com.zestio.app.profile.Profile;
import com.zestio.app.utils.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Team extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    private String logoUrl;

    @Column(nullable = false)
    private String teamCode;

    private String stadium;
    
    // Manager is just ONE profile (no cascade chaos)
    @OneToOne
    @JoinColumn(name = "manager_id")
    private Profile manager;

    @ManyToMany(mappedBy = "teams")
    private Set<Competition> competitions = new HashSet<>();

    @OneToMany(mappedBy = "team")
    private List<Profile> players;
}