package com.virgen_lourdes.minimarket.dto.requestDto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SaleDetailsProductRequestDto {

    private Long idDetails;

    @NotNull(message = "Quantity must not be null")
    @Positive(message = "Quantity must be greater than zero")
    private Double quantity;

    @Positive(message = "UnitPrice must be greater than zero")
    private Double unitPrice;

    @Positive(message = "TotalPriceDetail must be greater than zero")
    private Double totalPriceDetail;

    @NotNull(message = "Product ID must not be null")
    private Long product;
    @NotNull(message = "Description must not be null")
    private String description;
    private String unitMeasure;
    @NotNull(message = "Sale ID must not be null")
    private Long saleId;

}
