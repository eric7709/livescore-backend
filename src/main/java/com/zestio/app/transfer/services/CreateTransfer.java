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
public class CreateTransfer {

    private final TransferRepository transferRepository;
    private final ProfileRepository profileRepository;
    private final TeamRepository teamRepository;
    private final TransferMapper transferMapper;

    public TransferResponseDTO execute(TransferRequestDTO request) {
        validate(request);

        Profile player = profileRepository.findById(request.getPlayerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found"));

        Team to = teamRepository.findById(request.getToTeamId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "To team not found"));

        validateDifferentTeams(player.getTeam(), to);

        Integer resolvedSquadNumber = resolveSquadNumber(player, to, request.getNewSquadNumber());

        Transfer transfer = new Transfer();
        transfer.setPlayer(player);
        transfer.setFrom(player.getTeam());
        transfer.setTo(to);
        transfer.setTransferType(request.getTransferType());
        transfer.setFee(normalizeFee(request.getFee(), request));
        transfer.setTransferDate(request.getTransferDate());

        player.setTeam(to);
        player.setSquadNumber(resolvedSquadNumber);
        profileRepository.save(player);

        return transferMapper.toDTO(transferRepository.save(transfer));
    }

    /**
     * Decides what squad number the player should carry at the destination team.
     * - No current number: nothing to resolve, pass through null (or the override, if given).
     * - Current number is free at the destination: keep it.
     * - Current number is taken: the caller must have supplied newSquadNumber, and
     *   that number must itself be free — otherwise this is a 409, giving the
     *   frontend a distinct signal to prompt for a different number.
     */
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
        if (request == null) throw bad("Request body is required");
        if (request.getPlayerId() == null) throw bad("Player is required");
        if (request.getToTeamId() == null) throw bad("To team is required");
        if (request.getTransferType() == null) throw bad("Transfer type is required");
        if (request.getTransferDate() == null) throw bad("Transfer date is required");
        if (request.getFee() != null && request.getFee().compareTo(BigDecimal.ZERO) < 0) {
            throw bad("Fee cannot be negative");
        }
        if (request.getNewSquadNumber() != null && request.getNewSquadNumber() < 0) {
            throw bad("Squad number cannot be negative");
        }
    }

    private BigDecimal normalizeFee(BigDecimal fee, TransferRequestDTO request) {
        if (request.getTransferType() == TransferType.FREE) return null;
        return fee;
    }

    private void validateDifferentTeams(Team from, Team to) {
        if (from != null && to != null && from.getId().equals(to.getId())) {
            throw bad("From team and to team must be different");
        }
    }

    private ResponseStatusException bad(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}