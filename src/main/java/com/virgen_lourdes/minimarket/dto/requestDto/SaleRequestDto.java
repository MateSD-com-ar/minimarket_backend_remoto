package com.virgen_lourdes.minimarket.dto.requestDto;

import com.virgen_lourdes.minimarket.entity.SaleDetailsProduct;
import com.virgen_lourdes.minimarket.entity.enums.PaymentMethod;
import com.virgen_lourdes.minimarket.entity.enums.Status;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class SaleRequestDto {

    private Long id;

    @Size(min = 3, message = "El nombre debe tener al menos 3 caracteres")
    private String client;

    private String CUIL;
    private LocalDateTime paymentDate;
    private PaymentMethod paymentMethod;
    private Double discount;
    private Double interest;
    private Double subtotal;
    private Double total;
    private Status status;
    private Long userId;
    private List<SaleDetailsProduct> saleDetailsProducts;
    private LocalDateTime createdAt;

}
