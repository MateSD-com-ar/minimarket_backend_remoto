package com.virgen_lourdes.minimarket.dto.responseDto;

import com.virgen_lourdes.minimarket.entity.Sale;
import com.virgen_lourdes.minimarket.entity.enums.PaymentMethod;
import com.virgen_lourdes.minimarket.entity.enums.PaymentStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class SaleResponseDto {

    private Long id;
    private String client;
    private String CUIL;
    private LocalDateTime paymentDate;
    private PaymentMethod paymentMethod;
    private Double discount;
    private Double interest;
    private Double subtotal;
    private Double total;
    private PaymentStatus paymentStatus;
    private UserResponseDto user;
    private List<SaleDetailsProductResponseDto> saleDetailsProducts;
    private LocalDateTime createdAt;

    public SaleResponseDto(Sale sale) {
        this.id = sale.getId();
        this.client = sale.getClient();
        this.CUIL = sale.getCUIL();
        this.paymentDate = sale.getPaymentDate();
        this.paymentMethod = sale.getPaymentMethod();
        this.discount = sale.getDiscount();
        this.interest = sale.getInterest();
        this.subtotal = sale.getSubtotal();
        this.total = sale.getTotal();
        this.paymentStatus = sale.getPaymentStatus();
        this.user = UserResponseDto.of(sale.getUser());
        this.saleDetailsProducts = sale.getSaleDetailsProducts() == null? null : sale.getSaleDetailsProducts().stream()
                .map(SaleDetailsProductResponseDto::of).toList();
        this.createdAt = sale.getCreatedAt();
    }

    public static SaleResponseDto of(Sale sale) {
        return new SaleResponseDto(sale);
    }
}
