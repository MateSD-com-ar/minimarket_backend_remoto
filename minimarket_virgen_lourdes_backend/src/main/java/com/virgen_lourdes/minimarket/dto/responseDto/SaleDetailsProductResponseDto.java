package com.virgen_lourdes.minimarket.dto.responseDto;

import com.virgen_lourdes.minimarket.entity.Product;
import com.virgen_lourdes.minimarket.entity.SaleDetailsProduct;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SaleDetailsProductResponseDto {

    private Long id;
    private double quantity;
    private double unitPrice;
    private double totalPrice;
    private String description;
    private Product product;

    public SaleDetailsProductResponseDto(SaleDetailsProduct saleDetailsProduct) {
        this.id = saleDetailsProduct.getIdDetails();
        this.quantity = saleDetailsProduct.getQuantity();
        this.unitPrice = saleDetailsProduct.getUnitPrice();
        this.totalPrice = saleDetailsProduct.getTotalPriceDetail();
        this.product = saleDetailsProduct.getProduct();
        this.description=saleDetailsProduct.getDescription();
    }

    public static SaleDetailsProductResponseDto of(SaleDetailsProduct saleDetailsProduct){
        return new SaleDetailsProductResponseDto(saleDetailsProduct);
    }
}
