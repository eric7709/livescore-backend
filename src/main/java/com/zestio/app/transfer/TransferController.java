package com.zestio.app.transfer;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zestio.app.transfer.dtos.TransferFilterDTO;
import com.zestio.app.transfer.dtos.TransferPageResponseDTO;
import com.zestio.app.transfer.dtos.TransferRequestDTO;
import com.zestio.app.transfer.dtos.TransferResponseDTO;
import com.zestio.app.transfer.enums.TransferType;
import com.zestio.app.transfer.services.CreateTransfer;
import com.zestio.app.transfer.services.DeleteTransfer;
import com.zestio.app.transfer.services.GetTransfer;
import com.zestio.app.transfer.services.GetTransfers;
import com.zestio.app.transfer.services.UpdateTransfer;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final GetTransfers getTransfers;
    private final GetTransfer getTransfer;
    private final CreateTransfer createTransfer;
    private final UpdateTransfer updateTransfer;
    private final DeleteTransfer deleteTransfer;

    @GetMapping
    public ResponseEntity<TransferPageResponseDTO> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long playerId,
            @RequestParam(required = false) Long fromTeamId,
            @RequestParam(required = false) Long toTeamId,
            @RequestParam(required = false) TransferType transferType,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "transferDate") String sort,
            @RequestParam(defaultValue = "desc") String direction) {

        TransferFilterDTO filter = new TransferFilterDTO();
        filter.setSearch(search);
        filter.setPlayerId(playerId);
        filter.setFromTeamId(fromTeamId);
        filter.setToTeamId(toTeamId);
        filter.setTransferType(transferType);
        filter.setDateFrom(parseDate(dateFrom, "dateFrom"));
        filter.setDateTo(parseDate(dateTo, "dateTo"));

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction)
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(sortDirection, sort));

        return ResponseEntity.ok(getTransfers.execute(filter, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransferResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(getTransfer.execute(id));
    }

    @PostMapping
    public ResponseEntity<TransferResponseDTO> create(@RequestBody TransferRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(createTransfer.execute(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransferResponseDTO> update(
            @PathVariable Long id,
            @RequestBody TransferRequestDTO request) {
        return ResponseEntity.ok(updateTransfer.execute(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteTransfer.execute(id);
        return ResponseEntity.noContent().build();
    }

    private java.time.LocalDate parseDate(String value, String field) {
        if (value == null || value.isBlank()) return null;
        try {
            return java.time.LocalDate.parse(value);
        } catch (java.time.format.DateTimeParseException ex) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_REQUEST, field + " must use yyyy-MM-dd");
        }
    }
}
