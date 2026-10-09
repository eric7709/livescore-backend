package com.livescore.app.match.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.livescore.app.match.dto.MatchDTO;
import com.livescore.app.utils.Resolver;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetMatchById {
    private final Resolver resolver;
    
    @Transactional(readOnly = true)
    public MatchDTO get(Long id) {
        return MatchDTO.fromEntity(resolver.resolveMatch(id));
    }

}
