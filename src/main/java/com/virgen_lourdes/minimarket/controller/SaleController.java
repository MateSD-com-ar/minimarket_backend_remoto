package com.virgen_lourdes.minimarket.controller;

import com.virgen_lourdes.minimarket.dto.requestDto.SaleRequestDto;
import com.virgen_lourdes.minimarket.dto.responseDto.SaleResponseDto;
import com.virgen_lourdes.minimarket.service.ICrudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales")
public class SaleController {

    @Autowired
    private ICrudService<SaleRequestDto, SaleResponseDto, Long> saleService;

    @GetMapping
    public ResponseEntity<List<SaleResponseDto>> read(@ModelAttribute SaleRequestDto saleRequestDto) {
         List<SaleResponseDto> saleResponseDtos = saleService.read(saleRequestDto);
         return ResponseEntity.ok(saleResponseDtos);
    }

    @PostMapping
    public ResponseEntity<SaleResponseDto> create(@RequestBody @Validated SaleRequestDto saleRequestDto) {
        SaleResponseDto saleResponseDto = saleService.create(saleRequestDto);
        return ResponseEntity.ok(saleResponseDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SaleResponseDto> update(@RequestBody SaleRequestDto saleRequestDto, @PathVariable Long id) {
        SaleResponseDto saleResponseDto = saleService.update(saleRequestDto, id);
        return ResponseEntity.ok(saleResponseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        saleService.delete(id);
        return ResponseEntity.ok("Venta eliminada correctamente");
    }
}
