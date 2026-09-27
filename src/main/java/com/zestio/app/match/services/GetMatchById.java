package com.zestio.app.match.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zestio.app.match.dto.MatchDTO;
import com.zestio.app.utils.Resolver;

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
