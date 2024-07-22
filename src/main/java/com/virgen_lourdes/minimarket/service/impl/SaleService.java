package com.virgen_lourdes.minimarket.service.impl;

import com.virgen_lourdes.minimarket.dto.requestDto.SaleRequestDto;
import com.virgen_lourdes.minimarket.dto.responseDto.SaleResponseDto;
import com.virgen_lourdes.minimarket.repository.ISaleRepository;
import com.virgen_lourdes.minimarket.service.ICrudService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class SaleService implements ICrudService<SaleRequestDto, SaleResponseDto, Long> {

    @Autowired
    private ISaleRepository saleRepository;

    @Override
    public SaleResponseDto create(SaleRequestDto saleRequestDto) {
        return null;
    }

    @Override
    public List<SaleResponseDto> read(SaleRequestDto saleRequestDto) {
        return List.of();
    }

    @Override
    public SaleResponseDto update(SaleRequestDto saleRequestDto, Long id) {
        return null;
    }

    @Override
    public void delete(Long id) {

    }
}
