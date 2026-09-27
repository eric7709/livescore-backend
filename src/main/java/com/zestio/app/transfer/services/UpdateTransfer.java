package com.zestio.app.transfer.services;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.zestio.app.profile.Profile;
import com.zestio.app.profile.ProfileRepository;
import com.zestio.app.team.Team;
import com.zestio.app.team.TeamRepository;
import com.zestio.app.transfer.Transfer;
import com.zestio.app.transfer.TransferRepository;
import com.zestio.app.transfer.dtos.TransferRequestDTO;
import com.zestio.app.transfer.dtos.TransferResponseDTO;
import com.zestio.app.transfer.enums.TransferType;
import com.zestio.app.transfer.helpers.TransferMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateTransfer {

    private final TransferRepository transferRepository;
    private final ProfileRepository profileRepository;
    private final TeamRepository teamRepository;
    private final TransferMapper transferMapper;

    public TransferResponseDTO execute(Long id, TransferRequestDTO request) {
        Transfer transfer = transferRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transfer not found"));

        validate(request);

        Profile player = profileRepository.findById(request.getPlayerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found"));

        Team to = teamRepository.findById(request.getToTeamId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "To team not found"));

        if (player.getTeam() != null && player.getTeam().getId().equals(to.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "From team and to team must be different");
        }

        Integer resolvedSquadNumber = resolveSquadNumber(player, to, request.getNewSquadNumber());

        transfer.setPlayer(player);
        transfer.setFrom(player.getTeam());
        transfer.setTo(to);
        transfer.setTransferType(request.getTransferType());
        transfer.setFee(request.getTransferType() == TransferType.FREE ? null : request.getFee());
        transfer.setTransferDate(request.getTransferDate());

        player.setSquadNumber(resolvedSquadNumber);
        profileRepository.save(player);

        return transferMapper.toDTO(transferRepository.save(transfer));
    }

    private Integer resolveSquadNumber(Profile player, Team to, Integer newSquadNumber) {
        Integer currentNumber = player.getSquadNumber();

        if (newSquadNumber != null) {
            if (profileRepository.existsByTeamAndSquadNumber(to, newSquadNumber)) {
                throw conflict(to, newSquadNumber);
            }
            return newSquadNumber;
        }

        if (currentNumber == null) {
            return null;
        }

        if (profileRepository.existsByTeamAndSquadNumber(to, currentNumber)) {
            throw conflict(to, currentNumber);
        }

        return currentNumber;
    }

    private ResponseStatusException conflict(Team to, Integer squadNumber) {
        return new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Squad number " + squadNumber + " is already taken at " + to.getName()
        );
    }

    private void validate(TransferRequestDTO request) {
        if (request == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        if (request.getPlayerId() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Player is required");
        if (request.getToTeamId() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "To team is required");
        if (request.getTransferType() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transfer type is required");
        if (request.getTransferDate() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transfer date is required");
        if (request.getFee() != null && request.getFee().compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fee cannot be negative");
        }
        if (request.getNewSquadNumber() != null && request.getNewSquadNumber() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Squad number cannot be negative");
        }
    }
}