package com.livescore.app.auth.services;

import org.springframework.stereotype.Service;

import com.livescore.app.auth.tempUserAndPasswordToken.TempUser;
import com.livescore.app.auth.tempUserAndPasswordToken.TempUserRepository;
import com.livescore.app.auth.dto.TempUserResponseDTO;
import com.livescore.app.exceptions.BadRequestException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor 
@Service 
public class GetTempUserFromToken {

    private final TempUserRepository tempUserRepository;
    
    public TempUserResponseDTO execute(String token){
        TempUser tempUser = tempUserRepository.findByToken(token).orElse(null);
        if(tempUser.equals(null)){
            throw new BadRequestException("Invalid or expired token");
        }
        return TempUserResponseDTO.toDTO(tempUser);
    }
    
}
