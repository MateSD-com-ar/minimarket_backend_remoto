package com.virgen_lourdes.minimarket.dto.requestDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SaleDetailsProductRequestDto {

    private Long idDetails;
    private Double amount;
    private Double unitPrice;
    private Double totalPriceDetail;
    private Long product;
    private Long sale;

}
