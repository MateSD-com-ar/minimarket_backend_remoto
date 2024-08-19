package com.virgen_lourdes.minimarket.dto.requestDto;

import com.virgen_lourdes.minimarket.entity.enums.PaymentMethod;
import com.virgen_lourdes.minimarket.entity.enums.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class SaleRequestDto {

    private Long id;

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(min = 3, message = "El nombre debe tener al menos 3 caracteres")
    private String client;

    private String cuil;
    private LocalDateTime paymentDate;
    private PaymentMethod paymentMethod;
    private Double discount;
    private Double interest;
    private Double subtotal;
    private Double total;
    private Status status;

    @NotNull(message = "La venta debe tener un empleado vendedor asociado")
    private Long userId;

    @NotEmpty(message = "La venta debe tener al menos un producto")
    private List<SaleDetailsProductRequestDto> saleDetailsProducts;

    private LocalDateTime createdAt;

}
