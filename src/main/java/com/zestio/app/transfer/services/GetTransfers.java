package com.zestio.app.transfer.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zestio.app.transfer.Transfer;
import com.zestio.app.transfer.TransferRepository;
import com.zestio.app.transfer.dtos.TransferFilterDTO;
import com.zestio.app.transfer.dtos.TransferPageResponseDTO;
import com.zestio.app.transfer.helpers.TransferMapper;
import com.zestio.app.transfer.specifications.TransferSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetTransfers {

    private final TransferRepository transferRepository;
    private final TransferMapper transferMapper;

    public TransferPageResponseDTO execute(TransferFilterDTO filter, Pageable pageable) {
        Page<Transfer> page = transferRepository.findAll(
                TransferSpecification.withFilters(filter), pageable);

        return TransferPageResponseDTO.builder()
                .content(page.getContent().stream().map(transferMapper::toDTO).toList())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .page(page.getNumber())
                .size(page.getSize())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
