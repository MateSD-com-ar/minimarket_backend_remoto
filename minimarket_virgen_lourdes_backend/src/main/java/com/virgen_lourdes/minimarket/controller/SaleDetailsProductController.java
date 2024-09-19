package com.virgen_lourdes.minimarket.controller;

import com.virgen_lourdes.minimarket.dto.requestDto.SaleDetailsProductRequestDto;
import com.virgen_lourdes.minimarket.dto.responseDto.SaleDetailsProductResponseDto;
import com.virgen_lourdes.minimarket.entity.SaleDetailsProduct;
import com.virgen_lourdes.minimarket.exceptions.customExceptions.ProductNotFoundException;
import com.virgen_lourdes.minimarket.service.ISaleDetailsProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/details")
public class SaleDetailsProductController {

    @Autowired
    private ISaleDetailsProductService saleDetailsProductService;

    @GetMapping("/get")
    ResponseEntity<?> getAllDetails() {
        try {
            List<SaleDetailsProduct> saleDetailsProductList = saleDetailsProductService.getAllDetails();
            return ResponseEntity.ok(saleDetailsProductList);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/get/{idDetails}")
    ResponseEntity<?> getDetailsById(@PathVariable Long idDetails) {
        try {
            SaleDetailsProduct saleDetailsProduct = saleDetailsProductService.getDetailsById(idDetails);
            return ResponseEntity.ok(saleDetailsProduct);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{idDetails}")
    ResponseEntity<?> deleteDetails(@PathVariable Long idDetails) {
        try {
            saleDetailsProductService.deleteDetails(idDetails);
            return ResponseEntity.ok("Details deleted");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/create")
    ResponseEntity<List<SaleDetailsProductResponseDto>> createDetails(@Valid @RequestBody List<SaleDetailsProductRequestDto> saleDetailsProductRequestDto) {
        List<SaleDetailsProductResponseDto> saleDetailsProductResponseDtoList = saleDetailsProductService.createDetails(saleDetailsProductRequestDto);
        return ResponseEntity.ok(saleDetailsProductResponseDtoList);
    }

    @PutMapping("/edit/{idDetails}")
    ResponseEntity<?> editDetails(@PathVariable Long idDetails, @RequestBody SaleDetailsProductRequestDto saleDetailsProductRequestDto) {
        try {
            saleDetailsProductService.editDetails(idDetails, saleDetailsProductRequestDto);
            return ResponseEntity.ok(saleDetailsProductService.getDetailsById(idDetails));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

}
