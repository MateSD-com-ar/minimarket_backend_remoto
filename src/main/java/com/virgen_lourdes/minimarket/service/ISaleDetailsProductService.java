package com.virgen_lourdes.minimarket.service;

import com.virgen_lourdes.minimarket.dto.requestDto.SaleDetailsProductRequestDto;
import com.virgen_lourdes.minimarket.entity.SaleDetailsProduct;

import java.util.List;

public interface ISaleDetailsProductService {

    public List<SaleDetailsProduct> getAllDetails();
    public SaleDetailsProduct getDetailsById(Long idDetails);
    public void createDetails(SaleDetailsProductRequestDto saleDetailsProductRequestDto);
    public void deleteDetails (Long idDetails);
    public void editDetails (Long idDetails, SaleDetailsProductRequestDto saleDetailsProductRequestDto);

}
